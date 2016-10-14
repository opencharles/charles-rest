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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;

import com.amihaiemil.charles.rest.model.EsQuery;
import com.amihaiemil.charles.rest.model.SearchResult;
import com.amihaiemil.charles.rest.model.SearchResultsPage;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;

/**
 * Test cases for {@link AmazonEsSearch}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class AmazonEsSearchTestCase {

    /**
     * AmazonEsSearch performs ok when there are search results.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void searchWithResults() throws IOException {
        int port = this.port();
        MkContainer awsEs = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(this.readResource("esSearchResponse.json"))
        ).start(port);
        System.setProperty("aws.es.endpoint", "http://localhost:" + port + "/elasticsearch");
        System.setProperty("aws.es.region", "us-west");
        System.setProperty("aws.accessKeyId", "aws_key_id");
        System.setProperty("aws.secretKey", "secret_key");

        try {
            AmazonEsSearch es = new AmazonEsSearch(
                new EsQuery("test", "page", 0, 10),
                "amihaiemilxtestrepo"
            );
            SearchResultsPage srp = es.search();
            assertTrue(srp.getTotalHits() == 27);
            assertTrue(srp.getResults().size() == 10);
            SearchResult third = srp.getResults().get(2);
            assertTrue(third.getLink().equals("http://amihaiemil.com/stuff/link3page/page.html"));
            assertTrue(third.getCategory().equals("development"));
        
            SearchResult last = srp.getResults().get(9);
            assertTrue(last.getLink().equals("http://amihaiemil.com/some/other/page.html"));
            assertTrue(last.getCategory().equals("mischelaneous"));
        } finally {
            awsEs.stop();
        }
    }
    
    /**
     * AmazonEsSearch performs ok when there are no search results.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void searchWithNoResults() throws IOException {
        int port = this.port();
        MkContainer awsEs = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(this.readResource("esSearchResponseNoResults.json"))
        ).start(port);
        System.setProperty("aws.es.endpoint", "http://localhost:" + port + "/elasticsearch");
        System.setProperty("aws.es.region", "us-west");
        System.setProperty("aws.accessKeyId", "aws_key_id");
        System.setProperty("aws.secretKey", "secret_key");

        try {
            AmazonEsSearch es = new AmazonEsSearch(
                new EsQuery("test", "page", 0, 10),
                "amihaiemilxtestrepo"
            );
            SearchResultsPage srp = es.search();
            assertTrue(srp.getTotalHits() == 0);
            assertTrue(srp.getResults().isEmpty());
            assertTrue(srp.getPageNr() == 1);
            assertTrue(srp.getPages().isEmpty());
            assertTrue(srp.getNextPage().equals("-"));
            assertTrue(srp.getPreviousPage().equals("-"));
            
        } finally {
            awsEs.stop();
        }
    }
    
    /**
     * AmazonEsSearch throws IllegalStateException if the elasticsearch endpoint
     * sys property is missing.
     */
    @Test
    public void missingEsEndpoint() {
        EsQuery query = new EsQuery();
        query.setCategory("");
        query.setContent("");
        query.setIndex(0);
        query.setNr(10);
        AmazonEsSearch es = new AmazonEsSearch(query, "user/idx");
        try {
            es.search();
            fail("ISE was expected!");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().equals("ElasticSearch endpoint needs to be specified!"));
        }
        try {
            System.setProperty("aws.es.endpoint", "");
            es.search();
            fail("ISE was expected!");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().equals("ElasticSearch endpoint needs to be specified!"));
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
