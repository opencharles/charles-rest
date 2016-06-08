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
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import com.google.common.collect.Lists;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;

/**
 * Last comment where the agent was mentioned.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 *
 */
//TODO write tests for this class.
public class LastComment implements Comment {
	private JsonObject com;
	
	public LastComment(GithubIssue issue, String agentlogin) throws IOException {
		JsonObject latestCommentJson = issue.getLatestComment().json();
		if(latestCommentJson.getString("body").contains("@" + agentlogin)) {
			this.com = latestCommentJson;
		} else {
			List<Comment> comments = Lists.newArrayList(issue.getSelf().comments().iterate());
			boolean agentReplyFound = false;
			for(int i=comments.size() - 1; !agentReplyFound && i >=0; i--) {
				Comment current = comments.get(i);
				if(current.number() == latestCommentJson.getInt("id")) {
					continue;
				} else {
					JsonObject currentJsonComment = current.json();
					if(currentJsonComment.getJsonObject("user").getString("login").equals(agentlogin)) {
						agentReplyFound = true;
						this.com = Json.createObjectBuilder().add("id", "-1").add("body", "").build();
					} else {
						if(currentJsonComment.getString("body").contains("@" + agentlogin)) {
							this.com = currentJsonComment;
						}
					}
				}
			}
		}
	}

	@Override
	public int compareTo(Comment arg0) {
		throw new UnsupportedOperationException("#compareTo(Comment)");
	}

	@Override
	public JsonObject json() throws IOException {
		return this.com;
	}

	@Override
	public void patch(JsonObject json) throws IOException {
		throw new UnsupportedOperationException("#path(JsonObject)");
	}

	@Override
	public Issue issue() {
		throw new UnsupportedOperationException("#issue()");
	}

	@Override
	public int number() {
		return com.getInt("id");
	}

	@Override
	public void remove() throws IOException {
		throw new UnsupportedOperationException("#remove()");
	}
}
