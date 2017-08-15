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
 * The "brain" of the Github agent. Can understand commands and 
 * figure out the Steps that need to be performed to fulfill the 
 * command.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * 
 */
public class Brain {

    /**
     * All the languages that the chatbot can understang/speaks.
     */
    private Language[] languages;

    /**
     * The action's logger.
     */
    private Logger logger;

    /**
     * Location of the log file.
     */
    private LogsLocation logsLoc;

    /**
     * Constructor.
     */
    public Brain(Logger logger, LogsLocation logsLoc) {
        this(logger, logsLoc, new English());
    }

    /**
     * Constructor which takes the responses and languages.
     * @param resp
     * @param langs
     */
    public Brain(Logger logger, LogsLocation logsLoc, Language... langs) {
        this.logger = logger;
        this.logsLoc = logsLoc;
        this.languages = langs;
    }

    /**
     * Understand a command.
     * @param com Given command.
     * @return Steps.
     * @throws IOException if something goes worng.
     */
    public Steps understand(Command com) throws IOException {
         String authorLogin = com.authorLogin();
         logger.info("Command author's login: " + authorLogin);
         Step steps;
         String category = this.categorizeCommand(com);
         switch (category) {
             case "hello":
                 String hello = String.format(new English().response("hello.comment"), authorLogin);
                 steps = new SendReply(
                     new TextReply(com, hello), new Step.FinalStep()
                 );
                 break;
             case "indexsite":
                 steps = this.withCommonChecks(
                     com, new English(), this.indexSiteStep(com,new English())
                 );
                 break;
             case "indexpage":
                 steps = new PageHostedOnGithubCheck(
                     this.withCommonChecks(
                         com, new English(), this.indexPageStep(com, new English())
                     ),
                     this.finalCommentStep(com, new English(), "denied.badlink.comment", com.authorLogin())
                 );
                 break;
             case "indexsitemap":
            	 steps = new PageHostedOnGithubCheck(
                     this.withCommonChecks(
                         com, new English(),
                         this.indexSitemapStep(com, new English())
                     ),
                     this.finalCommentStep(
                         com, new English(),
                         "denied.badlink.comment",
                         com.authorLogin()
                     )
                 );
            	 break;
             case "deleteindex":
                 steps = new DeleteIndexCommandCheck(
                     new IndexExistsCheck(
                         com.indexName(),
                         new AuthorOwnerCheck(
                             this.deleteIndexStep(com, new English()),
                             new OrganizationAdminCheck(
                                 this.deleteIndexStep(com, new English()),
                                 this.finalCommentStep(
                                     com, new English(),
                                     "denied.commander.comment", com.authorLogin()
                                 )
                             )
                         ),
                         this.finalCommentStep(
                             com, new English(), "index.missing.comment",
                             com.authorLogin(),
                             this.logsLoc.address()
                         )
                     ),
                     this.finalCommentStep(
                         com, new English(), "denied.deleteindex.comment",
                         com.authorLogin(), com.agentLogin(), com.repo().name()
                     )
                 );
                 break;
             default:
                 logger.info("Unknwon command!");
                 String unknown = String.format(
                     new English().response("unknown.comment"),
                     authorLogin);
                 steps = new SendReply(
                            new TextReply(com, unknown),
                            new Step.FinalStep()
                        );
                 break;
         }
         return new Steps(
             steps,
             new SendReply(
                 new TextReply(
                     com,
                     String.format(
                         new English().response("step.failure.comment"),
                         com.authorLogin(), this.logsLoc.address()
                     )
                 ),
                 new Step.FinalStep("[ERROR] Some step didn't execute properly.")
             )
         );
    }

    /**
     * Steps for indexpage step.
     * @param com Command
     * @param lang Language
     * @return Step
     * @throws IOException 
     */
    public Step indexPageStep(Command com, Language lang) throws IOException {
    	return new SendReply(
            new TextReply(
                com,
                String.format(
                    lang.response("index.start.comment"),
                    com.authorLogin(),
                    this.logsLoc.address()
                )
            ),
            new IndexPage(
                new StarRepo(
                    this.finalCommentStep(
                        com, lang, "index.finished.comment",
                        com.authorLogin(),
                        this.logsLoc.address()
                    )
                )
            )
        );
    }
    
    /**
     * Steps for indexsitemap step.
     * @param com Command
     * @param lang Language
     * @return Step
     * @throws IOException 
     */
    public Step indexSitemapStep(Command com, Language lang) throws IOException {
    	return new SendReply(
            new TextReply(
                com,
                String.format(
                    lang.response("index.start.comment"),
                    com.authorLogin(),
                    this.logsLoc.address()
                )
            ),
            new IndexSitemap(
                new StarRepo(
                    this.finalCommentStep(
                        com, lang, "index.finished.comment",
                        com.authorLogin(),
                        this.logsLoc.address()
                    )
                )
            )
        );
    }

    /**
     * Steps for indexsite action
     * @param com Command
     * @param lang Language
     * @return Step
     * @throws IOException 
     */
    public Step indexSiteStep(Command com, Language lang) throws IOException {
        return new SendReply(
            new TextReply(
                com,
                String.format(
                    lang.response("index.start.comment"),
                    com.authorLogin(),
                    this.logsLoc.address()
                )
            ),
            new IndexSite(
                new StarRepo(
                    this.finalCommentStep(
                        com, lang, "index.finished.comment",
                        com.authorLogin(),
                        this.logsLoc.address()
                    )
                )
            )
        );
    }
    
    /**
     * Steps for deleteindex action.
     * @param com Command
     * @param lang Language
     * @return Step
     * @throws IOException
     */
    public Step deleteIndexStep(Command com, Language lang) throws IOException {
        return  new DeleteIndex(
                this.finalCommentStep(
                    com, lang, "deleteindex.finished.comment",
                    com.authorLogin(),
                    com.repo().name(),
                    this.logsLoc.address()
                )
            );
    }

    /**
     * Find out the type and Language of a command.
     * @param com Received Command.
     * @param logger Logger to use.
     * @return CommandCategory, which defaults to unknown command and
     *  first language in the agent's languages list (this.languages)
     * @throws IOException 
     */
    private String categorizeCommand(Command com) throws IOException {
    	String category = "unknown";
        for(Language lang : this.languages) {
            category = lang.categorize(com);
            if(!"unknown".equals(category)) {
                this.logger.info(
                    "Command type: " + category +
                    ". Language: " + lang.getClass().getSimpleName()
                );
                break;
            }
        }
        return category;
    }

    /**
     * Add precondition checks that are common to most actions (indexsite, indexpage, deleteindex etc)
     * @param com Received Command,
     * @param lang Spoken language.
     * @param action Step that should be performed after the checks are met.
     * @return Steps that have to be followed to fulfill an index command.
     * @throws IOException If something goes wrong.
     */
    private Step withCommonChecks(Command com, Language lang, Step action) throws IOException {
        PreconditionCheckStep repoForkCheck = new RepoForkCheck(
            action,
            this.finalCommentStep(com, lang, "denied.fork.comment", com.authorLogin())
        );
        PreconditionCheckStep authorOwnerCheck = new AuthorOwnerCheck(
            repoForkCheck,
            new OrganizationAdminCheck(
                repoForkCheck,
                this.finalCommentStep(com, lang, "denied.commander.comment", com.authorLogin())
            )
        );
        PreconditionCheckStep repoNameCheck = new RepoNameCheck(
            authorOwnerCheck,
            new GhPagesBranchCheck(
                authorOwnerCheck,
                this.finalCommentStep(com, lang, "denied.name.comment", com.authorLogin())
            )
        );        
        return repoNameCheck;
    }

    /**
     * Builds the final comment to be sent to the issue. <b>This should be the last in the steps' chain</b>.
     * @param com Command.
     * @param lang Spoken language.
     * @param formatParts Parts to format the response %s elements with.
     * @return SendReply step.
     */
    private SendReply finalCommentStep(
        Command com, Language lang, String messagekey, String ... formatParts
    ) {
        return new SendReply(
            new TextReply(
        	    com,
        	    String.format(
        		    lang.response(messagekey),
        		    (Object[]) formatParts
        		)
            ),
            new Step.FinalStep()
        );
    }

}
