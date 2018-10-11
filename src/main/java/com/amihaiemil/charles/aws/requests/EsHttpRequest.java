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
package com.amihaiemil.charles.aws.requests;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpResponseHandler;
import com.amihaiemil.charles.aws.EsEndPoint;

import java.net.URI;

/**
 * Base HTTP request sent to Amazon ES service using the aws-java-sdk.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public final class EsHttpRequest<T> extends AwsHttpRequest<T> {

    /**
     * Base request.
     */
    private final Request<Void> request;

    /**
     * Response handler.
     */
    private HttpResponseHandler<T> respHandler;

    /**
     * Response handler.
     */
    private HttpResponseHandler<AmazonServiceException> errHandler;

    /**
     * Ctor.
     * @param esEdp ElasticSearch URL.
     * @param uri REST path to the desired ElasticSearch endpoint.
     * @param respHandler Response handler.
     * @param errHandler Error handler.
     */
    public EsHttpRequest(
    	EsEndPoint esEdp,
        String uri,
        HttpResponseHandler<T> respHandler,
        HttpResponseHandler<AmazonServiceException> errHandler
    ){
    	this.request = new DefaultRequest<Void>("es");
        String esEndpoint = esEdp.read();
        if(esEndpoint == null || esEndpoint.isEmpty()) {
            throw new IllegalStateException("ElasticSearch endpoint needs to be specified!");
        }
        if(esEndpoint.endsWith("/")) {
            esEndpoint += uri;
        } else {
            esEndpoint += "/" + uri;
        }
        this.request.setEndpoint(URI.create(esEndpoint));
        
        
        this.respHandler = respHandler;
        this.errHandler = errHandler;
    }

    /**
     * Perform this request.	
     */
    @Override
    public T perform() {
        final Response<T> rsp = new AmazonHttpClient(new ClientConfiguration())
            .requestExecutionBuilder()
            .executionContext(new ExecutionContext(true))
            .request(this.request)
            .errorResponseHandler(this.errHandler)
            .execute(this.respHandler);
        return rsp.getAwsResponse();
    }

    /**
     * Fetch the base request.
     * @return
     */
    @Override
    Request<Void> request() {
        return this.request;
    }
    
}
