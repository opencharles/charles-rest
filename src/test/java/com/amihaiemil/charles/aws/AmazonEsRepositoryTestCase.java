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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.Test;

import com.amazonaws.AmazonServiceException;
import com.amihaiemil.charles.Link;
import com.amihaiemil.charles.SnapshotWebPage;
import com.amihaiemil.charles.WebPage;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link AmazonEsRepository}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
@SuppressWarnings("resource")
public class AmazonEsRepositoryTestCase {

    /**
     * IllegalStateException is thrown on missing secret key property.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void illegalStateOnMissingSecretKey() throws Exception {
        List<WebPage> pages = new ArrayList<WebPage>();
        pages.add(this.mockWebPage("http://www.test.com/crawledpage.html"));
        pages.add(this.mockWebPage("https://www.test.com/stuff/crawledpage.html"));

        AmazonEsRepository repo = new AmazonEsRepository(
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

        AmazonEsRepository repo = new AmazonEsRepository(
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
        AmazonEsRepository repo = new AmazonEsRepository(
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
        	new AmazonEsRepository(
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
        	new AmazonEsRepository(
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
        	new AmazonEsRepository(
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
        	boolean exists = new AmazonEsRepository(
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
        	boolean exists = new AmazonEsRepository(
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
