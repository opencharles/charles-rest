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
package com.amihaiemil.charles.aws;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Query for search.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public final class SearchQuery {

    /**
     * Query the content field.
     */
    private String content;

    /**
     * Query the category (logical type in the index)
     */
    private String category;

    /**
     * Show results from this index.
     */
    private int index;

    /**
     * Show nr of results on a page.
     */
    private int nr;
    
    /**
     * Default ctor.
     */
    public SearchQuery() {
        this("", "", 0, 10);
    }
    /**
     * Ctor.
     * @param content Keywords.
     * @param category Category.
     * @param index Index to start at.
     * @param nr Number of results per page.
     */
    public SearchQuery(String content, String category, int index, int nr) {
        this.content = content;
        this.category = category;
        this.index = index;
        this.nr = nr;
    }

    /**
     * Turn this to json query for performing a search request
     * (with query in the body; the only way to provide highlighting)
     * <br><br>
     * An example of json query:<br>
     * <pre>
     * {
     *     "from": 10,
     *     "size": 15,
     *     "filter": {
     *         "bool": {
     *             "filter": [{
     *                 "match_phrase_prefix": {
     *                     "textContent": "string here"
     *                  }
     *              },
     *              {
     *                  "type": {
     *                      "value": "tech"
     *                   }
     *              }]
     *         }
     *     }
     * }
     * </pre>
     * 
     * 
     * @return This Query in Json format 
     * @see 
     * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-query.html">
     * ElasticSearch Query
     * </a>
     * @see 
     * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/2.4/term-level-queries.html">
     * Term level queries
     * </a>
     */
    public JsonObject toJson() {
        JsonObject matcher = Json.createObjectBuilder()
            .add(
                "match_phrase_prefix",
                Json.createObjectBuilder().add("textContent", this.content)    
            ).build();
        JsonObject type = Json.createObjectBuilder()
            .add(
                "type",
                Json.createObjectBuilder().add("value", this.category)    
            ).build();
        JsonArray filter = Json.createArrayBuilder().add(matcher).add(type).build();
        JsonObject bool = Json.createObjectBuilder().add(
            "bool", Json.createObjectBuilder().add("filter", filter).build()
        ).build();
        
        JsonObject highlight = Json.createObjectBuilder()
            .add(
                "fields",
                Json.createObjectBuilder()
                    .add(
                        "textContent",
                        Json.createObjectBuilder()
                            .add("fragment_size", 150)
                            .build()
                    )
                    .build()
             ).build();
        
        JsonObject query = Json.createObjectBuilder()
            .add("from", this.index)
            .add("size", this.nr)
            .add("query", bool)
            .add("highlight", highlight)
            .build();
        return query;
    }
    
}
