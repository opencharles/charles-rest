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
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link GithubNotificationsCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class GithubNotificationsCheckTestCase {

    /**
     * GithubNotificationsCheck works fine when there are no issues.
     */
    @Test
    public void noNewIssues() throws Exception {
    	GithubAgent ga = Mockito.mock(GithubAgent.class);
    	Mockito.when(ga.issuesMentionedIn()).thenReturn(new ArrayList<GithubIssue>());
    	Mockito.when(ga.agentLogin()).thenThrow(new IllegalStateException("Test failed; this exception should not be thrown!"));

    	GithubNotificationsCheck gnc = new GithubNotificationsCheck(ga);
    	gnc.checkForNotifications();
    }

	/**
	 * GithubNotificationsCheck handles IOException if the issues' reading fails.
	 */
	@Test
	public void issuesReadFailes() throws Exception {
        GithubAgent ga = Mockito.mock(GithubAgent.class);
        Mockito.when(ga.issuesMentionedIn()).thenThrow(new IOException("Test IOException from issues check. This was expected and it's ok!"));
        Mockito.when(ga.agentLogin()).thenThrow(new IllegalStateException("Test failed; this exception should not be thrown!"));
 
    	GithubNotificationsCheck gnc = new GithubNotificationsCheck(ga);
        gnc.checkForNotifications();
    }
	
	/**
	 * GithubNotificationsCheck handles IOException if the author's login reading fails.
	 */
	@Test
	public void authorLoginReadFailed() throws Exception {
        GithubAgent ga = Mockito.mock(GithubAgent.class);
        List<GithubIssue> issues = new ArrayList<GithubIssue>();
        issues.add(Mockito.mock(GithubIssue.class));
        Mockito.when(ga.issuesMentionedIn()).thenReturn(issues);
        Mockito.when(ga.agentLogin()).thenThrow(new IOException("Test IOException from author login check. This was expected and it's ok!"));
 
    	GithubNotificationsCheck gnc = new GithubNotificationsCheck(ga);
        gnc.checkForNotifications();
    }
}
