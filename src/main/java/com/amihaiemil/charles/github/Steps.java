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

import org.slf4j.Logger;


/**
 * Steps taken to fulfill a command.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class Steps implements Step {

    /**
     * Steps to be performed.
     */
    private Step steps;
    
    /**
     * Message to send in case some step fails.
     */
    private SendReply failureMessage;
    
    /**
     * Action's logger.
     */
    private Logger logger;

    /**
     * Constructor.
     * @param steps Given steps.
     * @param log Given logger.
     * @param fm failure message in case any step fails.
     */
    public Steps(Step steps, Logger log, SendReply fm) {
        this.steps = steps;
        this.logger = log;
        this.failureMessage = fm;
    }

    /**
     * Return the steps to perform.
     * @return
     */
    public Step getStepsToPerform() {
        return this.steps;
    }
    
    /**
     * Perform all the given steps.
     */
    @Override
    public void perform() {
        try {
            this.steps.perform();
        } catch (RuntimeException ex) {
            logger.error("A runtime exception occured", ex);
            this.failureMessage.perform();
        }
    }

}
