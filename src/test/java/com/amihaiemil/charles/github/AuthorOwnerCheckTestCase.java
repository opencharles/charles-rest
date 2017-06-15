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

/**
 * Unit tests for {@link AuthorOwnerCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class AuthorOwnerCheckTestCase {

    /**
     * AuthorOwnerCheck can tell when the command author is owner of the repo.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void authorIsRepoOwner() throws Exception {
        Command com = this.mockCommand("amihaiemil", "amihaiemil", 0);        
        AuthorOwnerCheck aoc = new AuthorOwnerCheck(
            new Step.Fake(true), new Step.Fake(false)
        );
        aoc.perform(com, Mockito.mock(Logger.class));
    }

    /**
     * AuthorOwnerCheck can tell when the command author is NOT owner of the repo.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void authorIsNotRepoOwner() throws Exception {
        Command com  = this.mockCommand("someone", "amihaiemil", 0);
        AuthorOwnerCheck aoc = new AuthorOwnerCheck(
            new Step.Fake(false), new Step.Fake(true)
        );
        aoc.perform(com, Mockito.mock(Logger.class));
    }

    /**
     * Mock a command for the unit tests.
     * @param author Author of the command.
     * @param repoOwner Repository owner.
     * @param port Port on which the organization membership goes.
     * @return Command mock. 
     * @throws IOException If something goes wrong.
     */
    private Command mockCommand(String author, String repoOwner, int port) throws IOException {
        JsonObject repoJson = Json.createObjectBuilder()
            .add(
                "owner",
                Json.createObjectBuilder()
                .add("login", repoOwner)
                .build()
            ).build();
        Command command = Mockito.mock(Command.class);
        Mockito.when(command.authorLogin()).thenReturn(author);
        CommandedRepo crepo = Mockito.mock(CommandedRepo.class);
        Mockito.when(crepo.json()).thenReturn(repoJson);
        Mockito.when(command.repo()).thenReturn(crepo);
        return command;
    }
    
}
