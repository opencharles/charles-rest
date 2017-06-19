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

import javax.json.JsonObject;

import org.slf4j.Logger;

/**
 * Check for repository fork.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class RepoForkCheck extends PreconditionCheckStep {

    /**
     * Repository json as returned by the Github API.
     */
    private JsonObject repo;

    /**
     * Constructor.
     * @param repo Given repository json.
     * @param onTrue Step that should be performed next if the check is true.
     * @param onFalse Step that should be performed next if the check is false.
     */
    public RepoForkCheck(
        JsonObject repo, Step onTrue, Step onFalse
    ) {
        super(onTrue, onFalse);
        this.repo = repo;
    }

    /**
     * Check whether the repo is a fork or not.
     * @returns true if the repo is NOT a fork, false otherwise.
     */
    @Override
    public void perform(Command command, Logger logger) {
        logger.info("Checking whether the repository is a fork...");
        boolean fork = this.repo.getBoolean("fork");
        if(fork) {
            logger.warn("Repository should NOT be a fork!");
            this.onFalse().perform(command, logger);
        } else {
            logger.info("Repository is not a fork - Ok!");
            this.onTrue().perform(command, logger);
        }
    }
}
