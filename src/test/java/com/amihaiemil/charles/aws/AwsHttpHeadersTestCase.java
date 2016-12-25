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

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.amazonaws.Request;
import com.amazonaws.http.HttpMethodName;

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
    	AwsHttpHeaders<String> awsh = new AwsHttpHeaders<>(
            new AwsHttpRequest.FakeAwsHttpRequest(),
            new HashMap<String, String>()
        );
        assertTrue(awsh.request() != null);
        assertTrue(awsh.request().getServiceName().equals("fake"));
    }
    
    /**
     * AwsHttpHeaders can perform the original {@link AwsHttpRequest}
     * @throws IOException If something goes wrong while reading
     *  the request's content.
     */
    @Test
    public void performsRequest() throws IOException {
    	Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-type", "json");
        headers.put("HeaderName", "HeaderValue");
        AwsHttpHeaders<String> awsh = new AwsHttpHeaders<>(
            new AwsHttpRequest.FakeAwsHttpRequest(), headers
        );
        assertTrue(awsh.perform().equals("performed fake request"));
        assertTrue(awsh.request().getHttpMethod().equals(HttpMethodName.POST));
        Map<String, String> retreived = awsh.request().getHeaders();
        assertTrue(retreived.size() == 2);
        assertTrue(retreived.get("Content-type").equals("json"));
        assertTrue(retreived.get("HeaderName").equals("HeaderValue"));

    }
}
