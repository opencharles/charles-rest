/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of charles-rest nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.amihaiemil.charles.rest;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amihaiemil.charles.github.Action;
import com.amihaiemil.charles.rest.model.Notification;
import com.amihaiemil.charles.rest.model.Notifications;
import com.amihaiemil.charles.rest.model.SimplifiedNotifications;
import com.amihaiemil.charles.rest.model.WebhookNotifications;
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
@Path("/notifications")
@Stateless
public class NotificationsResource extends JsonResource {

    /**
     * Logger,
     */
    private static final Logger LOG = LoggerFactory.getLogger(NotificationsResource.class.getName());

    /**
     * The http request.
     */
    @Context
    private HttpServletRequest request;

    public NotificationsResource() {
        super(
            Json.createObjectBuilder()
                .add(
                    "postNotifications",
                    Json.createObjectBuilder()
                        .add("method", "POST /api/notifications/post")
                        .add("description", "Post notifications yourself. The body should be a JsonArray with Jsons of format {\"repoFullName\": \"user/repo\", \"issueNumber\":23}.")
                )
                .add(
                    "githubCommentWebhook",
                    Json.createObjectBuilder()
                        .add("method", "POST /api/notifications/ghook")
                        .add("description", "Setup a Github Webhook with application/json Content-Type and only check the \"Issue Comment\" event.")
                )
                .build()
        );
    }

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
    @Path("/post")
    public Response postNotifications(String notifications) {
        final String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(token == null || token.isEmpty()) {
            return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
        } else {
            final String key = System.getProperty("github.auth.token");
            if(token.equals(key)) {
                final boolean startedHandling = this.handleNotifications(new SimplifiedNotifications(notifications));
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
        return Response.serverError().build();
    }

    /**
     * Webhook for Github issue_comment event.
     * @param issueComment Event Json payload.
     * @see <a href=https://developer.github.com/v3/activity/events/types/#webhook-event-name-13>Github API</a>
     * @return Http response.
     */
    @POST
    @Path("/ghook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response webhook(final JsonObject issueComment) {
        final String event = this.request.getHeader("X-Github-Event");
        String userAgent = this.request.getHeader("User-Agent");
        if(userAgent == null) {
            userAgent = "";
        }
        if(userAgent.startsWith("GitHub-Hookshot/")) {
            if("ping".equalsIgnoreCase(event)) {
                return Response.ok().build();
            }
            if("issue_comment".equalsIgnoreCase(event)) {
                boolean startedHandling = this.handleNotifications(
                    new WebhookNotifications(issueComment)
                );
                if(startedHandling) {
                    return Response.ok().build();
                }
            }
        }
        return Response.status(HttpURLConnection.HTTP_PRECON_FAILED).build();
    }
    
    /**
     * Handles notifications, starts one action thread for each of them.
     * @param notifications List of notifications.
     * @return true if actions were started successfully; false otherwise.
     */
    private boolean handleNotifications(final Notifications notifications) {
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
                for(final Notification notification : notifications) {
                    this.take(
                        new Action(
                            gh.repos().get(
                                new Coordinates.Simple(notification.repoFullName())
                            ).issues().get(notification.issueNumber())
                        )
                    );
                }
                return true;
            } catch (IOException ex) {
                LOG.error("IOException while getting the Issue from Github API");
                return false;
            }
        }
    }

    /**
     * This JAX-RS resource in Json format.
     * @return Response.
     */
    @GET
    @Path("/")
    public Response json() {
        return Response.ok().entity(this.toString()).build();
    }

    /**
     * Take an action.
     * @param action Given action.
     */
    @Asynchronous
    private void take(Action action) {
        action.perform();
    }
}
