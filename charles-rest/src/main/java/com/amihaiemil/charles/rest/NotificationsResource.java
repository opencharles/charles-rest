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

import java.net.HttpURLConnection;

import javax.json.JsonArray;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
     * The http request.
     */
	@Context
	HttpServletRequest request;

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
	@Consumes(MediaType.APPLICATION_JSON)
    public Response postNotifications(JsonArray notifications) {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION);
		if(token == null || token.isEmpty()) {
			return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		} else {
		    if(token.equals("s3cr3t")) {
		    	return Response.ok().entity(notifications.toString()).build();
		    } else {
		    	return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
		    }
		}
    }
}
