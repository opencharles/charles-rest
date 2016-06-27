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

import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateful;

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
	 * @return list of Steps.
	 */
     public List<Step> understand(Command com) {
	     String authorLogin = com.json().getJsonObject("user").getString("login");
    	 List<Step> steps = new LinkedList<Step>();
    	 CommandCategory category = new CommandCategory("unknown", languages.get(0));
    	 for(Language l : languages) {
    		 category = l.categorize(com);
    		 if(category.isUnderstood()) {
    			 break;
    		 }
    	 }
    	 switch (category.type()) {
    	 	case "hello":
    	 		String hello = String.format(category.language().response("hello.comment"), "@" + authorLogin);
    	 		steps.add(
    	 			new SendReply(
    	 				new TextReply(com, hello)
    	 			)
    	 		);
    	 		break;
    	 	case "indexsite":
    	 		break;
    	 	case "indexpage":
    	 		break;
    	 	default:
    	 		String unknown = String.format(
    	 			category.language().response("unknown.comment"),
    	 			"@" + authorLogin);
    	 		steps.add(
        	 		new SendReply(
            	 		new TextReply(com, unknown)
            	 	)
    	 		);
    	 		break;
		 }
    	 return steps;
     }
}
