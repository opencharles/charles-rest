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
import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repos.RepoCreate;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;

/**
 * Test cases related to the index-page action.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class IndexPageaActionTestCase {

    /**
     * All the precondition checks for an index-page command
     * finish successfully when we have a blog-repo (a repo which
     * has the name owner.github.io)
     * @throws Exception If something goes wrong.
     */
    @Test
    public void indexPageChecksAreSuccessfulOnBlogRepo() throws Exception {
        Command com = this.mockCommand(
            "amihaiemil", "amihaiemil", "amihaiemil.github.io",
            false, false, "http://amihaiemil.github.io/page/to/index"
        );
        Language eng = this.mockEnglish(com);
        Brain br = new Brain(
            Mockito.mock(Logger.class),
            Mockito.mock(LogsLocation.class),
            Arrays.asList(eng)
        );
        Brain spiedBrain = Mockito.spy(br);
        Mockito.doReturn(//replace the index step with a simple comment; we're just interested in the checks here, not the index step itself
            new SendReply(
                 new TextReply(com, "index-page checks passed!"),
                 Mockito.mock(Logger.class),
                 new Step.FinalStep(Mockito.mock(Logger.class))
             )
        ).when(spiedBrain).indexPageStep(com, eng);
        
        Step steps = spiedBrain.understand(com);
        steps.perform();
        
        List<Comment> comments = Lists.newArrayList(com.issue().comments().iterate());
        assertTrue(comments.size() == 2);
        assertTrue(
            comments.get(0).json().getString("body").startsWith(
                "@charlesmike index [this]("
            )
        );
        assertTrue(
            comments.get(1).json().getString("body").endsWith(
                "\n\nindex-page checks passed!"
            )
        );
    }

    /**
     * All the precondition checks for an index-page command
     * finish successfully when we have a repo with gh-pages website.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void indexPageChecksAreSuccessfulOnGhPagesRepo() throws Exception {
        Command com = this.mockCommand(
            "amihaiemil", "amihaiemil", "repowithghpages",
            false, true, "https://amihaiemil.github.io/repowithghpages/page/to/index"
        );
        Language eng = this.mockEnglish(com);
        Brain br = new Brain(
            Mockito.mock(Logger.class),
            Mockito.mock(LogsLocation.class),
            Arrays.asList(eng)
        );
        Brain spiedBrain = Mockito.spy(br);
        Mockito.doReturn(//replace the index step with a simple comment; we're just interested in the checks here, not the index step itself
            new SendReply(
                 new TextReply(com, "index-page checks passed!"),
                 Mockito.mock(Logger.class),
                 new Step.FinalStep(Mockito.mock(Logger.class))
             )
        ).when(spiedBrain).indexPageStep(com, eng);
        
        Step steps = spiedBrain.understand(com);
        steps.perform();
        
        List<Comment> comments = Lists.newArrayList(com.issue().comments().iterate());
        assertTrue(comments.size() == 2);
        assertTrue(
                comments.get(0).json().getString("body").startsWith(
                    "@charlesmike index [this]("
                )
        );
        assertTrue(
            comments.get(1).json().getString("body").endsWith(
                "\n\nindex-page checks passed!"
            )
        );
    }
    
    /**
     * All the precondition checks for an index-page command
     * finish successfully when we have a repo with gh-pages website, which is
     * owned by an organization. In this case, the command author has to be an active admin
     * of the organization.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void indexPageChecksAreSuccessfulOnGhPagesRepoUnderOrg() throws Exception {
        Command com = this.mockCommand(
            "amihaiemil", "teamed", "repowithghpages",
            false, true, "https://teamed.github.io/repowithghpages/page/to/index"
        );
        Language eng = this.mockEnglish(com);
        Brain br = new Brain(
            Mockito.mock(Logger.class),
            Mockito.mock(LogsLocation.class),
            Arrays.asList(eng)
        );
        Brain spiedBrain = Mockito.spy(br);
        Mockito.doReturn(//replace the index step with a simple comment; we're just interested in the checks here, not the index step itself
            new SendReply(
                 new TextReply(com, "index-page checks passed!"),
                 Mockito.mock(Logger.class),
                 new Step.FinalStep(Mockito.mock(Logger.class))
             )
        ).when(spiedBrain).indexPageStep(com, eng);
        
        Mockito.when(com.authorOrgMembership()).thenReturn(
            Json.createObjectBuilder()
                .add("state", "active")
                .add("role", "admin")
                .build()
        );
        
        
        Step steps = spiedBrain.understand(com);
        steps.perform();
        
        List<Comment> comments = Lists.newArrayList(com.issue().comments().iterate());
        assertTrue(comments.size() == 2);
        assertTrue(
                comments.get(0).json().getString("body").startsWith(
                    "@charlesmike index [this]("
                )
        );
        System.out.println( comments.get(1).json().getString("body"));
        assertTrue(
            comments.get(1).json().getString("body").endsWith(
                "\n\nindex-page checks passed!"
            )
        );
    }

    /**
     * All the precondition checks for an index-page command
     * finish successfully when we have a blog repo, which is
     * owned by an organization. In this case, the command author has to be an active admin
     * of the organization.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void indexPageChecksAreSuccessfulOnBlogRepoUnderOrg() throws Exception {
        Command com = this.mockCommand(
            "amihaiemil", "teamed", "teamed.github.io",
            false, false, "http://teamed.github.io/somepath/to/index"
        );
        Language eng = this.mockEnglish(com);
        Brain br = new Brain(
            Mockito.mock(Logger.class),
            Mockito.mock(LogsLocation.class),
            Arrays.asList(eng)
        );
        Brain spiedBrain = Mockito.spy(br);
        Mockito.doReturn(//replace the index step with a simple comment; we're just interested in the checks here, not the index step itself
            new SendReply(
                 new TextReply(com, "index-page checks passed!"),
                 Mockito.mock(Logger.class),
                 new Step.FinalStep(Mockito.mock(Logger.class))
             )
        ).when(spiedBrain).indexPageStep(com, eng);
        
        Mockito.when(com.authorOrgMembership()).thenReturn(
            Json.createObjectBuilder()
                .add("state", "active")
                .add("role", "admin")
                .build()
        );
        
        
        Step steps = spiedBrain.understand(com);
        steps.perform();
        
        List<Comment> comments = Lists.newArrayList(com.issue().comments().iterate());
        assertTrue(comments.size() == 2);
        assertTrue(
                comments.get(0).json().getString("body").startsWith(
                    "@charlesmike index [this]("
                )
        );
        assertTrue(
            comments.get(1).json().getString("body").endsWith(
                "\n\nindex-page checks passed!"
            )
        );
    }
    
    /**
     * Preconditions' check for index-page command fail because the author
     * is not repo owner.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void indexPageChecksFailAuthorNotOwner() throws Exception {
        Command com = this.mockCommand(
            "notowner", "amihaiemil", "amihaiemil.github.io",
            false, false, "http://amihaiemil.github.io/page/to/index"
        );
        Language eng = this.mockEnglish(com);
        Brain br = new Brain(
            Mockito.mock(Logger.class),
            Mockito.mock(LogsLocation.class),
            Arrays.asList(eng)
        );
        Brain spiedBrain = Mockito.spy(br);
        IndexPage index = Mockito.mock(IndexPage.class);
        Mockito.doThrow(new IllegalStateException("Should not have reached here!")).when(index).perform();
        Mockito.doReturn(index).when(spiedBrain).indexPageStep(com, eng);

        Mockito.when(com.authorOrgMembership()).thenReturn(
            Json.createObjectBuilder().build()
        );
        
        Step steps = spiedBrain.understand(com);
        steps.perform();
        
        List<Comment> comments = Lists.newArrayList(com.issue().comments().iterate());
        assertTrue(comments.size() == 2);
        assertTrue(
                comments.get(0).json().getString("body").startsWith(
                    "@charlesmike index [this]("
                )
        );
        assertTrue(
            comments.get(1).json().getString("body").endsWith(
                "\n\ndenied because author is not repo owner!"
            )
        );
    }
    
    /**
     * Preconditions' check for index-page command fail: the author is owner,
     * the repo has a gh-pages branch BUT it is a fork.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void indexPageChecksFailRepoIsForked() throws Exception {
        Command com = this.mockCommand(
            "amihaiemil", "amihaiemil", "forkedrepo",
            true, true, "http://amihaiemil.github.io/forkedrepo/page/to/index"
        );
        Language eng = this.mockEnglish(com);
        Brain br = new Brain(
            Mockito.mock(Logger.class),
            Mockito.mock(LogsLocation.class),
            Arrays.asList(eng)
        );
        Brain spiedBrain = Mockito.spy(br);
        IndexPage index = Mockito.mock(IndexPage.class);
        Mockito.doThrow(new IllegalStateException("Should not have reached here!")).when(index).perform();
        Mockito.doReturn(index).when(spiedBrain).indexPageStep(com, eng);
        
        Step steps = spiedBrain.understand(com);
        steps.perform();
        
        List<Comment> comments = Lists.newArrayList(com.issue().comments().iterate());
        assertTrue(comments.size() == 2);
        assertTrue(
                comments.get(0).json().getString("body").startsWith(
                    "@charlesmike index [this]("
                )
        );
        System.out.println(comments.get(1).json().getString("body"));
        assertTrue(
            comments.get(1).json().getString("body").endsWith(
                "\n\nrepo is a fork!"
            )
        );
    }

    /**
     * Preconditions' check for index-page command fail. The author is owner,
     * but it has no website in it: is not named owner.github.io and has no
     * gh-pages branch.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void indexPageChecksFailNoWebsite() throws Exception {
        Command com = this.mockCommand(
            "amihaiemil", "amihaiemil", "someRepoWithNoWebsite",
            false, false, "http://amihaiemil.github.io/page/to/index"
        );
        Language eng = this.mockEnglish(com);
        Brain br = new Brain(
            Mockito.mock(Logger.class),
            Mockito.mock(LogsLocation.class),
            Arrays.asList(eng)
        );
        Brain spiedBrain = Mockito.spy(br);
        IndexPage index = Mockito.mock(IndexPage.class);
        Mockito.doThrow(new IllegalStateException("Should not have reached here!")).when(index).perform();
        Mockito.doReturn(index).when(spiedBrain).indexPageStep(com, eng);
        
        Step steps = spiedBrain.understand(com);
        steps.perform();
        
        List<Comment> comments = Lists.newArrayList(com.issue().comments().iterate());
        assertTrue(comments.size() == 2);
        assertTrue(
                comments.get(0).json().getString("body").startsWith(
                    "@charlesmike index [this]("
                )
        );
        System.out.println(comments.get(1).json().getString("body"));
        assertTrue(
            comments.get(1).json().getString("body").endsWith(
                "\n\nThis repo doesn't host any website!"
            )
        );
    }
    
    /**
     * Preconditions' check for index-page command fail. The author is owner,
     * repo is not a fork and has a website, but the given page is not part of it!
     * gh-pages branch.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void indexPageChecksFailPageNotInTheRepo() throws Exception {
        Command com = this.mockCommand(
            "amihaiemil", "amihaiemil", "charles",
            false, true, "http://www.othersite.test/page/to/index"
        );
        Language eng = this.mockEnglish(com);
        Brain br = new Brain(
            Mockito.mock(Logger.class),
            Mockito.mock(LogsLocation.class),
            Arrays.asList(eng)
        );
        Brain spiedBrain = Mockito.spy(br);
        IndexPage index = Mockito.mock(IndexPage.class);
        Mockito.doThrow(new IllegalStateException("Should not have reached here!")).when(index).perform();
        Mockito.doReturn(index).when(spiedBrain).indexPageStep(com, eng);
        
        Step steps = spiedBrain.understand(com);
        steps.perform();
        
        List<Comment> comments = Lists.newArrayList(com.issue().comments().iterate());
        assertTrue(comments.size() == 2);
        assertTrue(
                comments.get(0).json().getString("body").startsWith(
                    "@charlesmike index [this]("
                )
        );
        assertTrue(
            comments.get(1).json().getString("body").endsWith(
                "\n\nThe page is not from this repo!"
            )
        );
    }
    
    /**
     * Mock a Github command where the agent is mentioned.
     * @param commander - commander's login
     * @param owner - owner's login
     * @param repoName - repo name
     * @param fork - is the repo a fork or not?
     * @param ghpages - does the repo have a gh-pages branch or not?
     * @return The created Command.
     * @throws IOException If something goes wrong.
     */
    private Command mockCommand(
        String commander, String owner,
        String repoName, boolean fork,
        boolean ghpages, String link
    ) throws IOException {
        MkStorage storage = new MkStorage.InFile();
        Github gh = new MkGithub(storage, owner);
        RepoCreate repoCreate = new RepoCreate(repoName, false);
        gh.repos().create(repoCreate);
        Coordinates repoCoord = new Coordinates.Simple(owner, repoName);
        
        Issue issue = gh.repos().get(repoCoord).issues().create("Test issue for commands", "test body");
        Comment c = issue.comments().post("@charlesmike index [this](" + link + ") page pls");      
        Issue agentIssue = new MkGithub(storage, "charlesmike")
            .repos().get(repoCoord).issues().get(issue.number());
  
  
        Command com = Mockito.mock(Command.class);
        Mockito.when(com.json()).thenReturn(c.json());
        Mockito.when(com.issue()).thenReturn(agentIssue);
        Mockito.when(com.authorLogin()).thenReturn(commander);

         //we build our own json repo since the one returned by MkGithub doesn't map 1:1 with the expected and so the tests fail.
         JsonObject repo = Json.createObjectBuilder()
             .add("name", repoName)
             .add("owner", Json.createObjectBuilder().add("login", owner).build())
             .add("fork", fork)
             .build();
             
         
        CommandedRepo crepo = Mockito.mock(CommandedRepo.class);
        Mockito.when(crepo.json()).thenReturn(repo);
        Mockito.when(crepo.ownerLogin()).thenReturn(owner);
        Mockito.when(crepo.name()).thenReturn(repoName);
        Mockito.when(crepo.hasGhPagesBranch()).thenReturn(ghpages);

         Mockito.when(com.repo()).thenReturn(crepo);
         return com;
    }
    
    /**
     * Mock responses and command category.
     * @param com
     * @return
     * @throws IOException
     */
    private Language mockEnglish(Command com) throws IOException {
        Language english = Mockito.mock(English.class);
        Mockito.when(english.response("step.failure.comment")).thenReturn("failure on step");
        Mockito.when(english.response("index.start.comment")).thenReturn("index start!");
        Mockito.when(english.response("index.finished.comment")).thenReturn("index finished!");
        Mockito.when(english.response("denied.fork.comment")).thenReturn("repo is a fork!");
        Mockito.when(english.response("denied.badlink.comment")).thenReturn("The page is not from this repo!");
        Mockito.when(english.response("denied.commander.comment")).thenReturn("denied because author is not repo owner!");
        Mockito.when(english.response("denied.name.comment")).thenReturn("This repo doesn't host any website!");
        Mockito.when(english.categorize(com)
        ).thenReturn(new CommandCategory("indexpage", english));
        return english;
    }
}
