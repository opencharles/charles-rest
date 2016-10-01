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
    	    if(this.ghPagesBranch != null) {
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
