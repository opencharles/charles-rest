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

import javax.json.JsonObject;

import org.slf4j.Logger;

import com.amihaiemil.charles.steps.IndexSite;
import com.amihaiemil.charles.steps.Step;

/**
 * Step taken by the Github agent when receiving an indexsite command. 
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class IndexSiteSteps implements Step {

	/**
	 * Index site command.
	 */
	private Command com;
	
	/**
	 * Json repository as returned by the Github API.
	 */
	private JsonObject repoJson;
	
	/**
	 * Spoken language.
	 */
	private Language lang;
	
	/**
	 * Action logger.
	 */
	private Logger logger;
	
	/**
	 * Location of the logs.
	 */
	private LogsLocation logs;
	
	/**
	 * Author check step.
	 */
	private Step aoc;
	
	/**
	 * Repo fork check.
	 */
	private Step rfc;
	
	/**
	 * Repo name check step.
	 */
	private Step rpc;
	
	/**
	 * Gh pages branch check step.
	 */
	private Step gpc;

	/**
	 * Star the repo after indexing.
	 */
	private Step sr;
	
    /**
     * Constructor.
     * @param com Command.
     * @param lang Conversation language.
     * @param logger Logger.
     */
    public IndexSiteSteps(IndexSiteStepsBuilder builder) {
        this.com = builder.com;
        this.repoJson = builder.repo;
        this.logger = builder.logger;
        this.logs = builder.logs;
        this.lang = builder.lang;
        this.aoc = builder.authorOwnerStep;
        this.rfc = builder.repoForkCheck;
        this.rpc = builder.repoNameCheck;
        this.gpc = builder.ghPagesBranchCheck;
        this.sr = builder.starRepo;
	}

	/**
	 * Perform the steps necessary to fulfill an indexsite command.<br><br>
	 * 1) Check if the author is owner of the repo and check if repo is not a fork.<br>
	 * 2) Check if repo name matches pattern owner.github.io or repo has a gh-pages branch. <br>
	 * 3) Crawl and index site. <br>
	 * 4) Send email to commander with follow-up data.
	 */
	@Override
	public boolean perform() {
        if(this.aoc.perform()) {
		    if(this.rfc.perform()) {
			    boolean siteRepo = this.rpc.perform();
			    boolean indexed = false;
		        if (siteRepo) {
		    	    indexed = this.indexSiteStep(this.com.authorLogin(), repoJson.getString("name"), false).perform();
		        } else {
		            boolean ghPagesBranch = this.gpc.perform();
		            if(ghPagesBranch) {
		        	    indexed = this.indexSiteStep(this.com.authorLogin(), repoJson.getString("name"), true).perform();
		            } else {
		        	    return this.denialReply("denied.name.comment").perform();
		            }
		           }
		        if(indexed) {
			    	sr.perform();
			    	return this.confirmationReply("index.finished.comment").perform();
			    }
		    } else {
		    	return this.denialReply("denied.fork.comment").perform();
		    }
        } else {
            return this.denialReply("denied.commander.comment").perform();
        }
		return false;
	}

    /**
     * Builds the reply to send to an unauthorized command.
     * @return SendReply step.
     */
    SendReply denialReply(String messagekey) {
        Reply rep = new TextReply(
            com,
            String.format(
         	    lang.response(messagekey),
                com.authorLogin()
         	)
        );
        return new SendReply(rep, logger);
    }
    
    /**
     * Confirmation rely, after the index is finished successfully.
     * @param messageKey Key of the message.
     * @return SendReply step.
     */
    SendReply confirmationReply(String messageKey) {
        return new SendReply(
		    new TextReply(
			   com,
			    String.format(
			       this.lang.response(messageKey),
			       this.com.authorLogin(),
			       this.repoJson.getString("name"),
			       this.logs.address()
			    )
			),
			this.logger
	    );
    }

    /**
     * Return an IndexSite Step for the given repo.
     * @param ownerLogin Username of the repo's owner.
     * @param repoName Repository name.
     * @param ghPages true if the Repo is not a site repository (owner.github.io) but has a gh-pages branch; false otherwise.
     * @return IndexSite instance.
     */
    IndexSite indexSiteStep(String ownerLogin, String repoName, boolean ghPages) {
    	if(!ghPages) {
    		return new IndexSite("http://" + repoName);
    	}
    	return new IndexSite("http://" + ownerLogin + ".github.io/" + repoName);
    }
    
    /**
     * Builder for {@link IndexSiteSteps}
     */
    public static class IndexSiteStepsBuilder {
    	private Command com;
    	private JsonObject repo;
    	private Language lang;
    	private LogsLocation logs;
    	private Logger logger;
    	private Step authorOwnerStep;
    	private Step repoForkCheck;
    	private Step repoNameCheck;
    	private Step ghPagesBranchCheck;
    	private Step starRepo;
    	
    	/**
    	 * Constructor.
    	 * @param com Command that triggered the action.
    	 * @param repo Json repo as returned by the Github API
    	 * @param lang Spoken Language.
    	 * @param logger Action logger.
    	 */
    	public IndexSiteStepsBuilder(
    	    Command com, JsonObject repo,
    	    Language lang, Logger logger, LogsLocation logs
    	) {
    		this.com = com;
    		this.repo = repo;
    		this.lang = lang;
    		this.logger = logger;
    		this.logs = logs;
    	}
    	
    	/**
    	 * Specify the author name check to this builder.
    	 * @param aoc Given author name check.
    	 * @return This builder.
    	 */
    	public IndexSiteStepsBuilder authorOwnerCheck(Step aoc) {
    		this.authorOwnerStep = aoc;
    		return this;
    	}
    	
    	/**
    	 * Specify the repository fork check for this builder.
    	 * @param rfc Given RepositoryForkCheck.
    	 * @return This builder.
    	 */
    	public IndexSiteStepsBuilder repoForkCheck(Step rfc) {
    		this.repoForkCheck = rfc;
    		return this;
    	}
    	
    	/**
    	 * Specify the repository name check to this builder.
    	 * @param rnc Given repository name check.
    	 * @return This builder.
    	 */
    	public IndexSiteStepsBuilder repoNameCheck(Step rnc) {
    		this.repoNameCheck = rnc;
    		return this;
    	}
    	
    	/**
    	 * Specify the gh-pages branch check to this builder.
    	 * @param gpc Given Github pages branch check.
    	 * @return This builder.
    	 */
    	public IndexSiteStepsBuilder ghPagesBranchCheck(Step gpc) {
    		this.ghPagesBranchCheck = gpc;
    		return this;
    	}

    	/**
    	 * Specify the StarRepo step to this builder. (after an index is complete, the repo is starred by the agent)
    	 * @param sr StarRepo step. 
    	 * @return This builder.
    	 */
    	public IndexSiteStepsBuilder starRepo(Step sr) {
            this.starRepo = sr;
            return this;
    	}

    	public IndexSiteSteps build() {
			return new IndexSiteSteps(this);
		}
    	
    }

}
