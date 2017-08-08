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
import java.io.IOException;

/**
 * The general preconditions for any command other than 'hello' are:
 *
 * 1) Repo is under the commander's name or commander is an active admin of the
 *    organization. This rule does not apply if the commander is specified in .charles.yml;
 * 2) Repo is not a fork;
 * 3) Repo is a website (is named commander.github.io) or has a gh-pages branch;
 *
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
public final class GeneralPreconditionsCheck extends PreconditionCheckStep {

    /**
     * Ctor.
     * @param onTrue Step that should be performed next if the check is true.
     * @param onFalse Step that should be performed next if the check is false.
     */
    public GeneralPreconditionsCheck(
        final Step onTrue, final Step onFalse
    ) {
        super(onTrue, onFalse);
    }

    @Override
    public void perform(Command command, Logger logger) throws IOException {
        final PreconditionCheckStep all;

        final PreconditionCheckStep repoForkCheck = new RepoForkCheck(
            command.repo().json(), this.onTrue(),
            this.finalCommentStep(command, "denied.fork.comment", command.authorLogin())
        );

        if(!this.isCommanderAllowed(command)) {
            final PreconditionCheckStep authorOwnerCheck = new AuthorOwnerCheck(
                    repoForkCheck,
                    new OrganizationAdminCheck(
                        repoForkCheck,
                        this.finalCommentStep(command, "denied.commander.comment", command.authorLogin())
                    )
            );
            all = new RepoNameCheck(
                command.repo().json(), authorOwnerCheck,
                new GhPagesBranchCheck(
                    authorOwnerCheck,
                    this.finalCommentStep(command, "denied.name.comment", command.authorLogin())
                )
            );
        } else {
            all = new RepoNameCheck(
                command.repo().json(), repoForkCheck,
                new GhPagesBranchCheck(
                    repoForkCheck,
                    this.finalCommentStep(command, "denied.name.comment", command.authorLogin())
                )
            );
        }
        all.perform(command, logger);
    }

    /**
     * Builds the final comment to be sent to the issue.
     * <b>This should be the last in the steps' chain</b>.
     * @param com Command.
     * @param formatParts Parts to format the response %s elements with.
     * @return SendReply step.
     */
    private SendReply finalCommentStep(
        Command com, String messagekey, String ... formatParts
    ) {
        return new SendReply(
            new TextReply(
                com,
                String.format(
                    com.language().response(messagekey),
                    (Object[]) formatParts
                )
            ),
            new Step.FinalStep()
        );
    }

    /**
     * Is the command's author specified in .charles.yml as a commander?
     * @param com Initial command.
     * @throws IOException if the commanders' list cannot be read from Github.
     * @return True of False
     */
    private boolean isCommanderAllowed(Command com) throws IOException {
        for(String commander : com.repo().charlesYml().commanders()) {
            if(commander.equalsIgnoreCase(com.authorLogin())) {
                return true;
            }
        }
        return false;
    }
}
