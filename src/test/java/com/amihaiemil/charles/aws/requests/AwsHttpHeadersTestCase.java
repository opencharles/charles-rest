/*
 * Copyright (c) 2016-2017, Mihai Emil Andronache
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
package com.amihaiemil.charles.aws.requests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.amazonaws.Request;
import com.amazonaws.http.HttpMethodName;
import com.amihaiemil.charles.aws.requests.AwsHttpHeaders;
import com.amihaiemil.charles.aws.requests.AwsHttpRequest;

/**
 * Unit tests for {@link AwsHttpHeaders}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class AwsHttpHeadersTestCase {
    
    /**
     * AwsHttpHeaders can fetch the original {@link Request}
     */
    @Test
    public void fetchesOriginalRequest() {
    	final AwsHttpHeaders<String> awsh = new AwsHttpHeaders<>(
            new AwsHttpRequest.FakeAwsHttpRequest(),
            new HashMap<String, String>()
        );
        MatcherAssert.assertThat(awsh.request(),
        	Matchers.not(Matchers.equalTo(null))
        );
        MatcherAssert.assertThat(awsh.request().getServiceName(),
        	Matchers.equalTo("fake")
        );
    }
    
    /**
     * AwsHttpHeaders can perform the original {@link AwsHttpRequest}
     */
    @Test
    public void performsRequest() {
        final AwsHttpHeaders<String> awsh = new AwsHttpHeaders<>(
            new AwsHttpRequest.FakeAwsHttpRequest(),
            new HashMap<String, String>()
        );        
        MatcherAssert.assertThat(awsh.perform(),
            Matchers.equalTo("performed fake request")
        );
        MatcherAssert.assertThat(awsh.request().getHttpMethod(),
            Matchers.equalTo(HttpMethodName.POST)
        );
    }
    
    /**
     * AwsHttpHeaders can add headers to the original {@link AwsHttpRequest}
     * @throws IOException If something goes wrong while reading
     *  the request's content.
     */
    @Test
    public void addsHeaders() throws IOException {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-type", "json");
        headers.put("HeaderName", "HeaderValue");
        
        final AwsHttpRequest<String> fake = 
            new AwsHttpRequest.FakeAwsHttpRequest();
        fake.request().addHeader("Request-type", "fake");
        
        final AwsHttpHeaders<String> awsh = new AwsHttpHeaders<>(fake, headers);
        final Map<String, String> retreived = awsh.request().getHeaders();
        
        MatcherAssert.assertThat(
            retreived.size() == 3, Matchers.is(true)
        );
        MatcherAssert.assertThat(
            retreived.get("Content-type"), Matchers.equalTo("json")
        );
        MatcherAssert.assertThat(
            retreived.get("HeaderName"), Matchers.equalTo("HeaderValue")
        );
        MatcherAssert.assertThat(
            retreived.get("Request-type"), Matchers.equalTo("fake")
        );
    }
}
