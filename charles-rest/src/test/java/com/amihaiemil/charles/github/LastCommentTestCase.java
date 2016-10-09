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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.mockito.Mockito;

import com.jcabi.github.Comment;
import com.jcabi.github.Comments;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos.RepoCreate;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.ApacheRequest;

/**
 * Unit tests for {@link LastComment}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
public class LastCommentTestCase {
	/**
	 * The latest comment in the issue mentions the agent.
	 * @throws Exception if something goes wrong.
	 */
	@Test
    public void latestCommentMentionsTheAgent() throws Exception {
    	Issue[] issues = this.mockIssue();
    	Comment com = issues[0].comments().post("@charlesmike hello there, how are you?");
    	LastComment lastComment = new LastComment(issues[1]);
    	JsonObject jsonComment = lastComment.json();
    	assertTrue(com.json().equals(jsonComment));
    }
	
	/**
	 * Command can fetch the agent's login.
	 * @throws Exception if something goes wrong.
	 */
	@Test
    public void getsAgentLogin() throws Exception {
    	Issue[] issues = this.mockIssue();
    	issues[0].comments().post("@charlesmike hello there, how are you?");
    	LastComment lastComment = new LastComment(issues[1]);
    	assertTrue(lastComment.agentLogin().equals("charlesmike"));
    }
	
	/**
	 * Command can fetch the author's login.
	 * @throws Exception if something goes wrong.
	 */
	@Test
    public void getsAuthorLogin() throws Exception {
    	Issue[] issues = this.mockIssue();
    	issues[0].comments().post("@charlesmike hello there, how are you?");
    	LastComment lastComment = new LastComment(issues[1]);
    	assertTrue(lastComment.authorLogin().equals("amihaiemil"));
    }
	
	/**
	 * Command can fetch the author's email.
	 * @throws Exception if something goes wrong.
	 */
	@Test
    public void getsAuthorEmail() throws Exception {
        Issue[] issues = this.mockIssue();
        issues[0].comments().post("@charlesmike hello there, how are you?");
        LastComment lastComment = new LastComment(issues[1]);
        assertTrue(lastComment.authorEmail().equals("amihaiemil@gmail.com"));
    }
	
	/**
	 * The agent is not mentioned in the latest comment but in a previous one.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void agentMentionedInOtherComment() throws Exception {
    	Issue[] issues = this.mockIssue();
    	Issue commander = issues[0];
    	
    	Comment agentcom = commander.comments().post("@charlesmike hello there, how are you?");
    	commander.comments().post("@someoneelse, please check that...");

    	LastComment lastComment = new LastComment(issues[1]);
    	JsonObject jsonComment = lastComment.json();
    	assertTrue(agentcom.json().equals(jsonComment));
	}
	
	/**
	 * The agent is mentioned in more previous comments and it should respond only to the last mention.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void mentionedInMoreComments() throws Exception {
    	Issue[] issues = this.mockIssue();
    	Issue commander = issues[0];
    	
    	commander.comments().post("@charlesmike hello there, how are you?");
    	commander.comments().post("@charlesmike hello? Please answer?");
    	Comment agentCom = commander.comments().post("@charlesmike why won't you answer?");

    	commander.comments().post("@someoneelse, please do something...");
    	commander.comments().post("@someoneelse, please check that...");

    	LastComment lastComment = new LastComment(issues[1]);
    	JsonObject jsonComment = lastComment.json();
    	assertTrue(agentCom.json().equals(jsonComment));
	}
	
	/**
	 * If the agent is not mentioned at all in the issue (should happen rearely, only if the a mentioning comment was removed before it
	 * checked the notifications) then LastComment should an "empty" one.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void agentNotMentionedAtAll() throws Exception {
    	Issue[] issues = this.mockIssue();
    	Issue commander = issues[0];
    	commander.comments().post("@someoneelse hello there, how are you?");
    	commander.comments().post("@someoneelse, please check that...");

    	LastComment lastComment = new LastComment(issues[1]);
    	JsonObject jsonComment = lastComment.json();
    	JsonObject emptyMentionComment = Json.createObjectBuilder().add("id", "-1").add("body", "").build();
    	assertTrue(emptyMentionComment.equals(jsonComment));
	}

	/**
	 * Agent already replied once to the last comment.
	 * @throws Exception if something goes wrong.
	 */
	@Test
	public void agentRepliedAlreadyToTheLastComment() throws Exception {
		final MkStorage storage = new MkStorage.Synced(new MkStorage.InFile());
        final Repo repoMihai = new MkGithub(storage, "amihaiemil").repos().create( new RepoCreate("amihaiemil.github.io", false));
        final Issue issue = repoMihai.issues().create("test issue", "body");
        issue.comments().post("@charlesmike hello!");
        
        final Github charlesmike = new MkGithub(storage, "charlesmike");
        Issue issueCharlesmike = charlesmike.repos().get(repoMihai.coordinates()).issues().get(issue.number());
        issueCharlesmike.comments().post("@amihaiemil hi there, I can help you index... ");

    	issue.comments().post("@someoneelse, please check that...");
        
    	LastComment lastComment = new LastComment(issueCharlesmike);
    	JsonObject jsonComment = lastComment.json();
    	JsonObject emptyMentionComment = Json.createObjectBuilder().add("id", "-1").add("body", "").build();
    	assertTrue(emptyMentionComment.equals(jsonComment)); 
	}
	
	/**
	 * There is more than 1 mention of the agent in the issue and it has already 
	 * replied to others, but the last one is not replied to yet.
	 * @throws Exception if something goes wrong.
	 */
	@Test
	public void agentRepliedToPreviousMention() throws Exception {
		final MkStorage storage = new MkStorage.Synced(new MkStorage.InFile());
        final Repo repoMihai = new MkGithub(storage, "amihaiemil").repos().create( new RepoCreate("amihaiemil.github.io", false));
        final Issue issue = repoMihai.issues().create("test issue", "body");
        issue.comments().post("@charlesmike hello!");//first mention
        
        final Github charlesmike = new MkGithub(storage, "charlesmike");
        Issue issueCharlesmike = charlesmike.repos().get(repoMihai.coordinates()).issues().get(issue.number());
        issueCharlesmike.comments().post("@amihaiemil hi there, I can help you index... "); //first reply

        Comment lastMention = issue.comments().post("@charlesmike hello again!!");//second mention
    	issue.comments().post("@someoneelse, please check that..."); //some other comment that is the last on the ticket.
        
    	LastComment lastComment = new LastComment(issueCharlesmike);
    	JsonObject jsonComment = lastComment.json();
    	assertTrue(lastMention.json().equals(jsonComment)); 
	}
	
    /**
     * Mock an issue on Github.
     * @return 2 Issues: 1 from the commander's Github (where the comments
     * are posted) and 1 from the agent's Github (where comments are checked)
     * @throws IOException If something goes wrong.
     */
    private Issue[] mockIssue() throws IOException {
    	MkStorage storage = new MkStorage.InFile();
    	Github commanderGithub = new MkGithub(storage, "amihaiemil");
    	commanderGithub.users().self().emails().add(Arrays.asList("amihaiemil@gmail.com"));
    	Github agentGithub = new MkGithub(storage, "charlesmike");
    	
    	RepoCreate repoCreate = new RepoCreate("amihaiemil.github.io", false);
    	commanderGithub.repos().create(repoCreate);
    	Issue[] issues = new Issue[2];
    	Coordinates repoCoordinates = new Coordinates.Simple("amihaiemil", "amihaiemil.github.io");
    	Issue authorsIssue = commanderGithub.repos().get(repoCoordinates).issues().create("Test issue for commands", "test body");
    	Issue agentsIssue = agentGithub.repos().get(repoCoordinates).issues().get(authorsIssue.number());
    	issues[0] = authorsIssue;
    	issues[1] = agentsIssue;
    	
    	return issues;
    }

	/**
	 * Command can fetch the author's organization membership.
	 * @throws Exception if something goes wrong.
	 */
	@Test
    public void getsOrganizationMembership() throws Exception {
		int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
		    .next(
		        new MkAnswer.Simple(
		            Json.createObjectBuilder()
			            .add("state", "snowflake")
			            .add("role", "special_test_admin")
			            .build().toString()
		        )
		    ).start(port);
		try {
		    Github gh = Mockito.mock(Github.class);
            Mockito.when(gh.entry()).thenReturn(
                new ApacheRequest("http://localhost:" + port + "/")		
            );
		    Repo repo = Mockito.mock(Repo.class);
            Mockito.when(repo.json()).thenReturn(
                Json.createObjectBuilder().add(
                    "owner",
                    Json.createObjectBuilder().add(
                        "type", "organization"		
                    ).add(
                        "login", "someorganization"
                    ).build()
                ).build()
            );
            Mockito.when(repo.github()).thenReturn(gh);
		    
            Comments comments = Mockito.mock(Comments.class);
            Mockito.when(comments.iterate()).thenReturn(new ArrayList<Comment>());
            
            Issue issue = Mockito.mock(Issue.class);
		    Mockito.when(issue.repo()).thenReturn(repo);
		    Mockito.when(issue.comments()).thenReturn(comments);

            LastComment lastComment = new LastComment(issue);
            lastComment.comment(
                Json.createObjectBuilder().add(
                    "user",
                    Json.createObjectBuilder()
                        .add("login", "amihaiemil")
                        .build()
                ).build()
            );
            JsonObject mem = lastComment.authorOrgMembership();
            assertTrue(mem.getString("state").equals("snowflake"));
            assertTrue(mem.getString("role").equals("special_test_admin"));
		} finally {
			server.stop();
		}
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
