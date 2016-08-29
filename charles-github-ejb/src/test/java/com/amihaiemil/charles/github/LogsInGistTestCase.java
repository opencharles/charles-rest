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

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.jcabi.github.Gists;
import com.jcabi.github.Github;
import com.jcabi.github.mock.MkGithub;

/**
 * Test cases for {@link LogsInGist}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class LogsInGistTestCase {

    /**
     * LogsInGist can write a log file to a secret gist and return url to that gist.
     * @throws Exception If something goes wrong.
     */
    @Test
	public void writesLogsToGist() throws Exception {
    	String testLogPath = "src/test/resources/logsfortest.log";
    	Gists gists = this.mockGistsApi();
        LogsLocation logs = new LogsInGist(testLogPath, gists);
        assertTrue(logs.address().equals("https://gist.github.com/1"));
        String gistContent = gists.get("1").read("logsfortest.log");
        assertTrue(gistContent.contains("Started action 6bb41980-4c8e-4719-9848-9972982a24f0"));
        assertTrue(gistContent.contains("Finished action 6bb41980-4c8e-4719-9848-9972982a24f0"));
    }

    /**
     * Mocks the Gists api.
     * @return a mock of the Github Gists api.
     * @throws Exception If something goes wrong.
     */
    public Gists mockGistsApi() throws Exception {
        Github gh = new MkGithub("amihaiemil");
        return gh.gists();
    }
}
