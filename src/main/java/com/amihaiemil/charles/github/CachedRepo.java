/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of charles-rest nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.amihaiemil.charles.github;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import javax.json.JsonObject;

import com.jcabi.github.Content;
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
public class CachedRepo {

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
     * Cached .charles.yml file
     */
    private CharlesYml yml;

    /**
     * Ctor.
     * @param repo Github repository.
     */
    public CachedRepo(Repo repo) {
        this.repo = repo;
    }

    /**
     * Returns true if the repository has a gh-pages branch, false otherwise.
     * The result is <b>cached</b> and so the http call to Github API is performed only at the first call.
     * @return true if there is a gh-pages branch, false otherwise.
     * @throws IOException If an error occurs
     *  while communicating with the Github API.
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
     * @throws IOException If an error occurs
     *  while reading the repo from the Github API.
     */
    public JsonObject json() throws IOException {
        if(this.repoJson == null) {
            this.repoJson = this.repo.json();
        }
        return this.repoJson;
    }

    /**
     * Get the repo's name.
     * @return String.
     * @throws IOException If an error occurs
     *  while reading the repo from the Github API.
     */
    public String name() throws IOException{
        return this.json().getString("name");
    }

    /**
     * Get the repo owner's login.
     * @return String.
     * @throws IOException If an error occurs
     *  while reading the repo from the Github API.
     */
    public String ownerLogin() throws IOException {
        return this.json().getJsonObject("owner").getString("login");
    }

    /**
     * Is this repo owned by an organization?
     * @return True or false
     * @throws IOException If there's an error when communicating with the
     *  Github API.
     */
    public boolean isOwnedByOrganization() throws IOException {
        return "organization".equalsIgnoreCase(
            this.json().getJsonObject("owner").getString("type")
        );
    }

    /**
     * The charles.yml file contained in the repo.
     * @return {@link CharlesYml}
     * @throws IOException
     */
    public CharlesYml charlesYml() throws IOException {
        if(this.yml == null) {
            if(this.repo.contents().exists(".charles.yml", "master")) {
                this.yml = new CharlesYmlInput(
                    new ByteArrayInputStream(
                        new Content.Smart(
                            this.repo
                                .contents()
                                .get(".charles.yml")
                        ).decoded()
                    )
                );
            } else {
                this.yml = new CharlesYml.Default();
            }
        }
        return this.yml;
    }

}
