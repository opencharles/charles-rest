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

/**
 * Step where it's checked if the repo is under the author's name.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $id$
 * @since 1.0.0
 *
 */
public class AuthorOwnerCheck extends PreconditionCheckStep {

    /**
     * Given command;
     */
    private Command com;

    /**
     * Logger of the action.
     */
    private Logger logger;

    /**
     * Constructor.
     * @param com Command.
     * @param logger Action logger.
     * @param onTrue Step that should be performed next if the check is true.
     * @param onFalse Step that should be performed next if the check is false.
     */
    public AuthorOwnerCheck(
        Command com, Logger logger,
        Step onTrue, Step onFalse
    ) {
        super(onTrue, onFalse);
        this.com = com;
        this.logger = logger;
    }

    /**
     * Check that the author of a command is owner of the repo.
     * @return true if the check is successful, false otherwise
     */
    @Override
    public void perform() {
        logger.info("Checking ownership of the repo");
        try {
            String repoOwner = this.com.repo().json().getJsonObject("owner").getString("login");
            String author = this.com.authorLogin();
            if(repoOwner.equals(author)) {
                logger.info("Commander is repo owner - OK");
                this.onTrue().perform();
            } else {
                logger.warn("Commander is NOT repo owner");
                this.onFalse().perform();
            }
        } catch (IOException ex) {
            logger.error("IOException when fetching repo owner from Github API", ex);
            throw new IllegalStateException("IOException when fetching repo owner!", ex);
        }
    }
    
}
