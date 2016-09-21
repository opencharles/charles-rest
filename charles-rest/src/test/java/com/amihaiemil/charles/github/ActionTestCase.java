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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repos.RepoCreate;
import com.jcabi.github.mock.MkGithub;

/**
 * Unit tests for {@link action}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
public class ActionTestCase {
	
	/**
	 * More Actions are executed on separate threads.
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void actionsExecute() throws Exception {
		Language english = (Language)new English();
		GithubIssue issue1 = this.githubIssue("amihaiemil", "@charlesmike hello");
		GithubIssue issue2 = this.githubIssue("jeff", "@charlesmike hello");
		GithubIssue issue3 = this.githubIssue("vlad", "@charlesmike hi");
		GithubIssue issue4 = this.githubIssue("marius", "@charlesmike hello");
		Action ac1 = new Action(issue1, "charlesmike");
		Action ac2 = new Action(issue2, "charlesmike");
		Action ac3 = new Action(issue3, "charlesmike");
		Action ac4 = new Action(issue4, "charlesmike");
		
		final ExecutorService executorService = Executors.newFixedThreadPool(5);
		List<Future> futures = new ArrayList<Future>();
		futures.add(executorService.submit(ac1));
		futures.add(executorService.submit(ac2));
		futures.add(executorService.submit(ac3));
		futures.add(executorService.submit(ac4));

		for(Future f : futures) {
			assertTrue(f.get()==null);
		}
		
    	List<Comment> commentsWithReply1 = Lists.newArrayList(issue1.getSelf().comments().iterate());
    	List<Comment> commentsWithReply2 = Lists.newArrayList(issue2.getSelf().comments().iterate());
    	List<Comment> commentsWithReply3 = Lists.newArrayList(issue3.getSelf().comments().iterate());
    	List<Comment> commentsWithReply4 = Lists.newArrayList(issue4.getSelf().comments().iterate());
    	String expectedReply1 = "> @charlesmike hello\n\n" + String.format(english.response("hello.comment"),"amihaiemil");
    	assertTrue(commentsWithReply1.get(1).json().getString("body")
    			.equals(expectedReply1)); //there should be only 2 comments - the command and the reply.
    	
    	String expectedReply2 = "> @charlesmike hello\n\n" + String.format(english.response("hello.comment"),"jeff");
    	assertTrue(commentsWithReply2.get(1).json().getString("body")
    			.equals(expectedReply2)); //there should be only 2 comments - the command and the reply.
		
    	String expectedReply3 = "> @charlesmike hi\n\n" + String.format(english.response("hello.comment"),"vlad");
    	assertTrue(commentsWithReply3.get(1).json().getString("body")
    			.equals(expectedReply3)); //there should be only 2 comments - the command and the reply.
		
    	String expectedReply4 = "> @charlesmike hello\n\n" + String.format(english.response("hello.comment"),"marius");
    	assertTrue(commentsWithReply4.get(1).json().getString("body")
    			.equals(expectedReply4)); //there should be only 2 comments - the command and the reply.
		
	}
	/**
	 * Creates a GithubIssue with the given command.
	 * @param command command.
	 * @return GithubIssue
	 */
	public GithubIssue githubIssue(String commander, String command) throws Exception {
		Github gh = new MkGithub(commander);
    	RepoCreate repoCreate = new RepoCreate(commander + ".github.io", false);
    	gh.repos().create(repoCreate);
    	Issue issue = gh.repos().get(
    					  new Coordinates.Simple(commander, commander + ".github.io")
    				  ).issues().create("Test issue for commands", "test body");
    	Comment com = issue.comments().post(command);
    	
    	GithubIssue gissue = null;//new GithubIssue(commander + ".github.io", issue.number(), com.number(), issue);
    	return gissue;
	}

}
