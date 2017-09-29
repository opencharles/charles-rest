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

/**
 * The bot knows how to delete an index.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
public final class DeleteIndexKn implements Knowledge {

    /**
     * What do we do if it's not an 'deleteindex' command?
     */
    private Knowledge notDelete;

    /**
     * Ctor.
     * @param notDelete What do we do if it's not an 'deleteindex' command?
     */
    public DeleteIndexKn(final Knowledge notDelete) {
        this.notDelete = notDelete;
    }

    @Override
    public Steps start(final Command com, final LogsLocation logs) throws IOException {
        if("deleteindex".equalsIgnoreCase(com.type())) {
            return new StepsTree(
	            new DeleteIndexCommandCheck(
	                new IndexExistsCheck(
	                    com.indexName(),
	                    new GeneralPreconditionsCheck(
	                        new DeleteIndex(
	                            new SendReply(
	                                new TextReply(
	                                    com,
	                                    String.format(
	                                        com.language().response("deleteindex.finished.comment"),
	                                        com.authorLogin(), com.repo().name(), logs.address()
	                                    )
	                                ),
	                                new Follow(new Tweet(new Step.FinalStep()))
	                            )
	                        )
	                    ),
	                    new SendReply(
	                        new TextReply(
	                            com,
	                            String.format(
	                                com.language().response("index.missing.comment"),
	                                com.authorLogin(), logs.address()
	                            )
	                        ),
	                        new Step.FinalStep()
	                    )
	                ),
	                new SendReply(
	                    new TextReply(
	                        com,
	                        String.format(
	                            com.language().response("denied.deleteindex.comment"),
	                            com.authorLogin(), com.agentLogin(), com.repo().name()
	                        )
	                    ),
	                    new Step.FinalStep()
	                )
	            ),
	            com,
	            logs
	        );
        }
        return this.notDelete.start(com, logs);
    }

}
