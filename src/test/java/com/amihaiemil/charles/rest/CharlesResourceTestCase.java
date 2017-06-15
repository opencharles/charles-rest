/**
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
package com.amihaiemil.charles.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import com.amihaiemil.charles.rest.model.SearchResultsPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;

/**
 * Unit tests for {@link CharlesResource}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
@SuppressWarnings("resource")
public final class CharlesResourceTestCase {

    /**
     * CharlesResource displays the pages' links right.
     * @throws IOException In case something goes wrong.
     */
    @Test
    public void pagesAreDisplayed() throws IOException {
        int port = this.port();
		MkContainer awsEs = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(this.readResource("esSearchResponse.json"))
        ).start(port);
        System.setProperty("aws.es.endpoint", "http://localhost:" + port + "/elasticsearch");
        System.setProperty("aws.es.region", "us-west");
        System.setProperty("aws.accessKeyId", "aws_key_id");
        System.setProperty("aws.secretKey", "secret_key");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURL()).thenReturn(
            new StringBuffer("http://example.com")
        );
        CharlesResource resource = new CharlesResource(request);
        try {
            Response resp = resource.search("amihaiemil", "testrepo", "test", "page", "0", "3");
            ObjectMapper json = new ObjectMapper();
            SearchResultsPage results = json.readValue(resp.getEntity().toString(), new TypeReference<SearchResultsPage>(){});
            MatcherAssert.assertThat(
                results.getPreviousPage(), Matchers.equalTo("-")
            );
            MatcherAssert.assertThat(
                results.getNextPage(),
                Matchers.equalTo(
                    "http://example.com?kw=test&ctg=page&index=3&size=3"
                )
            );
            MatcherAssert.assertThat(
                results.getPages().size(), Matchers.is(9)
            );
            MatcherAssert.assertThat(
                results.getPages().get(0),
                Matchers.equalTo(
                    "http://example.com?kw=test&ctg=page&index=0&size=3"
                )
            );
            MatcherAssert.assertThat(
                results.getPages().get(8),
                Matchers.equalTo(
                    "http://example.com?kw=test&ctg=page&index=24&size=3"
                )
            );
        } finally {
            awsEs.stop();
        }
    }
    
    /**
     * Read resource for test.
     * @param resourceName
     * @return String content of the resource file.
     * @throws IOException If it goes wrong.
     */
    private String readResource(String resourceName) throws IOException {
        InputStream is = new FileInputStream(
            new File("src/test/resources/" + resourceName)
        );
        return new String(IOUtils.toByteArray(is));
    }
    
    /**
     * Find a free port.
     * @return A free port.
     * @throws IOException If something goes wrong.
     */
    private int port() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /**
     * Clear sys properties after each test.
     */
    @After
    public void cleanupSysProps() {
        System.clearProperty("aws.es.endpoint");
        System.clearProperty("aws.es.region");
        System.clearProperty("aws.accessKeyId");
        System.clearProperty("aws.secretKey");
    }
}
