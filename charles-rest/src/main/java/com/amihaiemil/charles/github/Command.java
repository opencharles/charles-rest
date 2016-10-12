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
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonObject;

import com.jcabi.github.Issue;
import com.jcabi.github.User;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;

/**
 * Command for the github agent.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
public abstract class Command {

    /**
     * Cached Github repo.
     */
    private CommandedRepo crepo;

    /**
     * Cached agentLogin.
     */
    private String agentLogin;

    /**
     * Cached author email address.
     */
    private String authorEmail;

    /**
     * Comment json.
     */
    private JsonObject comment;

    /**
     * Github issue.
     */
    private Issue issue;

    /**
     * Ctor.
     * @param issue
     * @param comment
     */
    public Command(Issue issue, JsonObject comment) {
        this.issue = issue;
        this.comment = comment;
    }

    /**
     * The json comment.
     * @return Json Object representing the comment on Github issue.
     */
    public JsonObject json() {
        return this.comment;
    }

    /**
     * Specify the comment json of this command.
     * @param com
     */
    protected void comment(JsonObject com) {
        this.comment = com;
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
     * @throws IOException 
     */
    public String agentLogin() throws IOException {
        if(this.agentLogin == null) {
            this.agentLogin = this.issue.repo()
                .github().users().self().login();
        }
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
        if(this.authorEmail == null){
            User author = this.issue.repo().github().users().get(this.authorLogin());
            Iterator<String> addresses = author.emails().iterate().iterator();
            if(addresses.hasNext()) {
                this.authorEmail = addresses.next();
            } else {
                this.authorEmail = "";
            }
        }
        return this.authorEmail;
    }

    public JsonObject authorOrgMembership() throws IOException {
        JsonObject repo = this.repo().json();
        if(repo.getJsonObject("owner").getString("type").equalsIgnoreCase("organization")) {
            Request req = this.issue.repo().github().entry()
                .uri().path("/orgs/").path(
                    repo.getJsonObject("owner"
                 ).getString("login")).path("/").path(this.authorLogin()).back();
            return req.fetch().as(JsonResponse.class).json().readObject();
        } else {
            return Json.createObjectBuilder().build();
        }
            
    }

    /**
     * Returns the commanded repository.
     * The result is <b>cached</b> and so the http call to Github API is performed only at the first call.
     * @return
     */
    public CommandedRepo repo() throws IOException {
        if(this.crepo == null) {
            this.crepo = new CommandedRepo(this.issue().repo());
        }
        return this.crepo;
    }

}
