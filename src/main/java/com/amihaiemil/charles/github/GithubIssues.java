package com.amihaiemil.charles.github;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateful;
import javax.json.JsonObject;

import org.apache.commons.lang3.time.DateFormatUtils;

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
@Stateful
public class GithubIssues {
	private Github github;

	public GithubIssues() {
		System.out.println(System.getProperty("charles.github.ejb.token"));
		this.github = new RtGithub(
					      System.getProperty("charles.github.ejb.token")
					  );
	}

	/**
	 * Get the Github issues that the agent is mentiond in.
	 * @return List of Github {@link Issue}
	 * @throws IOException - if something goes wrong.
	 */
	public List<Issue> issuesMentionedIn() throws IOException {		
		Iterable<JsonObject> notifications = new RtPagination<JsonObject>(
			this.github.entry().uri().path("/notifications").back(),
			RtPagination.COPYING
		);
		List<Issue> issues = new ArrayList<Issue>();
		for(JsonObject notification : notifications) {
			if("mention".equals(notification.getString("reason"))) {
				String issueUrl = notification.getJsonObject("subject").getString("url");
				int issueNumber = Integer.parseInt(issueUrl.substring(issueUrl.lastIndexOf("/") + 1));
				String repoFullName = notification.getJsonObject("repository").getString("full_name");
				issues.add(
					this.github.repos().get(
						new Coordinates.Simple(repoFullName)
					).issues().get(issueNumber)
				);
			}
		}
		if(!issues.isEmpty()) {
			this.markAsRead();
		}
		return issues;
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
