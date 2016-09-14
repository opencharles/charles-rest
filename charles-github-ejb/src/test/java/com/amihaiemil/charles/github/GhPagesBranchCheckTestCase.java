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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ServerSocket;

import javax.json.Json;
import javax.json.JsonObject;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;

/**
 * Unit tests for {@link GhPagesBranchCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class GhPagesBranchCheckTestCase {

	/**
	 * GhPagesBranchCheck can tell if the gh-pages branch exists in the repo.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void ghpagesBranchExists() throws Exception {
		int port = this.port();
		MkContainer server = new MkGrizzlyContainer().next(new MkAnswer.Simple(HttpStatus.SC_OK)).start(port);
		String branchesUrl = "http://localhost:" + port + "/path/to/branches{/branch}";
		try {
			JsonObject repo = Json.createObjectBuilder().add("branches_url", branchesUrl).build();
			Logger logger = Mockito.mock(Logger.class);
			Mockito.doNothing().when(logger).info(Mockito.anyString());
			Mockito.doNothing().when(logger).warn(Mockito.anyString());
			Mockito.doNothing().when(logger).error(Mockito.anyString());

			GhPagesBranchCheck gpc = new GhPagesBranchCheck(repo, logger);
			assertTrue(gpc.perform());
		} finally {
			server.stop();
		}
	}
	
	/**
	 * GhPagesBranchCheck can tell if the gh-pages branch does not exist in the repo.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void ghpagesBranchDoesntExist() throws Exception {
		int port = this.port();
		MkContainer server = new MkGrizzlyContainer().next(new MkAnswer.Simple(HttpStatus.SC_NOT_FOUND)).start(port);
		String branchesUrl = "http://localhost:" + port + "/path/to/branches{/branch}";
		try {
			JsonObject repo = Json.createObjectBuilder().add("branches_url", branchesUrl).build();
			Logger logger = Mockito.mock(Logger.class);
			Mockito.doNothing().when(logger).info(Mockito.anyString());
			Mockito.doNothing().when(logger).warn(Mockito.anyString());
			Mockito.doNothing().when(logger).error(Mockito.anyString());

			GhPagesBranchCheck gpc = new GhPagesBranchCheck(repo, logger);
			assertFalse(gpc.perform());
		} finally {
			server.stop();
		}
	}
	
	/**
	 * GhPagesBranchCheck.perform returns false on Github server error.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void performsFalseOnGithubServerError() throws Exception {
		int port = this.port();
		MkContainer server = new MkGrizzlyContainer().next(new MkAnswer.Simple(HttpStatus.SC_INTERNAL_SERVER_ERROR)).start(port);
		String branchesUrl = "http://localhost:" + port + "/path/to/branches{/branch}";
		try {
			JsonObject repo = Json.createObjectBuilder().add("branches_url", branchesUrl).build();
			Logger logger = Mockito.mock(Logger.class);
			Mockito.doNothing().when(logger).info(Mockito.anyString());
			Mockito.doNothing().when(logger).warn(Mockito.anyString());
			Mockito.doThrow(new IllegalStateException("Unexpected IOException...")).when(logger).error(
			    Mockito.anyString(), Mockito.isA(IOException.class)
			);

			GhPagesBranchCheck gpc = new GhPagesBranchCheck(repo, logger);
			assertFalse(gpc.perform());
		} finally {
			server.stop();
		}
	}
	
	/**
	 * GhPagesBranchCheck.perform throws IOException because the http request cannot be fulfilled.
	 * This tests excepts an RuntimeException (we mock the logger in such a way) because it's the easies way
	 * to see that the flow entered in the catch IOException block.
	 * @throws Exception If something goes wrong.
	 */
	@Test (expected = RuntimeException.class)
	public void requestFetchFails() throws Exception {
	    int port = this.port();
		String branchesUrl = "http://localhost:" + port + "/path/to/branches{/branch}";
		JsonObject repo = Json.createObjectBuilder().add("branches_url", branchesUrl).build();
		Logger logger = Mockito.mock(Logger.class);
		Mockito.doNothing().when(logger).info(Mockito.anyString());
		Mockito.doNothing().when(logger).warn(Mockito.anyString());
		Mockito.doThrow(new RuntimeException("This is expected, everything is ok!")).when(logger).error(
		    Mockito.anyString(), Mockito.any(IOException.class)
	    );
		new GhPagesBranchCheck(repo, logger).perform();
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
