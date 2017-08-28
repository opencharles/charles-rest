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
import java.net.HttpURLConnection;
import org.slf4j.Logger;
import com.jcabi.github.Github;
import com.jcabi.http.Request;

/**
 * The bot can follow the commander.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
public final class Follow extends IntermediaryStep {

    /**
     * Ctor.
     * @param next Next step to execute.
     */
    public Follow(Step next) {
        super(next);
    }

    @Override
    public void perform(Command command, Logger logger) throws IOException {
        final String author = command.authorLogin();
        final Github github = command.issue().repo().github();
        final Request follow = github.entry()
            .uri().path("/user/following/").path(author).back()
            .method("PUT");
        logger.info("Following Github user " + author + " ...");
        try {
            final int status = follow.fetch().status();
            if(status != HttpURLConnection.HTTP_NO_CONTENT) {
                logger.error("User follow status response is " + status + " . Should have been 204 (NO CONTENT)");
            } else {
                logger.info("Followed user " + author + " .");
            }
        } catch (final IOException ex) {//don't rethrow, this is just a cosmetic step, not critical.
            logger.error("IOException while trying to follow the user.");
        }
        this.next().perform(command, logger);
    }

}
