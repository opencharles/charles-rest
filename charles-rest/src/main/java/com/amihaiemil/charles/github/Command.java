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

import java.io.IOException;
import java.util.Iterator;

import javax.json.JsonObject;

import com.jcabi.github.Issue;
import com.jcabi.github.User;

/**
 * Command for the github agent.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
public abstract class Command {
	protected JsonObject comment;
	protected Issue issue;
	protected String agentLogin;
	
	/**
	 * The json comment.
	 * @return Json Object representing the comment on Github issue.
	 */
    public JsonObject json() {
    	return this.comment;
    }
    
    /**
     * Parent issue.
     * @return com.jcabi.github.Issue
     */
    public Issue issue() {
    	return this.issue;
    }
    
    /**
     * Username of the Github agent.
     * @return Github agent's String username.
     */
    public String agentLogin() {
    	return this.agentLogin;
    }
    
    /**
     * Username of this command's author.
     * @return String Github username.
     */
    public String authorLogin() {
    	return comment.getJsonObject("user").getString("login");
    }

    /**
     * Email address of this command's author.
     * @return String email address.
     * @throws IOException if there is an error while making the HTTP call
     * to get the author's email address.
     */
    public String authorEmail() throws IOException {
		User author = this.issue.repo().github().users().get(this.authorLogin());
		Iterator<String> addresses = author.emails().iterate().iterator();
		if(addresses.hasNext()) {
			return addresses.next();
		} else {
			return "";
		}
	}
}
