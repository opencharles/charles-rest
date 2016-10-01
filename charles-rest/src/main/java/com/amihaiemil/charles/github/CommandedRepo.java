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
import javax.json.JsonObject;
import org.hamcrest.Matchers;
import com.jcabi.github.Repo;
import com.jcabi.http.Request;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.response.RestResponse;

/**
 * Github repository where the command has been detected.
 * additional methods like hasGhPagesBranch()
 * @author Mihai Androanche (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class CommandedRepo {

    /**
     * Original repo.
     */
    private Repo repo;

    /**
     * Cached json representation.
     */
    private JsonObject repoJson;

    /**
     * Cached flag to tell if this repo has a gh-pages branch
     * or not.
     */
    private Boolean ghPagesBranch;

    /**
     * Ctor.
     * @param repo Github repository.
     */
    public CommandedRepo(Repo repo) {
    	this.repo = repo;
    }

	/**
     * Returns true if the repository has a gh-pages branch, false otherwise.
     * The result is <b>cached</b> and so the http call to Github API is performed only at the first call.
     * @return true if there is a gh-pages branch, false otherwise.
     */
    public boolean hasGhPagesBranch() throws IOException {
    	try {
    	    if(this.ghPagesBranch == null) {
    	        String branchesUrlPattern = this.json().getString("branches_url");
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
    
    /**
     * Get the Json representation of this repo.
     * @return JsonObject repo.
     * @throws IOException If something goes wrong.
     */
    public JsonObject json() throws IOException {
    	if(this.repoJson == null) {
    		this.repoJson = this.repo.json();
    	}
    	return this.repoJson;
    }

}
