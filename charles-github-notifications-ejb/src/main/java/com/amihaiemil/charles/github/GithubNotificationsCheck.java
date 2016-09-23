/*
 * Copyright (c) 2016, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of charles-github-ejb nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.amihaiemil.charles.github;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.http.Request;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;

/**
 * EJB that checks periodically for github notifications (mentions of the agent using @username).
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
@Singleton
@Startup
public class GithubNotificationsCheck {
	private static final Logger LOG = LoggerFactory.getLogger(GithubNotificationsCheck.class.getName());
	
	@Resource
	TimerService timerService;

	/**
	 * After this bean is constructed the checks are scheduled at a given interval (minutes),
	 * which defaults to 2.
	 */
	@PostConstruct
	public void scheduleChecks() {
		String checksInterval = System.getProperty("checks.interval.minutes");
		int intervalMinutes = 2;
		if(checksInterval != null && !checksInterval.isEmpty()) {
			try {
				intervalMinutes = Integer.parseInt(checksInterval);
				LOG.info("The check for Github notifications will be performed every " + intervalMinutes + " minutes!");
			} catch (NumberFormatException ex) {
				LOG.error("NumberFormatException when parsing interval " + checksInterval, ex);
			}
		}
		timerService.createTimer(1000*60*intervalMinutes, 1000*60*intervalMinutes, null);
	}
	
	/**
	 * Read notifications from the Github API when the scheduler timeout occurs.
	 */
	@Timeout
	public void readNotifications() {
        String token = System.getProperty("github.auth.token");
        String handlerEndpoint = System.getProperty("charles.rest.endpoint");
        String handlerEndpointToken = System.getProperty("charles.rest.token");

        if(token == null || token.isEmpty()) {
            LOG.error("Missing github.auth.token system property! Please specify the Github's agent authorization token!");
        } else {
            if(handlerEndpoint == null || handlerEndpoint.isEmpty()) {
                LOG.error("Missing charles.rest.roken system property! Please specify the REST endpoint where notifications are posted!");
            } else {
            	if(handlerEndpointToken == null || handlerEndpointToken.isEmpty()) {
            		LOG.error("Missing charles.rest.token system property! Please specify it so we can authenticate to " + handlerEndpoint + " !");
            	} else {
                    Request req = new ApacheRequest("https://api.github.com/notifications");
                    req = req.header(
                        HttpHeaders.AUTHORIZATION, String.format("token %s", token)
                    );
                    try {
			            JsonArray notifications = req.fetch()
			                .as(RestResponse.class).assertStatus(HttpURLConnection.HTTP_OK)
		                    .as(JsonResponse.class).json().readArray();
			            LOG.info("Found " + notifications.size() + " new notifications!");
			            if(notifications.size() > 0) {
			                List<JsonObject> validNotifications = new ArrayList<JsonObject>();
			                for(int i=0; i<notifications.size(); i++) {
			    	            JsonObject notification = notifications.getJsonObject(i);
			    	            if(this.isNotificationValid(notification)) {
			    		            validNotifications.add(notification);
			                    }
			                }
			                LOG.info("POST-ing " + validNotifications.size() + " valid notifications!");
			            
			                boolean posted = this.postNotifications(handlerEndpoint, handlerEndpointToken, validNotifications);
			            
			                if(posted) {//if the notifications were successfully posted to the REST service, mark them as read.
                                LOG.info("POST successful, marking notifications as read...");
			            	    req.uri()
			            	        .queryParam(
			        	                "last_read_at",
			        				    DateFormatUtils.formatUTC(
			        			            new Date(System.currentTimeMillis()),
			        					    "yyyy-MM-dd'T'HH:mm:ss'Z'"
			        			        )
			        			    ).back()
			        			    .method(Request.PUT).body().set("{}").back().fetch()
			        			    .as(RestResponse.class)
			        			    .assertStatus(
			        			        Matchers.isOneOf(
			        		                HttpURLConnection.HTTP_OK,
			        		                HttpURLConnection.HTTP_RESET
			        		            )
			        			    );
			            	    LOG.info("Notifications marked as read!");
			                }
			            }
        	        } catch (AssertionError aerr) {
        	            LOG.error("Unexpected status from https://api.github.com/notifications", aerr);
			        } catch (IOException e) {
				        LOG.error("IOException when calling https://api.github.com/notifications", e);
			        }
            	}
            }
        }
	}

	/**
	 * Validates a Github notification.<br><br>
	 * A notification is valid if the reson is "mention".<br> However, once
	 * mentioned in an issue, all the notifications from that issue will have this
	 * reason, so we also check if "url" and "last_comment_url" are the same or not. <br> 
	 * If they are the same, them the notification is not about a comment, but about close/reopen issue.
	 * 
	 * @param notification Github notification json.
	 * @return True if notification is valid; false otherwise.
	 */
	public boolean isNotificationValid(JsonObject notification) {
        if("mention".equals(notification.getString("reason"))) {
            JsonObject subject = notification.getJsonObject("subject"); 
            if(!subject.getString("url").equals(subject.getString("latest_comment_url"))) {
                return true;
            }
        }
        return false;
	}
	
    /**
     * Sends simplified notifications to the REST endpoint.
     * <br><br>
     * Only send the repoFullName and issueNumber from each notification.
     * The following handling logic (finding the first mentioning comment etc) is done
     * on the other side.
     * @param endpoint REST endpoint.
     * @param token Authorization token between this agent and the handling REST endpoint.
     * @param notifications Github notifications.
     * @return true if the Noifications were successfully posted, false otherwise.
     */
    public boolean postNotifications(String endpoint, String token, List<JsonObject> notifications) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for(JsonObject notification : notifications) {
            JsonObject subject = notification.getJsonObject("subject");
            String issueUrl = subject.getString("url");
            arrayBuilder.add(
    		    Json.createObjectBuilder()
    		        .add("repoFullName", notification.getJsonObject("repository").getString("full_name"))
    		        .add("issueNumber", Integer.parseInt(issueUrl.substring(issueUrl.lastIndexOf("/") + 1)))
    		        .build()
    		);
    	}
        Request req = new ApacheRequest(endpoint);
        req = req.header(
            HttpHeaders.AUTHORIZATION, token
        );
        try {
			int status = req.method(Request.POST).body().set(arrayBuilder.build()).back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(
			        Matchers.isOneOf(
			            HttpURLConnection.HTTP_OK,
			            HttpURLConnection.HTTP_UNAUTHORIZED,
			            HttpURLConnection.HTTP_UNAVAILABLE
			        )
			    ).status();
			if(status == HttpURLConnection.HTTP_OK) {
				return true;
			}
        } catch (AssertionError aerr) {
            LOG.error("Unexpected status from " + endpoint, aerr);
        } catch (IOException e) {
	        LOG.error("IOException when calling " + endpoint, e);
        }
	    return false;
    }
    
}
