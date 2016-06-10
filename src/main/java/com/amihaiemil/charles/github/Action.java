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
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that the agent takes once it finds a Github issue where it's been mentioned.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 *
 */
public class Action implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Action.class);

	/**
	 * Thread that runs this.
	 */
	private Thread tr;
	/**
	 * Github issue where the command was given.
	 */
	private GithubIssue issue;
	
	/**
	 * Github username of the agent.
	 */
	private String agentLogin;
	
	/**
	 * Possible responses of the agent.
	 */
	private Responses responses;
	
	/**
	 * Constructor.
	 * @param issue - The Github issue where the agent was mentionsd.
	 * @param agentLogin - The Github username of the agent.
	 * @param resp Possible responses.
	 */
	public Action(GithubIssue issue, String agentLogin, Responses resp) {
		String threadName = issue.getRepo() + "/" + issue.getNumber() + "_" + UUID.randomUUID().toString();
		tr = new Thread(this, threadName);
		this.agentLogin = agentLogin;
		this.issue = issue;
		this.responses = resp;
	}
	
	
	@Override
	public void run() {
		ValidCommand command;
		try {
			command = new ValidCommand(new LastComment(issue, agentLogin));
			String commandBody = command.json().getString("body");
			if(commandBody.equals("@" + agentLogin + " hello") || commandBody.equals("@" + agentLogin + " hi")) {
				this.sendReply(
					new TextReply(command),
					responses.getResponse("hello.comment")
				);
			}
		} catch (IllegalArgumentException e) {
			LOG.info("No command found in the issue or the agent has already replied to the last command!");
		} catch (IOException e) {
			LOG.error("Action failed with IOException: ",  e);
			this.sendReply(
				new ErrorReply("#", this.issue.getSelf()),
				responses.getResponse("error.comment")
			);
		}
	}
	
	/**
	 * Take this action.
	 */
	public void take() { 
		this.tr.start();
	}
	
	/**
	 * Send the reply to Github issue.
	 * @param reply 
	 * @param response
	 */
	private void sendReply(Reply reply, String response) {
		try {
			reply.send(response);
		} catch (IOException e) {
			LOG.error("FAILED TO REPLY!", e);
		}
	}
}
