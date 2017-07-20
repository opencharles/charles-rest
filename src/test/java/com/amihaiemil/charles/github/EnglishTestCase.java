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

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link English}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @@version $Id$
 * @since 1.0.0
 */
public class EnglishTestCase {
    /**
     * A 'hello' command is understood.
     */
    @Test
    public void recognizesHelloCommands() throws Exception {
        Command hello1 = this.mockCommand("@charlesmike, hello there!");
        Command hello2 = this.mockCommand("@charlesmike, hello?!");
        Command hello3 = this.mockCommand("@charlesmike hello");
        Command hello4 = this.mockCommand("@charlesmike hello, how do you do??");
        Command hello5 = this.mockCommand("@charlesmike hello, who are you?");
        
        Language english = new English();
        assertTrue(english.categorize(hello1).equals("hello"));
        assertTrue(english.categorize(hello2).equals("hello"));
        assertTrue(english.categorize(hello3).equals("hello"));
        assertTrue(english.categorize(hello4).equals("hello"));
        assertTrue(english.categorize(hello5).equals("hello"));
    }
    
    /**
     * A 'indexsite' command is understood.
     */
    @Test
    public void recognizezIndexSiteCommands() throws Exception{
        Command index1 = this.mockCommand("@charlesmike, please index this site!");
        Command index2 = this.mockCommand("@charlesmike, index site pls");
        Command index3 = this.mockCommand("@charlesmike index this repo's site...");
        Command index4 = this.mockCommand("@charlesmike index site please!!!");
        Command index5 = this.mockCommand("@charlesmike, pls index site?");
        
        Language english = new English();
        assertTrue(english.categorize(index1).equals("indexsite"));
        assertTrue(english.categorize(index2).equals("indexsite"));
        assertTrue(english.categorize(index3).equals("indexsite"));
        assertTrue(english.categorize(index4).equals("indexsite"));
        assertTrue(english.categorize(index5).equals("indexsite"));
    }
    
    /**
     * A 'indexpage' command is understood.
     */
    @Test
    public void recognizesIndexPageCommands() throws Exception{
        Command indexPage1 = this.mockCommand("@charlesmike, please index [this](http://www.amihaiemil.com) page");
        Command indexPage2 = this.mockCommand("@charlesmike index [this] (test.com) page!");
        Command indexPage3 = this.mockCommand("@charlesmike, pls index [this] (eva.amihaiemil.com) page!!!");
        Command indexPage4 = this.mockCommand("@charlesmike index [this] (http://www.amihaiemil.com) page ...");
        Command indexPage5 = this.mockCommand("@charlesmike index [this]       (http://www.amihaiemil.com) page ...");

        Language english = new English();
        assertTrue(english.categorize(indexPage1).equals("indexpage"));
        assertTrue(english.categorize(indexPage2).equals("indexpage"));
        assertTrue(english.categorize(indexPage3).equals("indexpage"));
        assertTrue(english.categorize(indexPage4).equals("indexpage"));
        assertTrue(english.categorize(indexPage5).equals("indexpage"));
    }
    
    /**
     * A 'deleteindex' command is understood.
     */
    @Test
    public void recognizesDeleteIndexCommands() throws Exception{
        Command deletePage1 = this.mockCommand("@charlesmike, delete `eva` index pls");
        Command deletePage2 = this.mockCommand("@charlesmike delete `amihaiemil.github.io` index");
        Command deletePage3 = this.mockCommand("@charlesmike, delete this index");
        Command deletePage4 = this.mockCommand("@charlesmike delete `charles` index please");
        Command deletePage5 = this.mockCommand("@charlesmike delete `charles-github-ejb` index");

        Language english = new English();
        assertTrue(english.categorize(deletePage1).equals("deleteindex"));
        assertTrue(english.categorize(deletePage2).equals("deleteindex"));
        assertTrue(english.categorize(deletePage3).equals("deleteindex"));
        assertTrue(english.categorize(deletePage4).equals("deleteindex"));
        assertTrue(english.categorize(deletePage5).equals("deleteindex"));
    }
    
    /**
     * A command can be categorized as unknown.
     */
    @Test
    public void unknownCommands() throws Exception{
        Command unknown1 = this.mockCommand("@charlesmike I don't think you understand");
        Command unknown2 = this.mockCommand("@charlesmike Why don't you respond?");
        Command unknown3 = this.mockCommand("@charlesmike how many languages do you speak?");

        Language english = new English();
        assertTrue(english.categorize(unknown1).equals("unknown"));
        assertTrue(english.categorize(unknown2).equals("unknown"));
        assertTrue(english.categorize(unknown3).equals("unknown"));
    }
    
    /**
     * There is an English response for "hello".
     */
    @Test
    public void knowsTheHelloResponse() {
        Language eng = new English();
        String hi = eng.response("hello.comment");
        assertTrue(hi.contains("Hi @%s! I can help you index your Github"));
    }
    
    /**
     * Mock a command.
     * @return The created Command.
     */
    public Command mockCommand(String message) throws Exception{
        JsonObject body = Json.createObjectBuilder().add("body", message).build();
        Command com = Mockito.mock(Command.class);
        Mockito.when(com.json()).thenReturn(body);
        Mockito.when(com.agentLogin()).thenReturn("charlesmike");
        return com;
    }
}
