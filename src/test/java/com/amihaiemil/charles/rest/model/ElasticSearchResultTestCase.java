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
package com.amihaiemil.charles.rest.model;

import java.io.ByteArrayInputStream;
import javax.json.Json;
import javax.json.JsonObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link ElasticSearchResult}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.2
 *
 */
public final class ElasticSearchResultTestCase {
    
    /**
     * ElasticSearchResponse can animate a Json hit from ES.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void animatesJsonHit() throws Exception {
        JsonObject hit = Json.createObjectBuilder()
            .add(
                "_source",
                Json.createObjectBuilder()
                    .add("title", "Test Title")
                    .add("url", "http://charles.amihaiemil.com/page.html")
                    .add("category", "testCategory")
            ).add(
                "highlight",
                Json.createObjectBuilder()
                    .add(
                        "textContent",
                        Json.createArrayBuilder().add("... test content page ...")
                    )
            ).build();
        SearchResult result = new ElasticSearchResult(hit);
        MatcherAssert.assertThat(result.title(), Matchers.equalTo("Test Title"));
        MatcherAssert.assertThat(result.link(), Matchers.equalTo("http://charles.amihaiemil.com/page.html"));
        MatcherAssert.assertThat(result.highlight(), Matchers.equalTo("... test content page ..."));
        MatcherAssert.assertThat(result.category(), Matchers.equalTo("testCategory"));
    }
    
    /**
     * ElasticSearchResponse can animate a Json hit which misses its category.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void animatesJsonHitWithMissingCategory() throws Exception {
        JsonObject hit = Json.createObjectBuilder()
            .add(
                "_source",
                Json.createObjectBuilder()
                    .add("title", "Test Title")
                    .add("url", "http://charles.amihaiemil.com/page.html")
            ).add(
                "highlight",
                Json.createObjectBuilder()
                    .add(
                        "textContent",
                        Json.createArrayBuilder().add("... test content page ...")
                    )
            ).build();
        SearchResult result = new ElasticSearchResult(hit);
        MatcherAssert.assertThat(result.title(), Matchers.equalTo("Test Title"));
        MatcherAssert.assertThat(result.link(), Matchers.equalTo("http://charles.amihaiemil.com/page.html"));
        MatcherAssert.assertThat(result.highlight(), Matchers.equalTo("... test content page ..."));
        MatcherAssert.assertThat(result.category(), Matchers.equalTo("page"));
    }
    
    /**
     * An instance of ElasticSearchResponse can be parsed into JsonObject by Jackson.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void isParsableByJackson() throws Exception {
        final JsonObject hit = Json.createObjectBuilder()
                .add(
                    "_source",
                    Json.createObjectBuilder()
                        .add("title", "Test Title")
                        .add("url", "http://charles.amihaiemil.com/page.html")
                ).add(
                    "highlight",
                    Json.createObjectBuilder()
                        .add(
                            "textContent",
                            Json.createArrayBuilder().add("... test content page ...")
                        )
                ).build();
         final SearchResult result = new ElasticSearchResult(hit);
         final JsonObject jackson = Json.createReader(
             new ByteArrayInputStream(
                 new ObjectMapper().writeValueAsString(result).getBytes()
             )
         ).readObject();
         MatcherAssert.assertThat(
             jackson.getString("link"), Matchers.equalTo("http://charles.amihaiemil.com/page.html")
         );
         MatcherAssert.assertThat(
             jackson.getString("title"), Matchers.equalTo("Test Title")
         );
         MatcherAssert.assertThat(
             jackson.getString("highlight"), Matchers.equalTo("... test content page ...")
             );
         MatcherAssert.assertThat(
             jackson.getString("category"), Matchers.equalTo("page")
         );
    }
}
