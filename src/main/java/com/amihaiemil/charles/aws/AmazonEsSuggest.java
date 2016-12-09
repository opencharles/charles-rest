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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import com.amihaiemil.charles.rest.model.SuggestQuery;

/**
 * Perform a suggest request in the Amazon ElasticSerch service
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public final class AmazonEsSuggest {

    /**
     * ElasticSearch suggest query.
     */
    private SuggestQuery query;

    /**
     * Index to search into.
     */
    private String indexName;

    /**
     * Ctor.
     * @param qry
     * @param idxName
     */
    public AmazonEsSuggest(SuggestQuery qry, String idxName) {
        this.query = qry;
        this.indexName = idxName;
    }

    /**
     * Perform a search query.
     * @return
     */
    public String[] suggest() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        AwsHttpRequest<String[]> request =
            new SignedRequest<>(
                new AwsHttpHeaders<>(
                    new AwsPost<>(
                        new EsHttpRequest<>(
                            this.indexName + "/_suggest",
                            new SuggestionsResponseHandler(), new SimpleAwsErrorHandler(false)
                        ),
                        new ByteArrayInputStream(this.query.toJson().toString().getBytes())
                    ), headers
                )
            );
        return request.perform();
    }
}
