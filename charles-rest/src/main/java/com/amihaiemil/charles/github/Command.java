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
 *  3)Neither the name of charles-rest nor the names of its
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
import java.util.Iterator;
import javax.json.JsonObject;
import org.hamcrest.Matchers;
import com.jcabi.github.Issue;
import com.jcabi.github.User;
import com.jcabi.http.Request;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;

/**
 * Command for the github agent.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
public abstract class Command {
	
	/**
	 * Cacged json representation of the Github repo.
	 */
	private JsonObject repo;

	/**
	 * Cached value.
	 */
	private Boolean ghPagesBranch;
	
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

    public JsonObject authorOrgMembership() throws IOException {
    	JsonObject repo = this.repo();
    	if(repo.getJsonObject("owner").getString("type").equalsIgnoreCase("organization")) {
            Request req = this.issue.repo().github().entry()
                .uri().path("/orgs/").path(
                    repo.getJsonObject("owner"
                 ).getString("login")).path("/").path(this.authorLogin()).back();
            return req.fetch().as(JsonResponse.class).json().readObject();
        } else {
        	throw new IllegalStateException("The owner of the repo is not an organization!");
        }
        	
    }

    /**
     * Returns the json representation of the repo in which this command was given.
     * The result is <b>cached</b> and so the http call to Github API is performed only at the first call.
     * @return
     */
    public JsonObject repo() throws IOException {
    	if(this.repo != null) {
    		return this.repo;
    	}
    	this.repo = this.issue.repo().json();
    	return this.repo;
    }

    /**
     * Returns true if the repository has a gh-pages branch, false otherwise.
     * The result is <b>cached</b> and so the http call to Github API is performed only at the first call.
     * @return true if there is a gh-pages branch, false otherwise.
     */
    public boolean hasGhPagesBranch() throws IOException {
    	try {
    	    if(this.ghPagesBranch != null) {
    	        String branchesUrlPattern = this.repo().getString("branches_url");
        	    String ghPagesUrl = branchesUrlPattern.substring(0, branchesUrlPattern.indexOf("{")) + "/gh-pages";
        	    Request req = new ApacheRequest(ghPagesUrl);
        	    this.ghPagesBranch = req.fetch().as(RestResponse.class)
    		        .assertStatus(
    		            Matchers.isOneOf(
    		                HttpURLConnection.HTTP_OK,
    		                HttpURLConnection.HTTP_NOT_FOUND
    		            )
    		        ).status() == HttpURLConnection.HTTP_OK;
        	    return this.ghPagesBranch;
    	    }
    	    return this.ghPagesBranch;
    	} catch (AssertionError aerr) {
    		throw new IOException ("Unexpected HTTP status response.", aerr);
    	}
    }
}
