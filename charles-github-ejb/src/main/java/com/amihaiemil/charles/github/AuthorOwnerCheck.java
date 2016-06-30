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

import java.io.IOException;

import javax.json.JsonObject;

/**
 * Step where the identity of the command author is checked.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $id$
 * @since 1.0.0
 *
 */
public class AuthorOwnerCheck implements Step {

	/**
	 * Command.
	 */
	private Command com; 

	/**
	 * Send a reply to the commander in case the check fails.
	 */
	private SendReply reply;
	
	/**
	 * Constructor.
	 * @param command Command received.
	 * @param message For the commander in case this check fails.
	 */
	public AuthorOwnerCheck(Command command, SendReply rpl) {
		this.com = command;
		this.reply = rpl;
	}
	
	/**
	 * Check that the author of a command is owner of the repo.
	 * @return true if the check is successful, false otherwise
	 */
	@Override
	public boolean perform() {
		try {
			JsonObject repo = this.com.issue().repo().json();
			String repoOwner = repo.getJsonObject("owner").getString("login");
			boolean isFork = repo.getBoolean("fork");
			if(repoOwner.equals(com.authorLogin()) && !isFork) {
				return true;
			}
			this.reply.perform();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
}
