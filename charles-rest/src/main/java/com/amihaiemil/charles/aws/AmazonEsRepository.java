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
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.HttpResponse;
import com.amihaiemil.charles.DataExportException;
import com.amihaiemil.charles.Repository;
import com.amihaiemil.charles.WebPage;

/**
 * AWS ElasticSearch repository that sends the webpages to the
 * Amazon ElasticSearch service, using the AWS sdk (for ease of request signing)
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class AmazonEsRepository implements Repository {
    private static final Logger LOG = LoggerFactory.getLogger(AmazonEsRepository.class);    

    /**
     * Name of the Es index where the pages will be exported.
     */
    private String indexName;

    /**
     * ctor.
     * @param indexName Name of the Es index where the pages will be exported.
     */
    public AmazonEsRepository(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public void export(List<WebPage> pages) throws DataExportException {
        try {
            SignedRequest<HttpResponse> sr = new SignedRequest<>(
                this.buildAwsIndexRequest(
                    new EsBulkJson(this.indexName, pages).structure()
                ),
                new SimpleAwsResponseHandler(false),
                new SimpleAwsErrorHandler(false)
            );
            sr.sendRequest();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new DataExportException(e.getMessage());
        }
    }

    /**
     * Builds the POST request to send to the
     * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html">
     * Es Bulk API
     * </a>
     * @param data Json structure expected by the bulk api.
     * @return Aws request.
     */
    public Request<Void> buildAwsIndexRequest(String data) {
        Request<Void> request = new DefaultRequest<Void>("es");
        request.setContent(new ByteArrayInputStream(data.getBytes()));
        String esEndpoint = System.getProperty("aws.es.endpoint");
        if(esEndpoint == null || esEndpoint.isEmpty()) {
            throw new IllegalStateException("ElasticSearch endpoint needs to be specified!");
        }
        if(esEndpoint.endsWith("/")) {
            esEndpoint += "_bulk?pretty";
        } else {
            esEndpoint += "/_bulk?pretty";
        }
        request.setEndpoint(URI.create(esEndpoint));
        request.setHttpMethod(HttpMethodName.POST);
        return request;
    }

}
