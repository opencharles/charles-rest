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
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * A page of search results coming from ElasticSearch.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.2
 */
public final class ElasticSearchResults implements SearchResultsPage {

    /**
     * Search results in JSON.
     */
    private JsonObject results; 
    
    private int pageNr;
    private String nextPage;
    private String previousPage;
    private List<String> pages;
    
    
    /**
     * Ctor.
     * @param results Results in JSON.
     */
    public ElasticSearchResults(final JsonObject results) {
        this(results, 0, "", "", new ArrayList<String>());
    }
    
    /**
     * Ctor.
     * @param results Json results from ElasticSearch.
     * @param pageNr Number of this page.
     * @param nextPage Link to the next page.
     * @param previousPage Link to the previous page.
     * @param pages Links to all the pages.
     */
    public ElasticSearchResults(
        final JsonObject results, final int pageNr,
        final String nextPage, final String previousPage,
        final List<String> pages
    ) {
        this.results = results;
        this.pageNr = pageNr;
        this.nextPage = nextPage;
        this.previousPage = previousPage;
        this.pages = pages;
    }
    
    @Override
    public List<SearchResult> results() {
        final List<SearchResult> res = new ArrayList<>();
        final JsonArray hits = this.results.getJsonObject("hits").getJsonArray("hits");
        for(int i=0; i<hits.size(); i++) {
            res.add(new ElasticSearchResult(hits.getJsonObject(i)));
        }
        return res;
    }

    @Override
    public int totalHits() {
        return this.results.getJsonObject("hits").getInt("total");
    }

    @Override
    public int pageNr() {
        return this.pageNr;
    }

    @Override
    public String previousPage() {
        return this.previousPage;
    }

    @Override
    public String nextPage() {
        return this.nextPage;
    }

    @Override
    public List<String> pages() {
        return this.pages;
    }

    @Override
    public SearchResultsPage withPageNr(int pageNr) {
        return new ElasticSearchResults(this.results, pageNr, this.nextPage, this.previousPage, this.pages);
    }

    @Override
    public SearchResultsPage withNextPage(String nextPage) {
        return new ElasticSearchResults(this.results, this.pageNr, nextPage, this.previousPage, this.pages);
    }

    @Override
    public SearchResultsPage withPrevPage(String prevPage) {
        return new ElasticSearchResults(this.results, this.pageNr, this.nextPage, prevPage, this.pages);
    }

    @Override
    public SearchResultsPage withPages(List<String> pages) {
        return new ElasticSearchResults(this.results, this.pageNr, this.nextPage, this.previousPage, pages);
    }
    
}
