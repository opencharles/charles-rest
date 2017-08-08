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
 * Step where it is checked if a repo has the gh-pages branch or not.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $id$
 * @since 1.0.0
 *
 */
public class GhPagesBranchCheck extends PreconditionCheckStep {

    /**
     * Constructor.
     * @param onTrue Step that should be performed next if the check is true.
     * @param onFalse Step that should be performed next if the check is false.
     */
    public GhPagesBranchCheck(Step onTrue, Step onFalse) {
        super(onTrue, onFalse);
    }

    /**
     * Perform this step.
     */
    @Override
    public void perform(Command command, Logger logger) throws IOException {
        logger.info("Checking whether the repository has a gh-pages branch...");
        try {
            
            if (command.repo().hasGhPagesBranch()) {
                logger.info("The repo has a gh-pages branch - OK!");
                this.onTrue().perform(command, logger);
            } else {
                logger.info("The repo does NOT have a gh-pages branch");
                this.onFalse().perform(command, logger);
            }
        } catch (IOException e) {
            logger.error("Exception when checking if gh-pages branch exists", e);
            throw new IllegalStateException("Exception when checking if gh-pages branch exists", e);
        }
    }

}
