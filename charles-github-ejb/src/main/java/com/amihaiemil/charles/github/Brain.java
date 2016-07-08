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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateful;
import javax.json.JsonObject;

import org.slf4j.Logger;

import com.amihaiemil.charles.steps.Step;

/**
 * The "brain" of the Github agent. Can understand commands and 
 * figure out the Steps that need to be performed to fulfill the 
 * command.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
@Stateful
public class Brain {
	
	private List<Language> languages = new LinkedList<Language>();
	
	/**
	 * Constructor.
	 */
	public Brain() {
		this.languages.add(new English());
	}
	
	/**
	 * Constructor which takes the responses and languages.
	 * @param resp
	 * @param langs
	 */
	public Brain(List<Language> langs) {
		this.languages = langs;
	}
	
	/**
	 * Understand a command.
	 * @param com Given command.
	 * @param logger Action logger.
	 * @param logs Location of the logs.
	 * @return Steps.
	 * @throws IOException if something goes worng.
	 */
     public Steps understand(Command com, Logger logger, LogsLocation logs) throws IOException {
    	 String authorLogin = com.authorLogin();
	     logger.info("Command author's login: " + authorLogin);
    	 List<Step> steps = new LinkedList<Step>();
    	 CommandCategory category = new CommandCategory("unknown", languages.get(0));
    	 for(Language l : languages) {
    		 category = l.categorize(com);
    		 if(category.isUnderstood()) {
    			 logger.info("Command type: " + category.type() + ". Language: " + l.getClass().getName());
    			 break;
    		 }
    	 }
    	 switch (category.type()) {
    	 	case "hello":
    	 		String hello = String.format(category.language().response("hello.comment"), authorLogin);
    	 		logger.info("Prepared response: " + hello);
    	 		steps.add(
    	 			new SendReply(
    	 				new TextReply(com, hello),
    	 				logger
    	 			)
    	 		);
    	 		break;
    	 	case "indexsite":
    	 		steps.addAll(this.indexSiteSteps(com, category, logger, logs));
    	 		break;
    	 	case "indexpage":
    	 		break;
    	 	default:
    	 		logger.info("Unknwon command!");
    	 		String unknown = String.format(
    	 			category.language().response("unknown.comment"),
    	 			authorLogin);
    	 		logger.info("Prepared response: " + unknown);
    	 		steps.add(
        	 		new SendReply(
            	 		new TextReply(com, unknown),
            	 		logger
            	 	)
    	 		);
    	 		break;
		 }
    	 return new Steps(
    	     steps,
    	     new SendReply(
    	         new TextReply(
    	             com,
    	             String.format(
    	                 category.language().response("step.failure.comment"),
    	                 com.authorLogin(),
    	                 logs.address()
    	             )
    	         ),
    	         logger
    	     )
    	 );
     }
     
    private List<Step> indexSiteSteps(
        Command com, CommandCategory category, Logger logger, LogsLocation logs
    ) throws IOException{
        List<Step> steps = new ArrayList<Step>();
		steps.add(
		    new SendReply(
		        new TextReply(
		            com,
		            String.format(
		                category.language().response("index.start.comment"),
		                com.authorLogin()
		            )
		        ),
		        logger
		    )		
		);

		JsonObject repo = com.issue().repo().json();

		steps.add(
		    new IndexSiteSteps.IndexSiteStepsBuilder(
		        com, repo, category.language(), logger
		    )
		    .repoForkCheck(new RepoForkCheck(repo, logger))
			.authorOwnerCheck(new AuthorOwnerCheck(com, repo, logger))
			.repoNameCheck(new RepoNameCheck(repo, logger))
			.ghPagesBranchCheck(new GhPagesBranchCheck(repo, logger))
			.starRepo(new StarRepo(com.issue().repo(), logger))
			.build()
		);
		
		steps.add(
		    new SendReply(
			    new TextReply(
			       com,
			       String.format(
			           category.language().response("index.finished.comment"),
			           com.authorLogin(),
			           repo.getString("name"),
			           logs.address()
			       )
			    ),
			    logger
			)		
		);
		return steps;
     }
}
