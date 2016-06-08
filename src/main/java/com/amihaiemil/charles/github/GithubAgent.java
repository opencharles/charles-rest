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
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.json.JsonObject;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.RtGithub;
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;

/**
 * Fetches from Github the issues in which the agent has been mentioned.
 * @author Mihai Andronache(amihaiemil@gmail.com)
 *
 */
@Stateless
public class GithubAgent {
	private Github github;
	private static final Logger LOG = LoggerFactory.getLogger(GithubAgent.class);
	
	public GithubAgent() {
		this.github = new RtGithub(
					      System.getProperty("charles.github.ejb.token")
					  );
		
	}

	/**
	 * Get the Github issues that the agent is mentiond in.
	 * @return List of Github {@link Issue}
	 * @throws IOException - if something goes wrong.
	 */
	public List<GithubIssue> issuesMentionedIn() throws IOException {
		LOG.info("Checking for issues where the agent was mentioned");
		Iterable<JsonObject> notifications = new RtPagination<JsonObject>(
			this.github.entry().uri().path("/notifications").back(),
			RtPagination.COPYING
		);
		List<GithubIssue> issues = new ArrayList<GithubIssue>();
		for(JsonObject notification : notifications) {
			if("mention".equals(notification.getString("reason"))) {
				String issueUrl = notification.getJsonObject("subject").getString("url");
				int issueNumber = Integer.parseInt(issueUrl.substring(issueUrl.lastIndexOf("/") + 1));
				String repoFullName = notification.getJsonObject("repository").getString("full_name");
				issues.add(
					new GithubIssue(
						repoFullName,
						issueNumber,
						this.github.repos().get(
								new Coordinates.Simple(repoFullName)
						).issues().get(issueNumber)
					)
				);
			}
		}
		if(!issues.isEmpty()) {
			LOG.info("Found "  + issues.size() + " issues where the agent " + 
					" was mentioned. Marking the corresponding notifications as read.");

			this.markAsRead();
			LOG.info("Done marking notifications as read.");
		} else {
			LOG.info("No issues where the agent is mentioned!");
		}
		return issues;
	}
	
	public String agentLogin() throws IOException {
		return this.github.users().self().login();
	}
	
	/**
	 * Mark notifications as read.
	 * @throws IOException
	 */
	private void markAsRead() throws IOException {
		this.github
			.entry()
			.uri()
			.path("/notifications")
			.queryParam(
				"last_read_at",
				DateFormatUtils.formatUTC(
					new Date(System.currentTimeMillis()),
					"yyyy-MM-dd'T'HH:mm:ss'Z'")).back()
			.method(Request.PUT).body().set("{}").back().fetch()
			.as(RestResponse.class)
			.assertStatus(HttpURLConnection.HTTP_RESET);
	}
}
