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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.http.HttpResponse;
import com.amihaiemil.charles.DataExportException;
import com.amihaiemil.charles.WebPage;
import com.amihaiemil.charles.aws.requests.AwsDelete;
import com.amihaiemil.charles.aws.requests.AwsHead;
import com.amihaiemil.charles.aws.requests.AwsHttpHeaders;
import com.amihaiemil.charles.aws.requests.AwsHttpRequest;
import com.amihaiemil.charles.aws.requests.AwsPost;
import com.amihaiemil.charles.aws.requests.EsHttpRequest;
import com.amihaiemil.charles.aws.requests.SignedRequest;
import com.amihaiemil.charles.rest.model.SearchResultsPage;

/**
 * AWS ElasticSearch repository that sends the webpages to the
 * Amazon ElasticSearch service, using the AWS sdk (for ease of request signing)
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * @todo #250:1h Implement the delete page knowledge. We have the repo method and unit
 *  test for it. Continue with the language, knowledge and steps.
 */
public final class AmazonElasticSearch implements ElasticSearch {
    private static final Logger LOG = LoggerFactory.getLogger(AmazonElasticSearch.class);    

    /**
     * Name of the Es index where the pages will be exported.
     */
    private String indexName;

    /**
     * AWS access key.
     */
    private AccessKeyId accesskey;
    
    /**
     * Aws secret key;
     */
    private SecretKey secretKey;
    
    /**
     * Aws ES region.
     */
    private Region reg;
    
    /**
     * ElasticSearch URL.
     */
    private EsEndPoint esEdp;
    
    /**
     * Ctor. 
     * @param indexName Name of the index.
     */
    public AmazonElasticSearch(final String indexName) {
        this(
            indexName,
            new StAccessKeyId(),
            new StSecretKey(),
            new StRegion(),
            new StEsEndPoint()
        );
    }
    
    /**
     * ctor.
     * @param indexName Name of the Es index where the pages will be exported.
     * @param accesskey Aws access key.
     * @param secretKey Aws secret key.
     * @param reg AWS ElasticSearch region.
     * @param es ElasticSearch URL.
     */
    public AmazonElasticSearch(
        final String indexName,
        final AccessKeyId accesskey,
        final SecretKey secretKey,
        final Region reg,
        final EsEndPoint es
    ) {
        this.indexName = indexName;
        this.accesskey = accesskey;
        this.secretKey = secretKey;
        this.reg = reg;
        this.esEdp = es;
    }

    @Override
    public SearchResultsPage search(final SearchQuery query) {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        AwsHttpRequest<SearchResultsPage> search =
            new SignedRequest<>(
                new AwsHttpHeaders<>(
                    new AwsPost<>(
                        new EsHttpRequest<>(
                            this.esEdp,
                            this.indexName + "/_search",
                            new SearchResponseHandler(),
                            new SimpleAwsErrorHandler(false)
                        ),
                        new ByteArrayInputStream(query.toJson().toString().getBytes())
                    ), headers
                ),
                this.accesskey,
                this.secretKey,
                this.reg
            );
        return search.perform();
    }
    
    @Override
    public void export(List<WebPage> pages) throws DataExportException {
        try {
            String data = new EsBulkJson(this.indexName, pages).structure();
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            final AwsHttpRequest<HttpResponse> index =
                new SignedRequest<>(
                    new AwsHttpHeaders<>(
                        new AwsPost<>(
                            new EsHttpRequest<>(
                                this.esEdp,
                                "_bulk",
                                new SimpleAwsResponseHandler(false),
                                new SimpleAwsErrorHandler(false)
                            ),
                            new ByteArrayInputStream(data.getBytes())
                        ), headers
                     ),
                     this.accesskey,
                     this.secretKey,
                     this.reg
                );
                index.perform();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new DataExportException(e.getMessage());
        }
    }

    @Override
    public boolean exists() {
        final AwsHttpRequest<Boolean> head =
            new SignedRequest<>(
                new AwsHead<>(
                    new EsHttpRequest<>(
                        this.esEdp,
                        this.indexName,
                        new BooleanAwsResponseHandler(),
                        new SimpleAwsErrorHandler(false)
                    )
                ),
                this.accesskey,
                this.secretKey,
                this.reg
            );
        boolean exists = false;
        try {
            exists = head.perform();
        } catch (AmazonServiceException ex) {
            if (!(ex.getStatusCode() == HttpStatus.SC_NOT_FOUND)) {
                throw ex;
            }
        }
        return exists;
    }
    
    @Override
    public void delete() {
        final AwsHttpRequest<HttpResponse> deleteIndex =
            new SignedRequest<>(
                new AwsDelete<>(
                    new EsHttpRequest<>(
                        this.esEdp,
                        this.indexName,
                        new SimpleAwsResponseHandler(false),
                        new SimpleAwsErrorHandler(false)
                    )
                ),
                this.accesskey,
                this.secretKey,
                this.reg
           );
       deleteIndex.perform();
    }

    @Override
    public void delete(final String type, final String id) {
        final AwsHttpRequest<HttpResponse> deleteDoc =
            new SignedRequest<>(
                new AwsDelete<>(
                    new EsHttpRequest<>(
                        this.esEdp,
                        this.indexName + "/" + type + "/" + id,
                        new SimpleAwsResponseHandler(false),
                        new SimpleAwsErrorHandler(false)
                    )
                ),
                this.accesskey,
                this.secretKey,
                this.reg
           );
           deleteDoc.perform();
    }
    
}
