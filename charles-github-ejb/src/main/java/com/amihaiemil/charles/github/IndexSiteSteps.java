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

import org.slf4j.Logger;

import com.amihaiemil.charles.steps.IndexSite;
import com.amihaiemil.charles.steps.SendEmail;
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
	 * Spoken language.
	 */
	private Language lang;
	
	/**
	 * Action logger.
	 */
	private Logger logger;
	
	/**
	 * Constructor.
	 * @param com Command.
	 * @param lang Conversation language.
	 * @param logger Logger.
	 */
	public IndexSiteSteps(Command com, Language lang, Logger logger) {
		this.com = com;
		this.logger = logger;
		this.lang = lang;
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
		AuthorOwnerCheck aoc = new AuthorOwnerCheck(com, logger);
		if(aoc.perform()) {
		    boolean siteRepo = new RepoNameCheck(this.com, this.logger).perform();
	    	IndexSite is = new IndexSite();
		    if (siteRepo) {
		    	if(is.perform()) {
                    SendEmail se = new SendEmail(null, null);
                    se.perform();
		    	}
		    } else {
		    	// maybe it has a gh-pages branch
		    }
        } else {
            return this.replyForUnauthorizedCommander().perform();
        }
		return false;
	}

    /**
     * Builds the reply to send to an unauthorized commander.
     * @return SendReply step.
     */
    public SendReply replyForUnauthorizedCommander() {
        Reply rep = new TextReply(
            com,
            String.format(
         	    lang.response("denied.comment"),
                "@" + com.authorLogin()
         	)
        );
        return new SendReply(rep, logger);
    }

}
