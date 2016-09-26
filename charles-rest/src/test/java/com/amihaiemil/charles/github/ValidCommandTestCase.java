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
 *  3)Neither the name or charles-rest nor the names of its
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

import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.mockito.Mockito;

import com.jcabi.github.Comment;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;

/**
 * Unit tests for {@link ValidCommand}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
public class ValidCommandTestCase {
	
	@Test(expected = IllegalArgumentException.class)
    public void exceptionOnEmptyComment() throws Exception {
		Command comm = Mockito.mock(Command.class);
		Mockito.when(comm.json()).thenReturn(Json.createObjectBuilder().add("body", "").build());
    	
    	new ValidCommand(comm);
    }
	
	@Test
	public void acceptsValidComment() throws Exception {
		Command comm = Mockito.mock(Command.class);
		JsonObject json = Json.createObjectBuilder().add("body", "test text").add("id", 2).build();
		Mockito.when(comm.json()).thenReturn(json);
		    	
    	assertTrue(new ValidCommand(comm).json().equals(json));
	}

	@Test
	public void getsAuthorLogin() throws Exception {
		Command comm = Mockito.mock(Command.class);
		JsonObject user = Json.createObjectBuilder().add("login", "amihaiemil").build();
		
		JsonObject json = Json.createObjectBuilder()
		    .add("user", user)
			.add("body", "test text")
		    .add("id", 2)
		    .build();
		Mockito.when(comm.json()).thenReturn(json);
		ValidCommand vc = new ValidCommand(comm);
		assertTrue(vc.authorLogin().equals("amihaiemil"));
	}

	@Test
	public void getsAuthorEmail() throws Exception {
		MkStorage storage = new MkStorage.Synced(new MkStorage.InFile());
		
		MkGithub authorGh = new MkGithub(storage, "amihaiemil");
		authorGh.users().self().emails().add(Arrays.asList("amihaiemil@gmail.com"));
		Repo authorRepo = authorGh.randomRepo();
		Comment com = authorRepo.issues().create("", "").comments().post("@charlesmike do something");

		Github agentGh = new MkGithub(storage, "charlesmike");
		Issue issue = agentGh.repos().get(authorRepo.coordinates()).issues().get(com.issue().number());
		Command comm = Mockito.mock(Command.class);
		
		JsonObject authorInfo = Json.createObjectBuilder().add("login", "amihaiemil").build();
		JsonObject json = Json.createObjectBuilder()
			.add("user", authorInfo)
			.add("body", com.json().getString("body"))
		    .add("id", 2)
		    .build();
		Mockito.when(comm.json()).thenReturn(json);
		Mockito.when(comm.issue()).thenReturn(issue);

		ValidCommand vc = new ValidCommand(comm);
		assertTrue(vc.authorEmail().equals("amihaiemil@gmail.com"));
	}
	
	@Test
	public void getsAgentLogin() {
		Command comm = Mockito.mock(Command.class);
		Mockito.when(comm.agentLogin()).thenReturn("chalesmike");
		JsonObject json = Json.createObjectBuilder()
			.add("body", "test text")
		    .add("id", 2)
		    .build();
		Mockito.when(comm.json()).thenReturn(json);
		ValidCommand vc = new ValidCommand(comm);
		assertTrue(vc.agentLogin().equals("chalesmike"));
	}
}
