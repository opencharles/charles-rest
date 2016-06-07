package com.amihaiemil.charles.github;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test cases for {@link GithubIssues}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 *
 */
public class GithubNotificationsITCase {
	@Test
    public void getsNotifitcations() throws Exception {
    	GithubIssues gn = new GithubIssues();
    	assertTrue(gn.issuesMentionedIn() != null);
    }
}
