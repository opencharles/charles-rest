/*
 Copyright (c) 2016, Mihai Emil Andronache
 All rights reserved.
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 * Neither the name of charles-rest nor the names of its
 contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.amihaiemil.charles.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amihaiemil.charles.github.Action;
import com.amihaiemil.charles.github.Notification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.wire.RetryWire;

/**
 * REST interface to receive Github notifications form the EJB checker.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
@Path("/")
public class NotificationsResource {

    /**
     * Logger,
     */
	private static final Logger LOG = LoggerFactory.getLogger(NotificationsResource.class.getName());

	/**
     * The http request.
     */
	@Context
	private HttpServletRequest request;

	/**
	 * Consumes a JsonArray consisting of Github notifications json objects.
	 * The <b>notifications are simplified</b>: a notification json looks like this:
	 * <pre>
	 * {
	 *     "repoFullName":"amihaiemil/myrepo",
	 *     "issueNumber":23
	 * }
	 * </pre>
	 * This info is enough. Since we have the repo and the issue that triggered the notification, we can easily
	 * find out the earliest comment where the Github agent was tagged.
	 * <br><br>
	 * <b>IMPORTANT:</b><br>
	 * Each call to this endpoint has to contain the <b>Authorization http header</b>, with a token
	 * <b>agreed upon</b> by this service and the EJB checker.<br>Technically, it might as well be the
	 * Github auth token since it has to be the same on both parties, but this is really sensitive information
	 * and we should pass it around as little as possible.<br><br>
	 * 
	 * @param notifications Json array of simplified Github notifications.
	 * @return Http Response.
	 */
	@POST
	@Path("notifications")
    public Response postNotifications(String notifications) {
		try {
		    String token = request.getHeader(HttpHeaders.AUTHORIZATION);
		    if(token == null || token.isEmpty()) {
			    return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		    } else {
		    	String key = System.getProperty("charles.rest.token");
		        if(token.equals(key)) {
		        	if(startedActionThreads() > 15) {
		        		return Response.status(HttpURLConnection.HTTP_UNAVAILABLE).build();
		        	}
		        	ObjectMapper mapper = new ObjectMapper();
				    List<Notification> parsedNotifications = mapper.readValue(notifications, new TypeReference<List<Notification>>(){});
		    	    boolean startedHandling = this.handleNotifications(parsedNotifications);
		    	    if(startedHandling) {
		    	    	return Response.ok().build();
		    	    }
		        } else {
		        	if(key == null || key.isEmpty()) {
		        		LOG.error("Missing token charles.rest.token (system property)! Please specify it!");
		        	}
		    	    return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		        }
	     	}
        } catch (IOException ex) {
            LOG.error("Exception when parsing Json notifications!", ex);
        }
		return Response.serverError().build();
	}

	/**
	 * Handles notifications, starts one action thread for each of them.
	 * @param notifications List of notifications.
	 * @return true if actions were started successfully; false otherwise.
	 */
	private boolean handleNotifications(List<Notification> notifications) {
		String authToken = System.getProperty("github.auth.token");
		if(authToken == null || authToken.isEmpty()) {
	        LOG.error("Missing github.auth.token. Please specify a Github api access token!");
		    return false;
		} else {
		    Github gh = new RtGithub(
		        new RtGithub(
		            authToken
		        ).entry().through(RetryWire.class)
	        );
		    try {
		        String agentLogin = gh.users().self().login();
		        for(Notification notification : notifications) {
		            new Action(
		                gh.repos().get(
					        new Coordinates.Simple(notification.getRepoFullName())
					    ).issues().get(notification.getIssueNumber()),
				 	    agentLogin
		            ).take();
		        }
		        LOG.info("Started " + notifications.size() + " actions, to handle each notification!");
		        return true;
		    } catch (IOException ex) {
		    	LOG.error("IOException while getting the Issue from Github API");
		    	return false;
		    }
		}
	}

    /**
     * When notifications are received, we check how many action threads are currently running.
     * If there are too many action threads started, we return HTTP 503 code, so the caller knows
     * to try again later when we have less load running.
     * @return number of Action threads started.
     */
    private int startedActionThreads() {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        int runningActions = 0;
        for(Thread tr : threads) {
            if(tr.getName().startsWith("Action_")) {
                runningActions ++;
            }
        }
        return runningActions;
	}
}
