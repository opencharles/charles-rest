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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.HttpResponseHandler;
import com.amihaiemil.charles.rest.model.SearchResult;
import com.amihaiemil.charles.rest.model.SearchResultsPage;

/**
 * Response handler that parses the search response into a {@link SearchResultsPage}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class SearchResponseHandler implements HttpResponseHandler<SearchResultsPage>{

    @Override
    public SearchResultsPage handle(HttpResponse response) {
        int status = response.getStatusCode();
        if(status < 200 || status >= 300) {
            AmazonServiceException ase = new AmazonServiceException("Unexpected status: " + status);
            ase.setStatusCode(status);
            throw ase;
        }
        return this.buildResultsPage(response);
    }

    @Override
    public boolean needsConnectionLeftOpen() {
        return false;
    }

    /**
     * Build the search results page
     * @param response
     * @return
     */
    private SearchResultsPage buildResultsPage(HttpResponse response) {
        SearchResultsPage page = new SearchResultsPage();
        InputStream content = response.getContent();
        JsonObject result = Json.createReader(content).readObject();
        int totalHits = result.getJsonObject("hits").getInt("total");
        if(totalHits != 0) {
            List<SearchResult> searchResults = new ArrayList<SearchResult>();
            JsonArray hits = result.getJsonObject("hits").getJsonArray("hits");
            for(int i=0; i<hits.size(); i++) {
                JsonObject hitSource = hits.getJsonObject(i).getJsonObject("_source");
                JsonObject highlight = hits.getJsonObject(i).getJsonObject("highlight");
                SearchResult res = new SearchResult(
                    hitSource.getString("url"),
                    highlight.getJsonArray("textContent").getString(0),
                    hitSource.getString("category")
                );
                searchResults.add(res);
            }
            page.setResults(searchResults);
            page.setTotalHits(totalHits);
        } 
        return page;
    }

}