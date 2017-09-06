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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A page of search results. It is annotated with JsonProperty so Jackson can parse the object
 * into JSON.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.2
 */
public interface SearchResultsPage {
    
    /**
     * The search results from this page.
     * @return List of SearchResult.
     */
    @JsonProperty("results")
    List<SearchResult> results();
    
    /**
     * The total number of results.
     * @return Integer.
     */
    @JsonProperty("totalHits")
    int totalHits();
    
    /**
     * The page's number.
     * @return Integer.
     */
    @JsonProperty("pageNr")
    int pageNr();

    /**
     * Link to the previous page.
     * @return String.
     */
    @JsonProperty("previousPage")
    String previousPage();
    
    /**
     * Link to the next page.
     * @return String.
     */
    @JsonProperty("nextPage")
    String nextPage();
    
    /**
     * Links to all the page results.
     * @return List of String.
     */
    @JsonProperty("pages")
    List<String> pages();
    
    /**
     * Specify the page number.
     * @param pageNr Integer.
     * @return SearchResultsPage.
     */
    SearchResultsPage withPageNr(final int pageNr);
    
    /**
     * Specify the next page.
     * @param nextPage String.
     * @return SearchResultsPage.
     */
    SearchResultsPage withNextPage(final String nextPage);
    
    /**
     * Specify the previous pager.
     * @param previousPage String.
     * @return SearchResultsPage.
     */
    SearchResultsPage withPrevPage(final String prevPage);
    
    /**
     * Specify all the pages.
     * @param pages Links to all the pages..
     * @return SearchResultsPage.
     */
    SearchResultsPage withPages(final List<String> pages);
    
    public static final class Fake implements SearchResultsPage {

        @Override
        public List<SearchResult> results() {
            return new ArrayList<SearchResult>();
        }

        @Override
        public int totalHits() {
            return 0;
        }

        @Override
        public int pageNr() {
            return 0;
        }

        @Override
        public String previousPage() {
            return "";
        }

        @Override
        public String nextPage() {
            return "";
        }

        @Override
        public List<String> pages() {
            return new ArrayList<String>();
        }

        @Override
        public SearchResultsPage withPageNr(int pageNr) {
            return this;
        }

        @Override
        public SearchResultsPage withNextPage(String nextPage) {
            return this;
        }

        @Override
        public SearchResultsPage withPrevPage(String prevPage) {
            return this;
        }

        @Override
        public SearchResultsPage withPages(List<String> pages) {
            return this;
        }
        
    }
}
