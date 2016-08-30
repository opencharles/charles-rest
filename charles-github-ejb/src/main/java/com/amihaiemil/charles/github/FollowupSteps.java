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

import com.amihaiemil.charles.steps.Step;

/**
 * Step that performs command follow up actions, like sending a confirmation comment and starring the repo.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
class FollowupSteps implements Step {

	private Step starRepo = new Step() {
		@Override
		public boolean perform() {
			return true;
		}
	};

	private Step sendConfirmationReply = new Step() {
		@Override
		public boolean perform() {
			return true;
		}
	};

	/**
	 * Ctor for builder pattern.
	 * @param builder IndexFollowupStepsBuilder
	 */
	private FollowupSteps(FollowupStepsBuilder builder) {
	    this.starRepo = builder.sr;
	    this.sendConfirmationReply = builder.cr;
	}

    /**
     * Perform this step.
     */
    @Override
    public boolean perform() {
        this.starRepo.perform();
        return this.sendConfirmationReply.perform();
    }

    public static class FollowupStepsBuilder {

		/**
		 * Star repo step.
		 */
    	private Step sr;

    	/**
    	 * Confirmation reply step.
    	 */
    	private Step cr;

    	/**
    	 * Specify star repo step to this builder.
    	 * @param sr Given star repo step.
    	 * @return This builder.
    	 */
    	public FollowupStepsBuilder starRepo(StarRepo sr) {
    		this.sr = sr;
    		return this;
    	}
    	
    	/**
    	 * Specify the confirmation reply step.
    	 * @param cr Given confirmation reply.
    	 * @return This builder.
    	 */
    	public FollowupStepsBuilder confirmationReply(SendReply cr) {
    		this.cr = cr;
    		return this;
    	}

    	public FollowupSteps build() {
			return new FollowupSteps(this);
		}
    	
    }

}
