/**
 * Copyright (c) 2016, Mihai Emil Andronache
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
    private List<Language> languages = new LinkedList<Language>();

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
        this(logger, logsLoc, Arrays.asList((Language) new English()));
    }

    /**
     * Constructor which takes the responses and languages.
     * @param resp
     * @param langs
     */
    public Brain(Logger logger, LogsLocation logsLoc, List<Language> langs) {
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
         CommandCategory category = this.categorizeCommand(com);
         switch (category.type()) {
             case "hello":
                 String hello = String.format(category.language().response("hello.comment"), authorLogin);
                 steps = new SendReply(
                             new TextReply(com, hello),
                             this.logger,
                             new Step.FinalStep(this.logger)
                         );
                 break;
             case "indexsite":
                 steps = this.withCommonChecks(
                     com, category.language(), this.indexSiteStep(com, category.language())
                 );
                 break;
             case "indexpage":
                 steps = new PageHostedOnGithubCheck(
                     com, this.logger,
                     this.withCommonChecks(
                         com, category.language(), this.indexPageStep(com, category.language())
                     ),
                     this.finalCommentStep(com, category.language(), "denied.badlink.comment", com.authorLogin())
                 );
                 break;
             case "deleteindex":
                 steps = new DeleteIndexCommandCheck(
                     com, this.logger,
                     new IndexExistsCheck(
                         com.indexName(), this.logger,
                         this.withCommonChecks(
                             com, category.language(),
                             this.deleteIndexStep(com, category.language())
                         ), 
                         this.finalCommentStep(
                             com, category.language(), "index.missing.comment",
                             com.authorLogin(),
                             this.logsLoc.address()
                         )
                     ),
                     this.finalCommentStep(
                         com, category.language(), "denied.deleteindex.comment",
                         com.authorLogin(), com.agentLogin(), com.repo().name()
                     )
                 );
                 break;
             default:
                 logger.info("Unknwon command!");
                 String unknown = String.format(
                     category.language().response("unknown.comment"),
                     authorLogin);
                 steps = new SendReply(
                            new TextReply(com, unknown),
                            this.logger,
                            new Step.FinalStep(this.logger)
                        );
                 break;
         }
         return new Steps(
             steps,
             new SendReply(
                 new TextReply(
                     com,
                     String.format(
                         category.language().response("step.failure.comment"),
                         com.authorLogin(), this.logsLoc.address()
                     )
                 ),
                 this.logger,
                 new Step.FinalStep(
                     this.logger,
                     "[ERROR] Some step didn't execute properly."
                 )
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
            ), this.logger,
            new IndexPage(
                com, this.logger,
                new StarRepo(
                    com.issue().repo(), this.logger,
                    this.finalCommentStep(
                        com, lang, "index.finished.comment",
                        com.authorLogin(),
                        com.repo().name(),
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
            ), this.logger,
            new IndexSite(
                com, logger,
                new StarRepo(
                    com.issue().repo(), this.logger,
                    this.finalCommentStep(
                        com, lang, "index.finished.comment",
                        com.authorLogin(),
                        com.repo().name(),
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
                com, logger,
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
    private CommandCategory categorizeCommand(Command com) throws IOException {
        CommandCategory category = new CommandCategory("unknown", languages.get(0));
           for(Language l : languages) {
               category = l.categorize(com);
               if(category.isUnderstood()) {
                   this.logger.info("Command type: " + category.type() + ". Language: " + l.getClass().getSimpleName());
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
            com.repo().json(), this.logger, action,
            this.finalCommentStep(com, lang, "denied.fork.comment", com.authorLogin())
        );
        PreconditionCheckStep authorOwnerCheck = new AuthorOwnerCheck(
            com, this.logger,
            repoForkCheck,
            new OrganizationAdminCheck(
                com, this.logger,
                repoForkCheck,
                this.finalCommentStep(com, lang, "denied.commander.comment", com.authorLogin())
            )
        );
        PreconditionCheckStep repoNameCheck = new RepoNameCheck(
            com.repo().json(), this.logger, authorOwnerCheck,
            new GhPagesBranchCheck(
                com, this.logger, authorOwnerCheck,
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
            ), this.logger,
            new Step.FinalStep(this.logger)
        );
    }

}
