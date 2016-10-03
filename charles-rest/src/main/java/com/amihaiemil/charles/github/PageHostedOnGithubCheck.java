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
 * Checks that a given page is hosted on Github (has the right domain)
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class PageHostedOnGithubCheck implements Step {

	/**
	 * Command.
	 */
	private Command com;

	/**
	 * Link to the page.
	 */
	private String link;
	
	/**
	 * Action's logger;
	 */
	private Logger logger;
	
	/**
	 * Ctor
	 * @param repo Json repo.
	 * @param link Page link.
	 * @param logger Logger.
	 */
	public PageHostedOnGithubCheck(Command com, String link, Logger logger) {
		this.com = com;
		this.link = link;
		this.logger = logger;
	}

	@Override
	public boolean perform() {
		try {
		    JsonObject repo = com.repo().json();
		    String owner = repo.getJsonObject("owner").getString("login");
		    String expDomain;
		    logger.info("Checking if the page belongs to the repo " + owner + "/" + repo.getString("name"));
		    logger.info("Page link: " + this.link);
		    boolean ghPagesBranch = com.repo().hasGhPagesBranch();
		    if (ghPagesBranch) {
			    expDomain = owner + ".github.io/" + repo.getString("name");
			    logger.info("The repo has a gh-pages branch so the page link has to start with " + expDomain);
		    } else {
			    expDomain = owner + ".github.io";
	            logger.info("The repo is a website repo so the page link has to start with " + expDomain);
		    }
		    boolean passed = this.link.startsWith("http://" + expDomain) || this.link.startsWith("https://" + expDomain);
		    if(!passed) {
			    logger.warn("The given webpage is not part of this repository!");
		    }
            return passed;
		} catch (IOException ex) {
            logger.error("IOException when calling the Github API", ex);
            throw new IllegalStateException("IOException when calling the Github API", ex);
        }
    }

}
