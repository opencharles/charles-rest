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

import static org.junit.Assert.*;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

/**
 * Unit tests for {@link PageHostedOnGithubCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class PageHostedOnGithubCheckTestCase {

    /**
     * PageHostedOnGithubCheck can tell when a link is valid in a repo
     * with gh-pages branch.
     */
    @Test
    public void tellsValidLinkGhPages() {
        PageHostedOnGithubCheck phgc = new PageHostedOnGithubCheck(
            this.mockRepo("amihaiemil", "myrepo"),
            "http://amihaiemil.github.io/myrepo/stuff/page.html",
            Mockito.mock(Logger.class)
        );
        assertTrue(phgc.perform());

        PageHostedOnGithubCheck phgc2 = new PageHostedOnGithubCheck(
            this.mockRepo("amihaiemil", "myrepo"),
            "https://amihaiemil.github.io/myrepo/stuff/page.html",
            Mockito.mock(Logger.class)
        );
        assertTrue(phgc2.perform());
    }
    
    /**
     * PageHostedOnGithubCheck can tell when a link is not valid in a repo
     * with gh-pages branch.
     */
    @Test
    public void tellsInvalidLinkGhPages() {
        PageHostedOnGithubCheck phgc = new PageHostedOnGithubCheck(
            this.mockRepo("amihaiemil", "myrepo"),
            "http://domain.io/stuff/page.html",
            Mockito.mock(Logger.class)
        );
        assertFalse(phgc.perform());

        PageHostedOnGithubCheck phgc2 = new PageHostedOnGithubCheck(
            this.mockRepo("amihaiemil", "myrepo"),
            "ftp://amihaiemil.github.io/folder/stuff/page.html",
            Mockito.mock(Logger.class)
        );
        assertFalse(phgc2.perform());
    }

    /**
     * PageHostedOnGithubCheck can tell when a link is valid in a repo
     * without gh-pages branch.
     */
    @Test
    public void tellsValidLink() {
        PageHostedOnGithubCheck phgc = new PageHostedOnGithubCheck(
            this.mockRepo("amihaiemil", "myrepo"),
            "http://amihaiemil.github.io/myrepo/stuff/page.html",
            Mockito.mock(Logger.class)
        );
        assertTrue(phgc.perform());

        PageHostedOnGithubCheck phgc2 = new PageHostedOnGithubCheck(
            this.mockRepo("amihaiemil", "myrepo"),
            "https://amihaiemil.github.io/myrepo/stuff/page.html",
            Mockito.mock(Logger.class)
        );
        assertTrue(phgc2.perform());
    }
    
    /**
     * PageHostedOnGithubCheck can tell when a link is not valid in a repo
     * without gh-pages branch.
     */
    @Test
    public void tellsInvalidLink() {
        PageHostedOnGithubCheck phgc = new PageHostedOnGithubCheck(
            this.mockRepo("amihaiemil", "myrepo"),
            "http://amihaiemil.github.io/stuff/page.html",
            Mockito.mock(Logger.class)
        );
        assertFalse(phgc.perform());

        PageHostedOnGithubCheck phgc2 = new PageHostedOnGithubCheck(
            this.mockRepo("amihaiemil", "myrepo"),
            "ftp://amihaiemil.github.io/myrepo/stuff/page.html",
            Mockito.mock(Logger.class)
        );
        assertFalse(phgc2.perform());
    }

    /**
     * Create a mock json repo for tests.
     * @param owner Login of the owner.
     * @param name Name of the repo.
     * @return JsonObject repo.
     */
    public JsonObject mockRepo(String owner, String name) {
    	return Json.createObjectBuilder()
    	    .add("name", name)
    	    .add(
    	        "owner",
    	        Json.createObjectBuilder().add("login", owner).build()
    	    ).build();
    }
}
