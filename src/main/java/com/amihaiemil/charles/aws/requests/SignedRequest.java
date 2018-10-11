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

import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amihaiemil.charles.aws.AccessKeyId;
import com.amihaiemil.charles.aws.Region;
import com.amihaiemil.charles.aws.SecretKey;

/**
 * A signed request made to AWS.
 * @param <T> Response type.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * @see <a href="http://docs.aws.amazon.com/general/latest/gr/signature-version-4.html"> AWS Docs </a>
 * @see <a href="https://blogs.aws.amazon.com/security/post/Tx3VP208IBVASUQ/How-to-Control-Access-to-Your-Amazon-Elasticsearch-Service-Domain">Aws blog post</a>
 *
 */
public class SignedRequest<T> extends AwsHttpRequest<T> {

    /**
     * Base request.
     */
    private AwsHttpRequest<T> base;

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
     * Ctor.
     * @param req Request to sign.
     * @param accesskey Key to sign with.
     * @param secretKey Secret to sign with.
     * @param reg Request to sign.
     */
    public SignedRequest(
        final AwsHttpRequest<T> req,
        final AccessKeyId accesskey,
        final SecretKey secretKey,
        final Region reg
    ) {
        this.base = req;
        this.accesskey = accesskey;
        this.secretKey = secretKey;
        this.reg = reg;
    }
    
    @Override
    public T perform() {
        AWS4Signer signer = new AWS4Signer();
        String region = this.reg.read();
        if(region == null || region.isEmpty()) {
            throw new IllegalStateException("Mandatory sys property aws.es.region not specified!");
        }
        signer.setRegionName(this.reg.read());
        signer.setServiceName(this.base.request().getServiceName());
        signer.sign(this.base.request(), new AwsCredentialsFromSystem(this.accesskey, this.secretKey));
        return this.base.perform();
    }

    @Override
    Request<Void> request() {
        return this.base.request();
    }
    
    /**
     * AWS credentials (aws access key id and aws secret key from the system properties).
     */
    private static class AwsCredentialsFromSystem implements AWSCredentials {

    	/**
         * AWS access key.
         */
        private final AccessKeyId accesskey;
        
        /**
         * Aws secret key;
         */
        private final SecretKey secretKey;
    	
        private AwsCredentialsFromSystem(final AccessKeyId accesskey, final SecretKey secret) {
        	this.accesskey = accesskey;
        	this.secretKey = secret;
        }
        
        @Override
        public String getAWSAccessKeyId() {
        	String accessKeyId = this.accesskey.read();
        	if(accessKeyId == null || accessKeyId.isEmpty()) {
                throw new IllegalStateException("Mandatory sys property aws.accessKeyId not specified!");
            }
            return accessKeyId;
        }

        @Override
        public String getAWSSecretKey() {
        	String secretKey = this.secretKey.read();
            if(secretKey == null || secretKey.isEmpty()) {
                throw new IllegalStateException("Mandatory sys property aws.secretKey not specified!");
            }
            return secretKey;
        }
        
    }

}
