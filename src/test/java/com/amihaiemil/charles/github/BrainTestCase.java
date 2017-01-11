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

import static org.junit.Assert.assertTrue;
import java.io.IOException;
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
    public void understandsHelloCommand() throws Exception {
        Command com = this.mockCommand();
        
        Language english = Mockito.mock(English.class);
        Mockito.when(english.response("step.failure.comment")).thenReturn("failure on step");
        Mockito.when(english.response("hello.comment")).thenReturn("hi there");
        Mockito.when(english.categorize(com)
        ).thenReturn(new CommandCategory("hello", english));
        
        Brain br = new Brain(Mockito.mock(Logger.class), Mockito.mock(LogsLocation.class), english);
        Steps steps = br.understand(com);
        assertTrue(steps != null);
        assertTrue(steps.getStepsToPerform() instanceof SendReply);
    }
    
    /**
     * {@link Brain} can undestand an index site command.
     * @throws Exception if something goes wrong.
     */
    @Test
    public void understandsIndexSiteCommand() throws Exception {
        Command com = this.mockCommand();
        
        Language english = Mockito.mock(English.class);
        Mockito.when(english.response("step.failure.comment")).thenReturn("failure on step");
        Mockito.when(english.response("index.start.comment")).thenReturn("index start!");
        Mockito.when(english.response("index.finished.comment")).thenReturn("index finished!");
        Mockito.when(english.response("denied.fork.comment")).thenReturn("repo is a fork!");
        Mockito.when(english.response("denied.commander.comment")).thenReturn("denied commander!");
        Mockito.when(english.response("denied.name.comment")).thenReturn("bad repo!!");
        Mockito.when(english.categorize(com)
        ).thenReturn(new CommandCategory("indexsite", english));
        
        Brain br = new Brain(Mockito.mock(Logger.class), Mockito.mock(LogsLocation.class), english);
        
        Steps steps = br.understand(com);
        assertTrue(steps != null);
        assertTrue(steps.getStepsToPerform() instanceof PreconditionCheckStep);
    }
    
    /**
     * {@link Brain} can undestand an index page command.
     * @throws Exception if something goes wrong.
     */
    @Test
    public void understandsIndexPageCommand() throws Exception {
        Command com = this.mockCommand();
        
        Language english = Mockito.mock(English.class);
        Mockito.when(english.response("step.failure.comment")).thenReturn("failure on step");
        Mockito.when(english.response("index.start.comment")).thenReturn("index start!");
        Mockito.when(english.response("index.finished.comment")).thenReturn("index finished!");
        Mockito.when(english.response("denied.badlink.comment")).thenReturn("bad link!");
        Mockito.when(english.response("denied.fork.comment")).thenReturn("repo is a fork!");
        Mockito.when(english.response("denied.commander.comment")).thenReturn("denied commander!");
        Mockito.when(english.response("denied.name.comment")).thenReturn("bad repo!!");
        
        Mockito.when(english.categorize(com)
        ).thenReturn(new CommandCategory("indexpage", english));
        
        Brain br = new Brain(Mockito.mock(Logger.class), Mockito.mock(LogsLocation.class), english);
        Steps steps = br.understand(com);
        assertTrue(steps != null);
        assertTrue(steps.getStepsToPerform() instanceof PageHostedOnGithubCheck);
    }
    
    /**
     * {@link Brain} can undestand a deleteindex command.
     * @throws Exception if something goes wrong.
     */
    @Test
    public void understandsDeleteIndexCommand() throws Exception {
        Command com = this.mockCommand();
        
        Language english = Mockito.mock(English.class);
        Mockito.when(english.response("step.failure.comment")).thenReturn("failure on step");
        Mockito.when(english.response("index.start.comment")).thenReturn("index start!");
        Mockito.when(english.response("index.finished.comment")).thenReturn("index finished!");
        Mockito.when(english.response("denied.badlink.comment")).thenReturn("bad link!");
        Mockito.when(english.response("denied.fork.comment")).thenReturn("repo is a fork!");
        Mockito.when(english.response("denied.commander.comment")).thenReturn("denied commander!");
        Mockito.when(english.response("denied.name.comment")).thenReturn("bad repo!!");
        Mockito.when(english.response("denied.deleteindex.comment")).thenReturn("delete denied!");
        Mockito.when(english.response("deleteindex.finished.comment")).thenReturn("index deleted!");
        Mockito.when(english.response("index.missing.comment")).thenReturn("index missing!");
        Mockito.when(english.categorize(com)
        ).thenReturn(new CommandCategory("deleteindex", english));
        
        Brain br = new Brain(Mockito.mock(Logger.class), Mockito.mock(LogsLocation.class), english);
        Steps steps = br.understand(com);
        assertTrue(steps != null);
        assertTrue(steps.getStepsToPerform() instanceof DeleteIndexCommandCheck);
    }
    
    /**
     * {@link Brain} can see an unknown command.
     * @throws Exception if something goes wrong.
     */
    @Test
    public void uknownCommand() throws Exception {
        Command com = this.mockCommand();
        
        Language english = Mockito.mock(English.class);
        Mockito.when(english.response("step.failure.comment")).thenReturn("failure on step");
        Mockito.when(english.response("unknown.comment")).thenReturn("Unknown command!");
        Mockito.when(english.categorize(com)
        ).thenReturn(new CommandCategory("uknown", english));
        
        Brain br = new Brain(Mockito.mock(Logger.class), Mockito.mock(LogsLocation.class), english);
        Steps steps = br.understand(com);
        assertTrue(steps != null);
        assertTrue(steps.getStepsToPerform() instanceof SendReply);
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
        Comment c = issue.comments().post("@charlesmike mock command for you (http://amihaiemil.github.io/folder/page.html)!");
        
        Command com = Mockito.mock(Command.class);
    
         Mockito.when(com.json()).thenReturn(c.json());
         Mockito.when(com.issue()).thenReturn(issue);
         
         CommandedRepo crepo = Mockito.mock(CommandedRepo.class);
        Mockito.when(crepo.json()).thenReturn(issue.repo().json());
         
         Mockito.when(com.repo()).thenReturn(crepo);

         return com;
    }
}
