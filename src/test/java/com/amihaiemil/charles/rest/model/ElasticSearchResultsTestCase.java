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

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit tests for {@link ElasticSearchResults}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.2
 *
 */
public final class ElasticSearchResultsTestCase {
    
    /**
     * ElasticSearchResults knows the total number of hits.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void returnsTotalNumberOfHits() throws Exception {
        final SearchResultsPage results = new ElasticSearchResults(
            Json.createObjectBuilder()
                .add(
                    "hits", Json.createObjectBuilder().add("total",23)
                ).build()
        );
        MatcherAssert.assertThat(results.totalHits(), Matchers.is(23));
    }
    
    /**
     * ElasticSearchResults can return the results it contains.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void returnsResults() throws Exception {
        final SearchResultsPage page = new ElasticSearchResults(
            Json.createObjectBuilder()
                .add(
                    "hits",
                    Json.createObjectBuilder()
                        .add(
                            "hits",
                             Json.createArrayBuilder()
                                 .add(Json.createObjectBuilder())
                                 .add(Json.createObjectBuilder())
                                 .add(Json.createObjectBuilder())
                        )
                ).build()
        );
        MatcherAssert.assertThat(page.results(), Matchers.hasSize(3));
    }
    
    /**
     * ElasticSearchResult can set the page number and return a new instance of it.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void setsPageNr() throws Exception {
        final SearchResultsPage page = new ElasticSearchResults(Json.createObjectBuilder().build());
        final SearchResultsPage withNr = page.withPageNr(3);

        MatcherAssert.assertThat(page.pageNr(), Matchers.is(0));
        MatcherAssert.assertThat(withNr.pageNr(), Matchers.is(3));
        MatcherAssert.assertThat(page == withNr, Matchers.is(false));
    }
    
    /**
     * ElasticSearchResult can set the pages' links and return a new instance of it.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void setsPagesLinks() throws Exception {
        final SearchResultsPage noLinks = new ElasticSearchResults(Json.createObjectBuilder().build());
        final List<String> links = new ArrayList<>();
        links.add("link1");
        links.add("link2");
        links.add("link3");
        final SearchResultsPage withLinks = noLinks.withPages(links);

        MatcherAssert.assertThat(noLinks.pages(), Matchers.hasSize(0));
        MatcherAssert.assertThat(withLinks.pages(), Matchers.hasSize(3));
        MatcherAssert.assertThat(noLinks == withLinks, Matchers.is(false));
    }
    
    /**
     * ElasticSearchResult can set the next page's link and return a new instance of it.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void setsNextPage() throws Exception {
        final SearchResultsPage noNext = new ElasticSearchResults(Json.createObjectBuilder().build());
        final SearchResultsPage withNext = noNext.withNextPage("link/next/page");

        MatcherAssert.assertThat(noNext.nextPage(), Matchers.equalTo(""));
        MatcherAssert.assertThat(withNext.nextPage(), Matchers.equalTo("link/next/page"));
        MatcherAssert.assertThat(noNext == withNext, Matchers.is(false));
    }
    
    /**
     * ElasticSearchResult can set the previous page's link and return a new instance of it.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void setsPreviousPage() throws Exception {
        final SearchResultsPage noPrev = new ElasticSearchResults(Json.createObjectBuilder().build());
        final SearchResultsPage withPrev = noPrev.withPrevPage("link/prev/page");

        MatcherAssert.assertThat(noPrev.previousPage(), Matchers.equalTo(""));
        MatcherAssert.assertThat(withPrev.previousPage(), Matchers.equalTo("link/prev/page"));
        MatcherAssert.assertThat(noPrev == withPrev, Matchers.is(false));
    }
}
