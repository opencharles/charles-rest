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

import com.amihaiemil.charles.steps.Step;

/**
 * Step that performs all the precondition checks that have to be met in order to execute
 * any action related to the index.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
class IndexPreconditionCheck implements Step {

	private Command com;
	private Language lang;
	private Logger logger;
	/**
	 * Checks that can be performed as a precondition.
	 */
	private Step authorOwnerStep = new Step() {
		@Override
		public boolean perform() {
			return true;
		}
	};
	
	private Step repoForkCheck = new Step() {
		@Override
		public boolean perform() {
			return true;
		}
	};
	
	private Step repoNameCheck = new Step() {
		@Override
		public boolean perform() {
			return true;
		}
	};
	
	private Step ghPagesBranchCheck = new Step() {
		@Override
		public boolean perform() {
			return true;
		}
	};

	/**
	 * Builder pattern ctor.
	 * @param builder
	 */
	IndexPreconditionCheck(IndexPreconditionCheckBuilder builder) {
	    this.com = builder.com;
	    this.lang = builder.lang;
	    this.logger = builder.logger;
	    this.authorOwnerStep = builder.authorOwnerStep;
	    this.repoForkCheck = builder.repoForkCheck;
	    this.repoNameCheck = builder.repoNameCheck;
	    this.ghPagesBranchCheck = builder.ghPagesBranchCheck;
	}

	@Override
	public boolean perform() {
		if(this.authorOwnerStep.perform()) {
		    if(this.repoForkCheck.perform()) {
		        if (this.repoNameCheck.perform()) {
		    	    return true;
		        } else {
		            boolean ghPagesBranch = this.ghPagesBranchCheck.perform();
		            if(ghPagesBranch) {
		                return true;
		            } else {
		        	    this.denialReply("denied.name.comment").perform();
		            }
		        }
		    } else {
		    	this.denialReply("denied.fork.comment").perform();
		    }
        } else {
            this.denialReply("denied.commander.comment").perform();
        }
		return false;
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
     * Builder for {@link IndexPreconditionCheck}
     */
    public static class IndexPreconditionCheckBuilder {
    	private Command com;
    	private Language lang;
    	private Logger logger;
    	private Step authorOwnerStep;
    	private Step repoForkCheck;
    	private Step repoNameCheck;
    	private Step ghPagesBranchCheck;
    	
    	/**
    	 * Constructor.
    	 * @param com Command that triggered the action.
    	 * @param repo Json repo as returned by the Github API
    	 * @param lang Spoken Language.
    	 * @param logger Action logger.
    	 */
    	public IndexPreconditionCheckBuilder(
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
    	public IndexPreconditionCheckBuilder authorOwnerCheck(AuthorOwnerCheck aoc) {
    		this.authorOwnerStep = aoc;
    		return this;
    	}
    	
    	/**
    	 * Specify the repository fork check for this builder.
    	 * @param rfc Given RepositoryForkCheck.
    	 * @return This builder.
    	 */
    	public IndexPreconditionCheckBuilder repoForkCheck(RepoForkCheck rfc) {
    		this.repoForkCheck = rfc;
    		return this;
    	}
    	
    	/**
    	 * Specify the repository name check to this builder.
    	 * @param rnc Given repository name check.
    	 * @return This builder.
    	 */
    	public IndexPreconditionCheckBuilder repoNameCheck(RepoNameCheck rnc) {    			
    		this.repoNameCheck = rnc;
    		return this;
    	}
    	
    	/**
    	 * Specify the gh-pages branch check to this builder.
    	 * @param gpc Given Github pages branch check.
    	 * @return This builder.
    	 */
    	public IndexPreconditionCheckBuilder ghPagesBranchCheck(GhPagesBranchCheck gpc) {
    		this.ghPagesBranchCheck = gpc;
    		return this;
    	}

    	public IndexPreconditionCheck build() {
			return new IndexPreconditionCheck(this);
		}
    	
    }
}
