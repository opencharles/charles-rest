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
import com.amihaiemil.charles.github.AwsEsRepository;

/**
 * AWS ElasticSearch repository that sends the webpages to the
 * Amazon ElasticSearch service, using the AWS sdk (for ease of request signing)
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public final class AmazonEsRepository implements AwsEsRepository {
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
            String data = new EsBulkJson(this.indexName, pages).structure();
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            AwsHttpRequest<HttpResponse> index =
                new SignedRequest<>(
                    new AwsHttpHeaders<>(
                        new AwsPost<>(
                            new EsHttpRequest<>(
                                "_bulk",
                                new SimpleAwsResponseHandler(false),
                                new SimpleAwsErrorHandler(false)
                            ),
                            new ByteArrayInputStream(data.getBytes())
                        ), headers
                     )
                );
                index.perform();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new DataExportException(e.getMessage());
        }
    }

    /**
     * Make a HEAD request and check if the elasticsearch
     * index exists.
     * @return True if the index exists, false otherwise.
     */
    @Override
    public boolean exists() {
        AwsHttpRequest<Boolean> head =
            new SignedRequest<>(
                new AwsHead<>(
                    new EsHttpRequest<>(
                        this.indexName,
                        new BooleanAwsResponseHandler(),
                        new SimpleAwsErrorHandler(false)
                    )
                )
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
    /**
     * Delete the elasticsearch index.
     * @param name Name of the index.
     */
    @Override
    public void delete() {
        AwsHttpRequest<HttpResponse> deleteIndex =
            new SignedRequest<>(
                new AwsDelete<>(
                    new EsHttpRequest<>(
                        this.indexName,
                        new SimpleAwsResponseHandler(false),
                        new SimpleAwsErrorHandler(false)
                    )
                )
           );
       deleteIndex.perform();
    }
    
}
