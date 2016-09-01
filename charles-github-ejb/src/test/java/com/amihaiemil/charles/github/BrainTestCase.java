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
import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repos.RepoCreate;
import com.jcabi.github.mock.MkGithub;

/**
 * Unit tests for {@link Brain}.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
public class BrainTestCase {

	/**
	 * {@link Brain} can undestand a command.
	 * @throws Exception if something goes wrong.
	 */
	@Test
	public void understandsCommand() throws Exception {
		Command com = this.mockCommand();
		
		Language english = Mockito.mock(English.class);
		Mockito.when(english.response("step.failure.comment")).thenReturn("failure on step");
		Mockito.when(english.response("hello.comment")).thenReturn("hi there, %s");
		Mockito.when(english.categorize(com)
		).thenReturn(new CommandCategory("hello", english));
		
		Brain br = new Brain(Mockito.mock(Logger.class), Mockito.mock(LogsLocation.class), Arrays.asList(english));
		Steps steps = br.understand(com);
		assertTrue(steps != null);
	}
	
	/**
     * Mock a Github command where the agent is mentioned.
     * @return The created MkIssue.
     * @throws IOException If something goes wrong.
     */
    public Command mockCommand() throws IOException {
    	Github gh = new MkGithub("amihaiemil");
    	RepoCreate repoCreate = new RepoCreate("amihaiemil.github.io", false);
    	gh.repos().create(repoCreate);
    	Issue issue = gh.repos().get(
    					  new Coordinates.Simple("amihaiemil", "amihaiemil.github.io")
    				  ).issues().create("Test issue for commands", "test body");
    	Comment c = issue.comments().post("@charlesmike hello there!");
    	
    	Command com = Mockito.mock(Command.class);
    
     	Mockito.when(com.json()).thenReturn(c.json());
     	Mockito.when(com.issue()).thenReturn(issue);
     	
     	return com;
    }
}
