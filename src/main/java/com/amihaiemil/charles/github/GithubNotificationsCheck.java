package com.amihaiemil.charles.github;

import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

import com.jcabi.github.Issue;

/**
 * EJB that checks every minute for github notifications (mentions of the agent using @username).
 * @author Mihai Andronache (amihaiemil@gmail.com)
 *
 */
@Singleton
public class GithubNotificationsCheck {
	
	@EJB 
	GithubIssues githubIssues;
	
	@Schedule(minute="*", persistent=false)
    public void checkForNotifications() throws IOException {
    	List<Issue> issues = githubIssues.issuesMentionedIn();
    	for(Issue issue : issues) {
    		//...
    	}
    }
}
