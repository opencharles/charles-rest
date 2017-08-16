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
 * The bot can understand a Command and have a conversation based on it.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
public final class Conversation implements Knowledge {

    /**
     * Languages that the chatbot speaks.
     */
    private Language[] languages;

    /**
     * The followup of this conversation; what does it know to do next?
     */
    private Knowledge followup;

    /**
     * Ctor.
     * @param followup Followup of this conversation; what does it know to do next?
     */
    public Conversation(final Knowledge followup) {
        this(followup, new English());
    }
    
    /**
     * Ctor.
     * @param followup Followup of this conversation; what does it know to do next?
     * @param langs Languages that the bot speaks.
     */
    public Conversation(final Knowledge followup, final Language... langs) {
        this.followup = followup;
        this.languages = langs;
    }

    @Override
    public Step handle(final Command com) throws IOException {
    	String type = "unknown";
    	Command understood = new Understood(com, type, this.languages[0]);
        for(Language lang : this.languages) {
        	type = lang.categorize(com);
            if(!"unknown".equals(type)) {
                understood = new Understood(com, type, lang);
                break;
            }
        }
        return this.followup.handle(understood);
    }

}
