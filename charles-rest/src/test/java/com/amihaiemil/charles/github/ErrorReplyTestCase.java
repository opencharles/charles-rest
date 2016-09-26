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

import java.io.IOException;
import java.util.List;


import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repos.RepoCreate;
import com.jcabi.github.mock.MkGithub;

/**
 * Unit tests for {@link ErrorReply}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
public class ErrorReplyTestCase {

	/**
	 * {@link ErrorReply} can send the error message to a Github issue.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void sendsReply() throws Exception {
		LogsLocation logs = new LogsOnServer("www.example.com/rest/endpoint/", "test.log");
		Issue issue = this.mockIssue();
		ErrorReply er = new ErrorReply(logs.address(), issue);
		er.send();
		List<Comment> comments = Lists.newArrayList(issue.comments().iterate());
		assertTrue(comments.size() == 1);
		assertTrue(
			comments.get(0).json().getString("body").equals(
				"There was an error when processing your command. [Here](www.example.com/rest/endpoint/test.log) are the logs."
			)
		);
	}

	/**
     * Mock a Github issue.
     * @return The created Issue.
     * @throws IOException If something goes wrong.
     */
    public Issue mockIssue() throws IOException {
    	Github gh = new MkGithub("amihaiemil");
    	RepoCreate repoCreate = new RepoCreate("amihaiemil.github.io", false);
    	gh.repos().create(repoCreate);
    	return gh.repos().get(
    					  new Coordinates.Simple("amihaiemil", "amihaiemil.github.io")
    				  ).issues().create("Test issue for commands", "test body");
    }
	
}
