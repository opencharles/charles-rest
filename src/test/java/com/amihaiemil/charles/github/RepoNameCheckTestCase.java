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

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import sun.misc.Cache;

/**
 * Unit tests for {@link RepoNameCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class RepoNameCheckTestCase {

    /**
     * RepoNameCheck can tell when the repo's name is in the right format.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void repoNameMatches() throws Exception {
        RepoNameCheck rnc = new RepoNameCheck(
        	new Step.Fake(true), new Step.Fake(false)
        );
        rnc.perform(
            this.mockCommand("amihaiemil", "amihaiemil.github.io"),
            Mockito.mock(Logger.class)
        );
    }

    /**
     * RepoNameCheck can tell when the repo's name is not in the right format.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void repoNameDoesntMatch() throws Exception {
        RepoNameCheck rnc = new RepoNameCheck(
            new Step.Fake(false), new Step.Fake(true)
        );
        rnc.perform(this.mockCommand("amihaiemil", "reponame"), Mockito.mock(Logger.class));
    }
    
    /**
     * Mock a command for the unit tests.
     * @param owner Owner of the repo.
     * @param name Repository name.
     * @return Command mock.
     * @throws IOException If something goes wrong.
     */
    private Command mockCommand(String owner, String name) throws IOException {
        JsonObject repoJson = Json.createObjectBuilder()
            .add("name", name)
            .add(
                "owner",
                Json.createObjectBuilder().add("login", owner).build()
            ).build();
        CachedRepo repo = Mockito.mock(CachedRepo.class);
        Mockito.when(repo.json()).thenReturn(repoJson);
        Command command = Mockito.mock(Command.class);
        Mockito.when(command.repo()).thenReturn(repo);
        return command;
    }

}
