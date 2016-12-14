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
 *  3)Neither the name of charles-rest nor the names of its
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
package com.amihaiemil.charles.aws;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * A query meant for the /_suggest endpoint of ElasticSearch
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public final class SuggestQuery implements EsQuery {

    /**
     * Input for wich we query suggestions.
     */
    private String input;

    /**
     * Default ctor.
     */
    public SuggestQuery() {
        this("");
    }

    /**
     * Ctor.
     * @param content
     * @param category
     * @param index
     * @param nr
     */
    public SuggestQuery(String input) {
        this.input = input;
    }

    /**
     * Turn this to a json query for the /_suggest endpoint of elasticsearch.<br>
     * Example of query <br><br>
     * <pre>
     *   {
     *     "text" : "the amsterdma meetpu",
     *     "contentSuggestion" : {
     *       "term" : {
     *         "field" : "textContent"
     *       }
     *     },
     *     "titleSuggestion" : {
     *       "term" : {
     *         "field" : "title"
     *       }
     *     }
     *   }
     * </pre>
     * @see
     * <a
     *   href="https://www.elastic.co/guide/en/elasticsearch/reference/2.3/search-suggesters.html"
     * >
     *   Elasticsearch Suggesters
     * </a>
     * @todo #150:30m/DEV This implementation might have to be changed. Currently it's using
     *  bare Suggester which does word correction rather than actual autocomplete. Waiting for
     *  an answer here:
     *  https://discuss.elastic.co/t/simple-way-to-implement-autocomplete-functionality/68979
     */
    @Override
    public JsonObject toJson() {
        JsonObject query =
            Json.createObjectBuilder()
                .add("text", this.input)
                .add(
                    "contentSuggestion",
                    Json.createObjectBuilder().add(
                        "term",
                        Json.createObjectBuilder()
                            .add("field", "textContent")
                            .build()
                    ).build()
                )
                .add(
                    "titleSuggestion",
                    Json.createObjectBuilder().add(
                        "term",
                        Json.createObjectBuilder()
                            .add("field", "title")
                            .build()
                    ).build()
            ).build();
        return query;
    }

}
