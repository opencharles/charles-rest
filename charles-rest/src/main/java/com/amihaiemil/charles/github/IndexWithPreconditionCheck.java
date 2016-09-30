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

import javax.json.JsonObject;

import org.slf4j.Logger;

import com.amihaiemil.charles.steps.Step;

/**
 * Step that performs all the precondition checks that have to be met in order to execute
 * any action related to the index.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
class IndexWithPreconditionCheck implements Step {

	private Command com;
	private Language lang;
	private Logger logger;

	/**
	 * The command's author is the repo owner (the repo is under their name).
	 */
	private Step authorOwnerStep;

	/**
	 * The command's author is one of the organization's active admins.
	 */
	private Step orgAdminCheck;
	
	/**
	 * Checks if the repo is a fork or not.
	 */
	private Step repoForkCheck;

	/**
	 * Checks the repo name format. If it doesn't match,
	 * the gh-pages check has to be performed triggered.
	 */
	private Step repoNameCheck;

	/**
	 * Checks if the repo has a gh-pages branch
	 */
	private Step ghPagesBranchCheck;

	/**
	 * Used when indexing a single page, to check if the given link is from
	 * a github-hosted website.
	 */
	private Step pageHostedOnGithubCheck;

	/**
	 * Index steps;
	 */
	private IndexSteps index;

	/**
	 * Builder pattern ctor.
	 * @param builder
	 */
	IndexWithPreconditionCheck(IndexWithPreconditionCheckBuilder builder) {
	    this.com = builder.com;
	    this.lang = builder.lang;
	    this.logger = builder.logger;
	    this.authorOwnerStep = builder.authorOwnerStep;
	    this.orgAdminCheck = builder.orgAdminCheck;
	    this.repoForkCheck = builder.repoForkCheck;
	    this.repoNameCheck = builder.repoNameCheck;
	    this.ghPagesBranchCheck = builder.ghPagesBranchCheck;
	    this.pageHostedOnGithubCheck = builder.pageOnGithub;
	    this.index = builder.index;
	}

	@Override
	public boolean perform() {
	    boolean success;
	    boolean repoBlogOrGhPages = false;
	    boolean owner = false;
		if (this.repoNameCheck.perform()) {
			repoBlogOrGhPages = true;
        } else {
            boolean ghPagesBranch = this.ghPagesBranchCheck.perform();
            if(ghPagesBranch) {
            	repoBlogOrGhPages = true;
            }
        }
		if(repoBlogOrGhPages) {
		    if(this.authorOwnerStep.perform()) {
		    	owner = true;
		    } else {
		    	boolean ownedByOrg = repoIsOwnedByOrganization();
		    	if(ownedByOrg) {
		    		if(this.orgAdminCheck.perform()) {
		    			owner = true;	
		    		}
		    	}
		    }
		    if(owner) {
		    	if(this.repoForkCheck.perform()) {
		        	if(this.pageHostedOnGithubCheck.perform()) {
		    	        startIndexComment().perform();
		    	        success = this.index.perform();
		        	} else {
		                success = this.denialReply("denied.badlink.comment").perform();
		        	}
		        } else {
		    	    success = this.denialReply("denied.fork.comment").perform();
		        }
		    } else {
		    	success = denialReply("denied.commander.comment").perform();
		    }
		} else {
        	success = this.denialReply("denied.name.comment").perform();
        }
		return success;
	}

	/**
	 * After all the precondition checks are met, the agent first lets the commander know that the index
	 * action has started.
	 * @return SendReply step.
	 */
    SendReply startIndexComment() {
        return new SendReply(
		    new TextReply(
		        this.com,
		        String.format(
		            this.lang.response("index.start.comment"),
		            this.com.authorLogin()
		        )
		    ), this.logger
		);
	}
	
    /**
     * Builds the reply to send to an unauthorized command.
     * @return SendReply step.
     */
    SendReply denialReply(String messagekey) {
        Reply rep = new TextReply(
            this.com,
            String.format(
         	    this.lang.response(messagekey),
                this.com.authorLogin()
         	)
        );
        return new SendReply(rep, this.logger);
    }
    
    /**
     * Is the repo owned by an organization?
     * @return True if the repo owner has type Organization, false otherwise.
     */
    boolean repoIsOwnedByOrganization() {
    	try {
			return com.repo().getJsonObject("owner").getString("type").equalsIgnoreCase("organization");
		} catch (IOException e) {
			throw new IllegalStateException("Error when checking repo owner type!", e);
		}
    }
	
	/**
     * Builder for {@link IndexWithPreconditionCheck}. All the steps have default values which
     * always perform true, in order to not be blockers if they are not specified in the builder.
     * 
     * An unspecified step should not be included in the precondition
     */
    public static class IndexWithPreconditionCheckBuilder {
    	private Command com;
    	private Language lang;
    	private Logger logger;
    	private Step authorOwnerStep = new Step.MissingStep();
        private Step orgAdminCheck = new Step.MissingStep();
    	private Step repoForkCheck = new Step.MissingStep();
    	private Step repoNameCheck = new Step.MissingStep();
    	private Step ghPagesBranchCheck = new Step.MissingStep();
    	private Step pageOnGithub = new Step.MissingStep();

    	private IndexSteps index;

    	/**
    	 * Constructor.
    	 * @param com Command that triggered the action.
    	 * @param repo Json repo as returned by the Github API
    	 * @param lang Spoken Language.
    	 * @param logger Action logger.
    	 */
    	public IndexWithPreconditionCheckBuilder(
    	    Command com, JsonObject repo,
    	    Language lang, Logger logger, LogsLocation logs
    	) {
    		this.com = com;
    		this.lang = lang;
    		this.logger = logger;
    	}
    	
    	/**
    	 * Specify the author name check to this builder.
    	 * @param aoc Given author name check.
    	 * @return This builder.
    	 */
    	public IndexWithPreconditionCheckBuilder authorOwnerCheck(AuthorOwnerCheck aoc) {
    		this.authorOwnerStep = aoc;
    		return this;
    	}
    	
    	/**
    	 * Specify the organization admin check for this builder.
    	 * @param oac Given organization admin check.
    	 * @return This builder.
    	 */
    	public IndexWithPreconditionCheckBuilder orgAdminCheck(OrganizationAdminCheck oac) {
    		this.orgAdminCheck = oac;
    		return this;
    	}
    	
    	/**
    	 * Specify the repository fork check for this builder.
    	 * @param rfc Given RepositoryForkCheck.
    	 * @return This builder.
    	 */
    	public IndexWithPreconditionCheckBuilder repoForkCheck(RepoForkCheck rfc) {
    		this.repoForkCheck = rfc;
    		return this;
    	}
    	
    	/**
    	 * Specify the repository name check to this builder.
    	 * @param rnc Given repository name check.
    	 * @return This builder.
    	 */
    	public IndexWithPreconditionCheckBuilder repoNameCheck(RepoNameCheck rnc) {    			
    		this.repoNameCheck = rnc;
    		return this;
    	}
    	
    	/**
    	 * Specify the gh-pages branch check to this builder.
    	 * @param gpc Given Github pages branch check.
    	 * @return This builder.
    	 */
    	public IndexWithPreconditionCheckBuilder ghPagesBranchCheck(GhPagesBranchCheck gpc) {
    		this.ghPagesBranchCheck = gpc;
    		return this;
    	}
    	
    	/**
    	 * Specify the page-on-github check to this builder.
    	 * @param gpc Given Github pages branch check.
    	 * @return This builder.
    	 */
    	public IndexWithPreconditionCheckBuilder pageOnGithubCheck(PageHostedOnGithubCheck phgc) {
    		this.pageOnGithub = phgc;
    		return this;
    	}
    	
    	/**
    	 * Specify the index step.
    	 * @param is Index step.
    	 * @return This builder.
    	 */
    	public IndexWithPreconditionCheckBuilder indexSteps(IndexSteps is) {
    		this.index = is;
    		return this;
    	}

    	public IndexWithPreconditionCheck build() {
			return new IndexWithPreconditionCheck(this);
		}
    	
    }
}
