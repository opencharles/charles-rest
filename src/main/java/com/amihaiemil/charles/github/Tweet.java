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

import java.io.IOException;
import org.slf4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * The bot can tweet at the end of each action.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
public final class Tweet extends IntermediaryStep {

	/**
	 * Ctor.
	 * @param next Next step to execute.
	 */
    public Tweet(final Step next) {
        super(next);
    }
    
    @Override
    public void perform(Command command, Logger logger) throws IOException {
    	if(command.repo().charlesYml().tweet()) {
		    try {
		    	final Twitter twitter = twitter();
		    	if(twitter != null) {
		    		String message = message(command);
		    		if(!message.isEmpty()) {
			    	    twitter.updateStatus(message);
		    		} else {
		    			logger.warn("Could not built message for twitter. Not twitting anything.");
		    		}
		    	} else {
		    		logger.warn("One of the 4 tweeter system properties is missing! Cannot tweet!");
		    	}
		    } catch (final Exception ex) {//don't rethrow it, tweeting is only a cosmetic thing, not critical..
		        logger.error("Failed to tweet... ", ex);
		    }
    	} else {
    		logger.info("Tweeting is disabled, won't tweet. You can enable tweeting via .charles.yml file.");
    	}
        this.next().perform(command, logger);
    }

    /**
     * Build the message to tweet.
     * @param com Command.
     * @return String message
     * @throws IOException if the message cannot be built.
     */
    private static String message(final Command com) throws IOException {
    	final String unformatted = com.language().response("tweet." + com.type());
    	final String issueUrl = com.issue().json().getString("url", "");
    	if(unformatted == null || unformatted.isEmpty() || issueUrl.isEmpty()) {
    		return "";
    	}
    	return String.format(unformatted, issueUrl);
    }
    
    /**
     * Get a {@link Twitter} instance.
     */
    private static Twitter twitter() {
    	final String key = System.getProperty("twitter.key", "");
    	final String secret = System.getProperty("twitter.secret", "");
    	final String token  = System.getProperty("twitter.token", "");
    	final String tsecret = System.getProperty("twitter.token.secret", "");
    	
    	if(key.isEmpty() || secret.isEmpty() || token.isEmpty() || tsecret.isEmpty()) {
    		return null;
    	} else {
    		final TwitterFactory factory = new TwitterFactory();
        	final Twitter twitter = factory.getInstance();
            twitter.setOAuthConsumer(key, secret);
            twitter.setOAuthAccessToken(new AccessToken(token, tsecret));
            return twitter;
    	}

    }
}
