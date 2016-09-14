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

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos.RepoCreate;
import com.jcabi.github.mock.MkGithub;

/**
 * Unit tests for {@link GithubNotificationsCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class GithubAgentTestCase {

	/**
	 * GithubAgent can return the agent's username.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void getsAgentLogin() throws Exception {
		GithubAgent agent = new GithubAgent(
		    new MkGithub("amihaiemil")
		);
		assertTrue(agent.agentLogin().equals("amihaiemil"));
	}
	
	/**
	 * GithubAgent can fetch an issue it's mentioned in.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void getsIssueFromNotification() throws Exception {
		Github gh = new MkGithub("amihaiemil");
		Repo repo = gh.repos().create(
			new RepoCreate("reponame", false)
		);
		Issue issue = repo.issues().create("issue title", "issue body");
		JsonObject notification = this.mockNotification(
		    String.valueOf(issue.number()), 
		    "amihaiemil/reponame"
		);

		GithubAgent agent = Mockito.spy(new GithubAgent(gh));
		Mockito.when(agent.notifications()).thenReturn(
		    Arrays.asList(notification)
		);
		List<GithubIssue> ghIssues = agent.issuesMentionedIn();
		assertTrue(ghIssues.size() == 1);
		assertTrue(ghIssues.get(0).getSelf().number() == issue.number());
	}
	
	/**
	 * GithubAgent can fetch more issues that it's mentioned in.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void getsMoreIssuesFromNotifications() throws Exception {
		Github gh = new MkGithub("amihaiemil");
		Repo repo = gh.repos().create(
			new RepoCreate("reponame", false)
		);
		Issue issue1 = repo.issues().create("issue 1 title", "issue body");
		JsonObject notification1 = this.mockNotification(
		    String.valueOf(issue1.number()), 
		    "amihaiemil/reponame"
		);
		Issue issue2 = repo.issues().create("issue 2 title", "issue body");
		JsonObject notification2 = this.mockNotification(
		    String.valueOf(issue2.number()), 
		    "amihaiemil/reponame"
		);

		GithubAgent agent = Mockito.spy(new GithubAgent(gh));
		Mockito.when(agent.notifications()).thenReturn(
		    Arrays.asList(notification1, notification2)
		);
		List<GithubIssue> ghIssues = agent.issuesMentionedIn();
		assertTrue(ghIssues.size() == 2);
		assertTrue(ghIssues.get(0).getSelf().number() == issue1.number());
		assertTrue(ghIssues.get(1).getSelf().number() == issue2.number());
	}

	/**
	 * GithubAgent can fetch issues that it's mentioned in and 
	 * skips the ones that are closed (an issue could be closed 
	 * between the time the agent receives the notification and the time it
	 * check them, or the notification can simply be the result of closing the issue).
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void g() throws Exception {
		Github gh = new MkGithub("amihaiemil");
		Repo repo = gh.repos().create(
			new RepoCreate("reponame", false)
		);
		Issue issue1 = repo.issues().create("issue 1 title", "issue body");
		JsonObject notification1 = this.mockNotification(
		    String.valueOf(issue1.number()), 
		    "amihaiemil/reponame"
		);
		JsonObject notification2 = this.mockNotification(
		    String.valueOf(2), //notification for issue with number 2 which doesn't exist (it's closed)
		    "amihaiemil/reponame"
		);

		GithubAgent agent = Mockito.spy(new GithubAgent(gh));
		Mockito.when(agent.notifications()).thenReturn(
		    Arrays.asList(notification1, notification2)
		);
		List<GithubIssue> ghIssues = agent.issuesMentionedIn();
		assertTrue(ghIssues.size() == 1);
		assertTrue(ghIssues.get(0).getSelf().number() == issue1.number());
	}
	
	/**
	 * Mock a notification json object.
	 * @param issueNr Issue the notification is about.
	 * @param repoFullName Repository fullname.
	 * @return Json object representing a notification.
	 */
	private JsonObject mockNotification(String issueNr, String repoFullName) {
		JsonObject subject = Json.createObjectBuilder()
		    .add("url", "path/to/issue/" + issueNr)
		    .add("latest_comment_url", "latest/comment/path/1")
		    .build();
		JsonObject repository = Json.createObjectBuilder()
		    .add("full_name", repoFullName).build();
		JsonObject notification = Json.createObjectBuilder()
		    .add("reason", "mention")
		    .add("subject", subject)
		    .add("repository", repository)
		    .build();
		return notification;
	}
}
