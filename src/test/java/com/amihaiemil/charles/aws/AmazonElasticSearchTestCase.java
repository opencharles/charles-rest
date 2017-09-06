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
package com.amihaiemil.charles.aws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.Test;

import com.amazonaws.AmazonServiceException;
import com.amihaiemil.charles.Link;
import com.amihaiemil.charles.SnapshotWebPage;
import com.amihaiemil.charles.WebPage;
import com.amihaiemil.charles.rest.model.SearchResult;
import com.amihaiemil.charles.rest.model.SearchResultsPage;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link AmazonElasticSearch}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
@SuppressWarnings("resource")
public class AmazonElasticSearchTestCase {

    /**
     * IllegalStateException is thrown on missing secret key property.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void illegalStateOnMissingSecretKey() throws Exception {
        List<WebPage> pages = new ArrayList<WebPage>();
        pages.add(this.mockWebPage("http://www.test.com/crawledpage.html"));
        pages.add(this.mockWebPage("https://www.test.com/stuff/crawledpage.html"));

        ElasticSearch repo = new AmazonElasticSearch(
            "testIndex",
            new AccessKeyId.Fake("access_key"),
            new SecretKey.Fake(null),
            new Region.Fake("ro"),
            new EsEndPoint.Fake("http://localhost:8080/es")
        );
        try {
            repo.export(pages);
            fail();
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Mandatory sys property aws.secretKey"));
        }
    }
    
    /**
     * IllegalStateException is thrown on missing access key property.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void illegalStateOnMissingAccessKey() throws Exception {
        List<WebPage> pages = new ArrayList<WebPage>();
        pages.add(this.mockWebPage("http://www.test.com/crawledpage.html"));
        pages.add(this.mockWebPage("https://www.test.com/stuff/crawledpage.html"));

        ElasticSearch repo = new AmazonElasticSearch(
            "testIndex",
            new AccessKeyId.Fake(null),
            new SecretKey.Fake("secret"),
            new Region.Fake("ro"),
            new EsEndPoint.Fake("http://localhost:8080/es")
        );
        try {
            repo.export(pages);
            fail();
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Mandatory sys property aws.accessKeyId"));
        }
    }
    
    /**
     * IllegalStateException is thrown on missing region property.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void illegalStateOnMissingRegion() throws Exception {
        List<WebPage> pages = new ArrayList<WebPage>();
        pages.add(this.mockWebPage("http://www.test.com/crawledpage.html"));
        pages.add(this.mockWebPage("https://www.test.com/stuff/crawledpage.html"));
        ElasticSearch repo = new AmazonElasticSearch(
            "testIndex",
            new AccessKeyId.Fake("access"),
            new SecretKey.Fake("secret"),
            new Region.Fake(null),
            new EsEndPoint.Fake("http://localhost:8080/es")
        );
        try {
            repo.export(pages);
            fail();
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Mandatory sys property aws.es.region"));
        }
    }
    
    /**
     * AmazonEsSearch throws IllegalStateException if the elasticsearch endpoint
     * sys property is missing.
     */
    @Test
    public void illegalStateOnMissingEsEndpoint() {
        ElasticSearch es = new AmazonElasticSearch(
            "user/idx",
            new AccessKeyId.Fake("aws_key_id"),
            new SecretKey.Fake("secret_key"),
            new Region.Fake("us-west"),
            new EsEndPoint.Fake(null)
        );
        try {
            es.search(new SearchQuery());
            fail("ISE was expected!");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().equals("ElasticSearch endpoint needs to be specified!"));
        }
    }
    
    /**
     * A request is sent to the AWS Es bulk api service.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void sendsExportRequestToAwsEs() throws Exception {
        List<WebPage> pages = new ArrayList<WebPage>();
        pages.add(this.mockWebPage("http://www.test.com/crawledpage.html"));
        pages.add(this.mockWebPage("https://www.test.com/stuff/crawledpage.html"));
        
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
           .next(new MkAnswer.Simple("{\"status\":\"Unit test successful!\"}"))
           .start(port);
        try {
            new AmazonElasticSearch(
                "testIndex",
                new AccessKeyId.Fake("access_key"),
                new SecretKey.Fake("secret_key"),
                new Region.Fake("ro"),
                new EsEndPoint.Fake("http://localhost:" + port + "/es")
            ).export(pages);
            MkQuery request = server.take();
            assertEquals("/es/_bulk/",request.uri().toString());
            assertTrue("POST".equals(request.method()));
        } finally {
            server.stop();
        }
    }
    
    /**
     * A request is sent to the AWS Es bulk api service, but it fails.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void sendsExportRequestToAwsEsWithError() throws Exception {
        List<WebPage> pages = new ArrayList<WebPage>();
        pages.add(this.mockWebPage("http://www.test.com/crawledpage.html"));
        pages.add(this.mockWebPage("https://www.test.com/stuff/crawledpage.html"));

        int port = this.port();
        
        MkContainer server = new MkGrizzlyContainer()
           .next(new MkAnswer.Simple(412))
           .start(port);
        try {
            new AmazonElasticSearch(
                "testIndex",
                new AccessKeyId.Fake("access_key"),
                new SecretKey.Fake("secret_key"),
                new Region.Fake("ro"),
                new EsEndPoint.Fake("http://localhost:" + port + "/es/")
            ).export(pages);
        } catch (AmazonServiceException ase) {
            assertTrue(ase.getErrorMessage().contains("Precondition Failed"));
            MkQuery request = server.take();
            assertEquals("/es/_bulk/", request.uri().toString());
            assertTrue("POST".equals(request.method()));
        }finally {
            server.stop();
        }
    }
    
    /**
     * A request to DELETE the index is made to ES.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void sendsDeleteRequestToAwsEs() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
           .next(new MkAnswer.Simple("{\"status\":\"index deleted\"}"))
           .start(port);
        try {
            new AmazonElasticSearch(
                "index.to.be.deleted",
                new AccessKeyId.Fake("access_key"),
                new SecretKey.Fake("secret_key"),
                new Region.Fake("ro"),
                new EsEndPoint.Fake("http://localhost:" + port + "/es/")
            ).delete();
            MkQuery request = server.take();
            assertEquals("/es/index.to.be.deleted/", request.uri().toString());
            assertTrue("DELETE".equals(request.method()));
        } finally {
            server.stop();
        }
    }
    
    /**
     * A request to DELETE a document, from one index, is made to ES.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void sendsDeleteDocumentRequestToAwsEs() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
           .next(new MkAnswer.Simple("{\"status\":\"index deleted\"}"))
           .start(port);
        try {
            new AmazonElasticSearch(
                "index",
                new AccessKeyId.Fake("access_key"),
                new SecretKey.Fake("secret_key"),
                new Region.Fake("ro"),
                new EsEndPoint.Fake("http://localhost:" + port + "/es/")
            ).delete("page", "document_id");
            MkQuery request = server.take();
            assertEquals("/es/index/page/document_id/", request.uri().toString());
            assertTrue("DELETE".equals(request.method()));
        } finally {
            server.stop();
        }
    }
    
    /**
     * AmazonEsRespository can tell if an index exists or not.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void tellsIfIndexExists() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
           .next(new MkAnswer.Simple(HttpStatus.SC_OK))
           .start(port);
        try {
            boolean exists = new AmazonElasticSearch(
                "present.index",
                new AccessKeyId.Fake("access_key"),
                new SecretKey.Fake("secret_key"),
                new Region.Fake("ro"),
                new EsEndPoint.Fake("http://localhost:" + port + "/es")
            ).exists();
            assertTrue(exists);
            MkQuery request = server.take();
            assertEquals("/es/present.index/", request.uri().toString());
            assertTrue("HEAD".equals(request.method()));
        } finally {
            server.stop();
        }
    }
    
    /**
     * AmazonEsRespository can tell if an index exists or not.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void tellsIfIndexDoesNotExist() throws Exception {
        int port = this.port();
        MkContainer server = new MkGrizzlyContainer()
           .next(new MkAnswer.Simple(HttpStatus.SC_NOT_FOUND))
           .start(port);
        try {
            boolean exists = new AmazonElasticSearch(
                "missing.index",
                new AccessKeyId.Fake("access_key"),
                new SecretKey.Fake("secret_key"),
                new Region.Fake("ro"),
                new EsEndPoint.Fake("http://localhost:" + port + "/es/")
            ).exists();
            assertFalse(exists);
            MkQuery request = server.take();
            assertEquals("/es/missing.index/", request.uri().toString());
            assertTrue("HEAD".equals(request.method()));
        } finally {
            server.stop();
        }
    }
    
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

        try {
            ElasticSearch es = new AmazonElasticSearch(
                "amihaiemilxtestrepo",
                new AccessKeyId.Fake("aws_key_id"),
                new SecretKey.Fake("secret_key"),
                new Region.Fake("us-west"),
                new EsEndPoint.Fake("http://localhost:" + port + "/elasticsearch")
            );
            SearchResultsPage srp = es.search(new SearchQuery("test", "page", 0, 10));
            assertTrue(srp.getTotalHits() == 27);
            assertTrue(srp.getResults().size() == 10);
            SearchResult third = srp.getResults().get(2);
            assertTrue(third.link().equals("http://amihaiemil.com/stuff/link3page/page.html"));
            assertTrue(third.category().equals("development"));
        
            SearchResult last = srp.getResults().get(9);
            assertTrue(last.link().equals("http://amihaiemil.com/some/other/page.html"));
            assertTrue(last.category().equals("mischelaneous"));
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
        try {
            ElasticSearch es = new AmazonElasticSearch(
                "amihaiemilxtestrepo",
                new AccessKeyId.Fake("aws_key_id"),
                new SecretKey.Fake("secret_key"),
                new Region.Fake("us-west"),
                new EsEndPoint.Fake("http://localhost:" + port + "/elasticsearch")
            );
            SearchResultsPage srp = es.search( new SearchQuery("test", "page", 0, 10));
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
     * Mock a WebPage for test.
     * @param url
     * @return Webpage instance.
     */
    private WebPage mockWebPage(String url) {
        WebPage page = new SnapshotWebPage();
        page.setUrl(url);

        page.setLinks(new HashSet<Link>());
        page.setTextContent("some content");
        page.setName("crawledpage.html");
        page.setTitle("crawledpage | title");
        return page;
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
}
