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

import java.net.URI;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.http.HttpMethodName;
import com.amihaiemil.charles.rest.model.EsQuery;
import com.amihaiemil.charles.rest.model.SearchResultsPage;

/**
 * Perform a search in the Amazon ElasticSerch service
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class AmazonEsSearch {

    /**
     * ElasticSearch  query.
     */
    private EsQuery query;

    /**
     * Index to search into.
     */
    private String indexName;

    /**
     * Ctor.
     * @param qry
     * @param idxName
     */
    public AmazonEsSearch(EsQuery qry, String idxName) {
        this.query = qry;
        this.indexName = idxName;
    }

    public SearchResultsPage search() {
        SignedRequest<SearchResultsPage> sr = new SignedRequest<>(
            this.buildAwsSearchRequest(),
                new SearchResponseHandler(),
                new SimpleAwsErrorHandler(false)
        );
        return sr.sendRequest();
    }

    /**
     * Builds the GET request to send to the
     * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html">
     * Es Search API
     * </a>
     * @param data Json structure expected by the bulk api.
     * @return Aws request.
     */
    public Request<Void> buildAwsSearchRequest() {
        Request<Void> request = new DefaultRequest<Void>("es");
        String esEndpoint = System.getProperty("aws.es.endpoint");
        if(esEndpoint == null || esEndpoint.isEmpty()) {
            throw new IllegalStateException("ElasticSearch endpoint needs to be specified!");
        }
        String search = "_search/"
            + indexName + "/" 
            + query.getCategory() + "?q=textContent="
            + query.getContent() + "&from="
            + query.getIndex() + "&size="
            + query.getNr();
        if(esEndpoint.endsWith("/")) {
            esEndpoint += search;
        } else {
            esEndpoint += "/" + search;
        }
        request.setEndpoint(URI.create(esEndpoint));
        request.setHttpMethod(HttpMethodName.GET);
        return request;
    }
}
