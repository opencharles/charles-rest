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
 * The bot knows how to index a website based on its sitemap.xml
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
public final class IndexSitemapKn implements Knowledge {
    
    /**
     * Location of the log file.
     */
    private LogsLocation logsLoc;

    /**
     * What do we do if it's not an 'indexsitemap' command?
     */
    private Knowledge notIdxSitemap;

    /**
     * Ctor.
     * @param logsLoc Location of the log file for the bot's action.
     * @param notIdxSitemap What do we do if it's not an 'indexpage' command?
     */
    public IndexSitemapKn(final LogsLocation logsLoc, final Knowledge notIdxSitemap) {
        this.logsLoc = logsLoc;
        this.notIdxSitemap = notIdxSitemap;
    }

    @Override
    public Step handle(final Command com) throws IOException {
        if("indexsitemap".equalsIgnoreCase(com.type())) {
            return new PageHostedOnGithubCheck(
                new GeneralPreconditionsCheck(
                    new SendReply(
                        new TextReply(
                            com,
                            String.format(
                                com.language().response("index.start.comment"),
                                com.authorLogin(), this.logsLoc.address()
                            )
                        ),
                        new IndexSitemap(
                            new StarRepo(
                                new SendReply(
                                    new TextReply(
                                        com,
                                        String.format(
                                            com.language().response("index.finished.comment"),
                                            com.authorLogin(), this.logsLoc.address()
                                        )
                                    ),
                                    new Step.FinalStep()
                                )
                            )
                        )
                    )
                ),
                new SendReply(
                    new TextReply(
                        com,
                        String.format(
                            com.language().response("denied.badlink.comment"),
                            com.authorLogin()
                        )
                    ),
                    new Step.FinalStep()
                )
            );
        }
        return this.notIdxSitemap.handle(com);
    }
}
