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
    public void recognizesHelloCommands() {
    	Command hello1 = this.mockCommand("@charlesmike, hello there!");
    	Command hello2 = this.mockCommand("@charlesmike, hello?!");
    	Command hello3 = this.mockCommand("@charlesmike hi");
    	Command hello4 = this.mockCommand("@charlesmike, how do you do??");
    	Command hello5 = this.mockCommand("@charlesmike who are you?");
    	
    	Language english = new English();
    	assertTrue(english.categorize(hello1).type().equals("hello"));
    	assertTrue(english.categorize(hello2).type().equals("hello"));
    	assertTrue(english.categorize(hello3).type().equals("hello"));
    	assertTrue(english.categorize(hello4).type().equals("hello"));
    	assertTrue(english.categorize(hello5).type().equals("hello"));
    }
	
	/**
	 * A 'indexsite' command is understood.
	 */
	@Test
	public void recognizezIndexSiteCommands() {
		Command index1 = this.mockCommand("@charlesmike, please index!");
    	Command index2 = this.mockCommand("@charlesmike, index pls");
    	Command index3 = this.mockCommand("@charlesmike index...");
    	Command index4 = this.mockCommand("@charlesmike index please!!!");
    	Command index5 = this.mockCommand("@charlesmike, pls index?");
    	
    	Language english = new English();
    	assertTrue(english.categorize(index1).type().equals("indexsite"));
    	assertTrue(english.categorize(index2).type().equals("indexsite"));
    	assertTrue(english.categorize(index3).type().equals("indexsite"));
    	assertTrue(english.categorize(index4).type().equals("indexsite"));
    	assertTrue(english.categorize(index5).type().equals("indexsite"));
	}
	
	/**
	 * A 'indexpage' command is understood.
	 */
	@Test
	public void recognizesIndexPageCommands() {
		Command indexPage1 = this.mockCommand("@charlesmike, please index [this](http://www.amihaiemil.com) page");
    	Command indexPage2 = this.mockCommand("@charlesmike index [this] (test.com) page!");
    	Command indexPage3 = this.mockCommand("@charlesmike, pls index [this] (eva.amihaiemil.com) page!!!");
    	Command indexPage4 = this.mockCommand("@charlesmike index [this] (http://www.amihaiemil.com) page ...");
    	Command indexPage5 = this.mockCommand("@charlesmike index [this]       (http://www.amihaiemil.com) page ...");

    	Language english = new English();
    	assertTrue(english.categorize(indexPage1).type().equals("indexpage"));
    	assertTrue(english.categorize(indexPage2).type().equals("indexpage"));
    	assertTrue(english.categorize(indexPage3).type().equals("indexpage"));
    	assertTrue(english.categorize(indexPage4).type().equals("indexpage"));
    	assertTrue(english.categorize(indexPage5).type().equals("indexpage"));
	}
	
	/**
	 * There is an English response for "hello".
	 */
	@Test
	public void knowsTheHelloResponse() {
		Language eng = new English();
		String hi = eng.response("hello.comment");
		assertTrue(hi.contains("Hi, %s! I can help you index your Github"));
	}
	
	/**
     * Mock a command.
     * @return The created Command.
     */
    public Command mockCommand(String message) {
    	JsonObject body = Json.createObjectBuilder().add("body", message).build();
    	Command com = Mockito.mock(Command.class);
    	Mockito.when(com.json()).thenReturn(body);
    	Mockito.when(com.login()).thenReturn("charlesmike");
    	return com;
    }
}
