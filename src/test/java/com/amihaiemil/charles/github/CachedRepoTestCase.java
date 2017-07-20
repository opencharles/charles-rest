/*
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of charles-rest nor the names of its
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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.mockito.Mockito;

import com.jcabi.github.Repo;
import com.jcabi.github.Repos.RepoCreate;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;

/**
 * Unit tests for {@link CachedRepo}
 * @author MIhai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
@SuppressWarnings("resource")
public class CachedRepoTestCase {
    
    /**
     * CommandedRepo can tell when the repo has a gh-pages branch.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void repoHasGhPagesBranch() throws Exception {
        int port = this.port();
		MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK))
            .start(port);
        try {
            CachedRepo crepo = Mockito.spy(
                new CachedRepo(Mockito.mock(Repo.class))
            );
            Mockito.when(crepo.json()).thenReturn(Json
                .createObjectBuilder()
                .add(
                    "branches_url",
                    "http://localhost:" + port + "/branches{/branch}"
                ).build()
            );
            assertTrue(
                "Expected a gh-pages branch!",
                crepo.hasGhPagesBranch()
            );
        } finally {
            server.stop();
        }
    }
    
    /**
     * CommandedRepo can tell when the repo doesn't
     * have a gh-pages branch.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void repoHasNoGhPagesBranch() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_FOUND))
            .start(port);
        try {
            CachedRepo crepo = Mockito.spy(
                new CachedRepo(Mockito.mock(Repo.class))
            );
            Mockito.when(crepo.json()).thenReturn(Json
                .createObjectBuilder()
                .add(
                    "branches_url",
                    "http://localhost:" + port + "/branches{/branch}"
                ).build()
            );
            assertFalse(
                "Unexpected gh-pages branch!",
                crepo.hasGhPagesBranch()
            );
        } finally {
            server.stop();
        }
    }
    
    /**
     * CommandedRepo throws IOException if the http response
     * status is not appropriate.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void unexpectedHttpStatusFromBranchesAPI() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_BAD_REQUEST))
            .start(port);
        try {
            CachedRepo crepo = Mockito.spy(
                new CachedRepo(Mockito.mock(Repo.class))
            );
            Mockito.when(crepo.json()).thenReturn(Json
                .createObjectBuilder()
                .add(
                    "branches_url",
                    "http://localhost:" + port + "/branches{/branch}"
                ).build()
            );
            crepo.hasGhPagesBranch();
            fail("Expected an IOException here");
        } catch (IOException ex) {
            assertTrue(
                ex.getMessage()
                    .equals("Unexpected HTTP status response.")
            );
        } finally {
            server.stop();
        }
    }
    
    /**
     * CommandedRepo can represent itself in json format.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void getsJson() throws Exception {
        MkGithub gh = new MkGithub("amihaiemil");
        Repo rep = gh.repos().create(new RepoCreate("charlesrepo", false));
        CachedRepo crepo = new CachedRepo(rep);
        JsonObject repoJson = crepo.json();
        assertTrue(crepo.name().equals("charlesrepo"));
        assertTrue(repoJson.getString("private").equals("false"));

        JsonObject repoFromCache = crepo.json();
        assertTrue(repoJson == repoFromCache);
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
}
