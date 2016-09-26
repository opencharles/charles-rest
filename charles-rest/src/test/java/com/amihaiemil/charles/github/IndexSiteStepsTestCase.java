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

import java.io.IOException;
import javax.json.Json;
import org.mockito.Mockito;
import com.jcabi.github.Issue;
import com.jcabi.github.mock.MkGithub;

/**
 * Unit tests for {@link IndexSteps}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @sice 1.0.0
 *
 */
public class IndexSiteStepsTestCase {

    /**
	 * Mock a command for the unit tests.
	 * @param author Author of the command.
	 * @param repoOwner Repository owner.
	 * @param authorEmail Command author's email.
	 * @return Command mock.
	 * @throws IOException If something goes wrong.
	 */
	private Command mockCommand(String author, String authorEmail, String repoSowner) throws IOException {
		MkGithub gh = new MkGithub(repoSowner);
		Issue issue = gh.randomRepo().issues().create("title", "body");
		Command command = Mockito.mock(Command.class);
		Mockito.when(command.authorLogin()).thenReturn(author);
		Mockito.when(command.authorEmail()).thenReturn(authorEmail);
		Mockito.when(command.issue()).thenReturn(issue);
		Mockito.when(command.json()).thenReturn(
		    Json.createObjectBuilder().add("body", "@charlesmike index pls").build()
        );
		return command;
	}
	
}
