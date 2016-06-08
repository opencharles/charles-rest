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
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos.RepoCreate;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;

/**
 * Unit tests for {@link LastComment}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 *
 */
public class LastCommentTestCase {
	/**
	 * The latest comment in the issue mentions the agent.
	 * @throws Exception if something goes wrong.
	 */
	@Test
    public void latestCommentMentionsTheAgent() throws Exception {
    	Issue issue = this.mockIssue();
    	Comment com = issue.comments().post("@charlesmike hello there, how are you?");
    	GithubIssue gissue = new GithubIssue(
    			"amihaiemil.github.io",
    			issue.number(),
    			com.number(), 
    			issue
    	);
    	LastComment lastComment = new LastComment(gissue, "charlesmike");
    	JsonObject jsonComment = lastComment.json();
    	assertTrue(com.json().equals(jsonComment));
    }
	
	/**
	 * The agent is not mentioned in the latest comment but in a previous one.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void agentMentionedInOtherComment() throws Exception {
    	Issue issue = this.mockIssue();
    	Comment agentcom = issue.comments().post("@charlesmike hello there, how are you?");
    	Comment latest = issue.comments().post("@someoneelse, please check that...");

    	GithubIssue gissue = new GithubIssue(
    			"amihaiemil.github.io",
    			issue.number(),
    			latest.number(), 
    			issue
    	);
    	LastComment lastComment = new LastComment(gissue, "charlesmike");
    	JsonObject jsonComment = lastComment.json();
    	assertTrue(agentcom.json().equals(jsonComment));
	}
	
	/**
	 * The agent is mentioned in more previous comments and it should respond only to the last mention.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void mentionedInMoreComments() throws Exception {
    	Issue issue = this.mockIssue();
    	issue.comments().post("@charlesmike hello there, how are you?");
    	issue.comments().post("@charlesmike hello? Please answer?");
    	Comment agentCom = issue.comments().post("@charlesmike why won't you answer?");

    	issue.comments().post("@someoneelse, please do something...");
    	Comment latest = issue.comments().post("@someoneelse, please check that...");

    	GithubIssue gissue = new GithubIssue(
    			"amihaiemil.github.io",
    			issue.number(),
    			latest.number(), 
    			issue
    	);
    	LastComment lastComment = new LastComment(gissue, "charlesmike");
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
    	Issue issue = this.mockIssue();
    	issue.comments().post("@someoneelse hello there, how are you?");
    	Comment latest = issue.comments().post("@someoneelse, please check that...");

    	GithubIssue gissue = new GithubIssue(
    			"amihaiemil.github.io",
    			issue.number(),
    			latest.number(), 
    			issue
    	);
    	LastComment lastComment = new LastComment(gissue, "charlesmike");
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

    	Comment latest = issue.comments().post("@someoneelse, please check that...");

        GithubIssue gissue = new GithubIssue(
    		"amihaiemil.github.io",
    		issue.number(),
    		latest.number(), 
    		issue
    	);
        
    	LastComment lastComment = new LastComment(gissue, "charlesmike");
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
    	Comment latest = issue.comments().post("@someoneelse, please check that..."); //some other comment that is the last on the ticket.

        GithubIssue gissue = new GithubIssue(
    		"amihaiemil.github.io",
    		issue.number(),
    		latest.number(), 
    		issue
    	);
        
    	LastComment lastComment = new LastComment(gissue, "charlesmike");
    	JsonObject jsonComment = lastComment.json();
    	assertTrue(lastMention.json().equals(jsonComment)); 
	}
	
    /**
     * Mock an issue on Github.
     * @return The created MkIssue.
     * @throws IOException If something goes wrong.
     */
    public Issue mockIssue() throws IOException {
    	Github gh = new MkGithub("amihaiemil");
    	RepoCreate repoCreate = new RepoCreate("amihaiemil.github.io", false);
    	gh.repos().create(repoCreate);
    	return gh.repos().get(new Coordinates.Simple("amihaiemil", "amihaiemil.github.io")).issues().create("Test issue for commands", "test body");
    }
}
