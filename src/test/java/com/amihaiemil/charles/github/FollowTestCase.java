package com.amihaiemil.charles.github;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.request.ApacheRequest;

/**
 * Unit tests for {@link Follow}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
@SuppressWarnings("resource")
public final class FollowTestCase {
    
    /**
     * Follow can follow the commander.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void fullowsUserSuccessfuly() throws Exception {
        int port = this.port();
        final MkContainer github = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpURLConnection.HTTP_NO_CONTENT)
        ).start(port);

        final Command com = this.mockCommand();
        Mockito.when(com.issue().repo().github().entry()).thenReturn(new ApacheRequest("http://localhost:" + port + "/"));
        
        final Logger logger = Mockito.mock(Logger.class);
        
        try {
            new Follow(new Step.Fake(true)).perform(com, logger);
            
            Mockito.verify(logger).info("Following Github user " + com.authorLogin() + " ...");
            Mockito.verify(logger).info("Followed user " + com.authorLogin() + " .");
            final MkQuery request = github.take();
            MatcherAssert.assertThat(
                request.uri().toString(),
                Matchers.equalTo("/user/following/" + com.authorLogin())
            );
            MatcherAssert.assertThat(request.method(), Matchers.equalTo("PUT"));
        } finally {
            github.stop();
        }
    }
    
    /**
     * Follow can stay silent if the response status is not the expected one.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void differentResponseStatus() throws Exception {
        int port = this.port();
        final MkContainer github = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpURLConnection.HTTP_INTERNAL_ERROR)
        ).start(port);
        
        final Command com = this.mockCommand();
        Mockito.when(com.issue().repo().github().entry()).thenReturn(new ApacheRequest("http://localhost:" + port + "/"));
        final Logger logger = Mockito.mock(Logger.class);
        
        try {
            new Follow(new Step.Fake(true)).perform(com, logger);
            
            Mockito.verify(logger).info("Following Github user " + com.authorLogin() + " ...");
            Mockito.verify(logger).error(
                "User follow status response is " + HttpURLConnection.HTTP_INTERNAL_ERROR  + " . Should have been 204 (NO CONTENT)"
            );
            final MkQuery request = github.take();
            MatcherAssert.assertThat(
                request.uri().toString(),
                Matchers.equalTo("/user/following/" + com.authorLogin())
            );
            MatcherAssert.assertThat(request.method(), Matchers.equalTo("PUT"));
        } finally {
            github.stop();
        }
    }
    
    /**
     * Follow can stay silent if there's an exception.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void serverIsDown() throws Exception {
        final Command com = this.mockCommand();
        Mockito.when(com.issue().repo().github().entry()).thenReturn(new ApacheRequest("http://localhost:" + this.port() + "/"));
        final Logger logger = Mockito.mock(Logger.class);
        
        new Follow(new Step.Fake(true)).perform(com, logger);
        
        Mockito.verify(logger).info("Following Github user " + com.authorLogin() + " ...");
        Mockito.verify(logger).error("IOException while trying to follow the user.");
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
     * Mock a command, add issue, repo and github mocks into it.
     * @return Command.
     */
    private Command mockCommand() {
        final Command com = Mockito.mock(Command.class);
        Mockito.when(com.authorLogin()).thenReturn("amihaiemil");
        
        final Issue issue = Mockito.mock(Issue.class);
        final Repo repo = Mockito.mock(Repo.class);
        Mockito.when(repo.github()).thenReturn(Mockito.mock(Github.class));
        Mockito.when(issue.repo()).thenReturn(repo);
        
        Mockito.when(com.issue()).thenReturn(issue);
        
        return com;
    }
}
