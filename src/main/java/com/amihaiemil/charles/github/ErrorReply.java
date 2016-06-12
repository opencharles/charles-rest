package com.amihaiemil.charles.github;

import java.io.IOException;

import com.jcabi.github.Issue;

/**
 * Reply the agent gives when there was an error on the server.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 *
 */
public class ErrorReply implements Reply {

	private String logsAddress;
	private Issue issue;
	
	public ErrorReply(String logsAddress, Issue issue) {
		this.logsAddress = logsAddress;
		this.issue = issue;
	}
	
	@Override
	public void send() throws IOException {
		this.issue.comments().post(String.format("", this.logsAddress));
	}

}
