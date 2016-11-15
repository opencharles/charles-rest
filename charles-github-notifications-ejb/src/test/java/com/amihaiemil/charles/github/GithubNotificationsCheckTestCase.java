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
 *  3)Neither the name of charles-github-notifications-ejb nor the names of its
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;

/**
 * Unit tests for {@link GithubNotificationsCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class GithubNotificationsCheckTestCase {

    /**
     * GithubNotificationsCheck can tell if a Github notification is valid or not.
     */
    @Test
    public void validatesNotification() {
        GithubNotificationsCheck ghnc = new GithubNotificationsCheck();
        
        assertTrue(
            ghnc.isNotificationValid(
                this.mockNotification("mention", "/issue/url", "latest/comment/url", "")
            )
        );

        assertFalse(
            ghnc.isNotificationValid(
                this.mockNotification("mention", "/issue/url", "/issue/url", "")
            )
        );
        
        assertFalse(
            ghnc.isNotificationValid(
                this.mockNotification("other", "/issue/url", "latest/comment/url", "")
            )
        );
        
        assertFalse(
            ghnc.isNotificationValid(
                this.mockNotification("other", "/issue/url", "/issue/url", "")
            )
        );
    }

    /**
     * GithubNotificationsCheck can handle an empty notifications array.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void readsEmptyNotifications() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple("[]")).start(port);
        try {
            System.setProperty("github.auth.token", "githubtoken");
            System.setProperty("charles.rest.endpoint", "restendpointcharles");
            Logger logger = Mockito.mock(Logger.class);
            GithubNotificationsCheck ghnv = new GithubNotificationsCheck(
                "http://localhost:"+port+"/", logger
            );
            ghnv.readNotifications();
            Mockito.verify(logger).info("Found 0 new notifications!");
        } finally {
            server.stop();
        }
    }
    
    /**
     * GithubNotificationsCheck can handle an notifications array.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void readsNotifications() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple("[{\"notification\":\"first\"},{\"notification\":\"second\"}]"))
            .next(new MkAnswer.Simple(200))
            .start(port);
        try {
            System.setProperty("github.auth.token", "githubtoken");
            System.setProperty("charles.rest.endpoint", "restendpointcharles");
            Logger logger = Mockito.mock(Logger.class);
            GithubNotificationsCheck ghnv = Mockito.spy(
                new GithubNotificationsCheck(
                    "http://localhost:"+port+"/", logger
                )
            );
            Mockito.doReturn(true).when(ghnv).isNotificationValid(Mockito.any(JsonObject.class));
            Mockito.doReturn(true).when(ghnv).postNotifications(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyList()
            );
            Mockito.doCallRealMethod().when(ghnv).readNotifications();
            
            ghnv.readNotifications();
            
            Mockito.verify(logger).info("Found 2 new notifications!");
            Mockito.verify(logger).info("POST-ing 2 valid notifications!");
            Mockito.verify(logger).info("POST successful, marking notifications as read...");
            Mockito.verify(logger).info("Notifications marked as read!");
        } finally {
            server.stop();
        }
    }
    
    /**
     * GithubNotificationsCheck can post notifications successfully.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void postsNotificationsOk() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(200))
            .start(port);
        try {
            Logger logger = Mockito.mock(Logger.class);
            Mockito.doThrow(new IllegalStateException("Unexpected AssertionError..."))
                .when(logger)
                .error(Mockito.anyString(), Mockito.any(AssertionError.class));
            Mockito.doThrow(new IllegalStateException("Unexpected IOException..."))
                .when(logger)
                .error(Mockito.anyString(), Mockito.any(IOException.class));
            GithubNotificationsCheck ghnv = new GithubNotificationsCheck("", logger);
            
            List<JsonObject> notifications = new ArrayList<>();
            notifications.add(
                this.mockNotification("mentioned", "path/to/issue/1", "latest/comment/123", "amihaiemil/myrepo")
            );
            notifications.add(
                this.mockNotification("mentioned", "path/to/issue/2", "latest/comment/124", "amihaiemil/myrepo2")
            );

            assertTrue(
                ghnv.postNotifications("http://localhost:"+port+"/", "token", notifications)
            );
            
        } finally {
            server.stop();
        }
    }
    
    /**
     * GithubNotificationsCheck can post notifications and handle an HTTP unauthorized response status.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void postsNotificationsUnauthorized() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(401))
            .start(port);
        try {
            Logger logger = Mockito.mock(Logger.class);
            Mockito.doThrow(new IllegalStateException("Unexpected AssertionError..."))
                .when(logger)
                .error(Mockito.anyString(), Mockito.any(AssertionError.class));
            Mockito.doThrow(new IllegalStateException("Unexpected IOException..."))
                .when(logger)
                .error(Mockito.anyString(), Mockito.any(IOException.class));
            GithubNotificationsCheck ghnv = new GithubNotificationsCheck("", logger);
            
            assertFalse(
                ghnv.postNotifications("http://localhost:"+port+"/", "token", new ArrayList<JsonObject>())
            );
            
        } finally {
            server.stop();
        }
    }
    
    /**
     * GithubNotificationsCheck can catch and log an undexpected status response when posting notifications
     * @throws Exception If something goes wrong.
     */
    @Test
    public void postsNotificationsServerError() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(500))
            .start(port);
        try {
            Logger logger = Mockito.mock(Logger.class);
            GithubNotificationsCheck ghnv = new GithubNotificationsCheck("", logger);
            
            assertFalse(
                ghnv.postNotifications("http://localhost:"+port+"/", "token", new ArrayList<JsonObject>())
            );
            Mockito.verify(logger).error(Mockito.anyString(), Mockito.any(AssertionError.class));
            
        } finally {
            server.stop();
        }
    }
    
    /**
     * GithubNotificationsCheck can catch and log an IOException when posting notifications
     * @throws Exception If something goes wrong.
     */
    @Test
    public void postsNotificationsIoException() throws Exception {
        int port = this.port();
        Logger logger = Mockito.mock(Logger.class);
        GithubNotificationsCheck ghnv = new GithubNotificationsCheck("", logger);
        assertFalse(
            ghnv.postNotifications("http://localhost:"+this.port()+"/", "token", new ArrayList<JsonObject>())
        );
        Mockito.verify(logger).error(Mockito.anyString(), Mockito.any(IOException.class));
    }
    
    /**
     * GithubNotificationsCheck catches and logs server error from Github Notifications APi
     * @throws Exception If something goes wrong.
     */
    @Test
    public void serverErrorWhenCheckingNotifications() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(500)).start(port);
        try {
            System.setProperty("github.auth.token", "githubtoken");
            System.setProperty("charles.rest.endpoint", "restendpointcharles");
            Logger logger = Mockito.mock(Logger.class);
            GithubNotificationsCheck ghnv = new GithubNotificationsCheck(
                "http://localhost:"+port+"/", logger
            );
            ghnv.readNotifications();
            Mockito.verify(logger).error(Mockito.anyString(), Mockito.any(AssertionError.class));
        } finally {
            server.stop();
        }
    }

    /**
     * GithubNotificationsCheck catches and logs IOException from Github Notifications APi
     * @throws Exception If something goes wrong.
     */
    @Test
    public void ioExceptionWhenCheckingNotifications() throws Exception {
        System.setProperty("github.auth.token", "githubtoken");
        System.setProperty("charles.rest.endpoint", "restendpointcharles");
        Logger logger = Mockito.mock(Logger.class);
        GithubNotificationsCheck ghnv = new GithubNotificationsCheck(
            "http://localhost:"+this.port()+"/", logger
        );
        ghnv.readNotifications();
        Mockito.verify(logger).error(Mockito.anyString(), Mockito.any(IOException.class));
    }
    
    /**
     * GithubNotificationsCheck logs an error if the github auth token is missing
     * @throws Exception If something goes wrong.
     */
    @Test
    public void missingGithubAuthToken() throws Exception {
        Logger logger = Mockito.mock(Logger.class);
        GithubNotificationsCheck ghnv = new GithubNotificationsCheck(
            "http://localhost:8080/", logger
        );
        ghnv.readNotifications();
        Mockito.verify(logger).error(
            "Missing github.auth.token system property! Please specify the Github's agent authorization token!"
       );
    }
    
    /**
     * GithubNotificationsCheck logs an error if the chales rest endpoint is missing.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void missingCharlesRestEndpoint() throws Exception {
        System.setProperty("github.auth.token", "githubtoken");
        Logger logger = Mockito.mock(Logger.class);
        GithubNotificationsCheck ghnv = new GithubNotificationsCheck(
            "http://localhost:8080/", logger
        );
        ghnv.readNotifications();
        Mockito.verify(logger).error(
            "Missing charles.rest.roken system property! Please specify the REST endpoint where notifications are posted!"
       );
    }
    
    /**
     * Mock a notification json object returned by the Github API.
     * @param reason reson of the notification.
     * @param url Issue url.
     * @param latestCommendUrl Latest comment url.
     * @param repoFullName Repository's fullname (user/repo)
     * @return JsonObject
     */
    private JsonObject mockNotification(String reason, String url, String latestCommendUrl, String repoFullName) {
        return Json.createObjectBuilder()
        .add("reason", reason)
        .add(
            "subject",
            Json.createObjectBuilder()
                .add("url", url)
                .add("latest_comment_url", latestCommendUrl)
                .build()
        )
        .add(
            "repository",
            Json.createObjectBuilder()
                .add("full_name", repoFullName)
                .build()
        )
        .build();
    }
    
    /**
     * Find a free port.
     * @return A free port.
     * @throws IOException If something goes wrong.
     */
    private int port() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
    
    /**
     * Some tests set these 3 pros and they need to be cleared after
     * each test run so the next one has a fresh start.
     */
    @After
    public void cleanupSysProps() {
        System.clearProperty("github.auth.token");
        System.clearProperty("charles.rest.endpoint");
    }
}
