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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;

import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.http.HttpResponse;
import com.amihaiemil.charles.rest.model.SearchResult;
import com.amihaiemil.charles.rest.model.SearchResultsPage;

/**
 * Test cases for {@link SearchResponseHandler}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class SearchResponseHandlerTestCase {

    /**
     * SearchResponseHandler can build the search results page from an
     * aws HttpResponse
     * @throws Exception If something goes wrong.
     */
    @Test
    public void buildsSearchResultsPage() throws Exception {
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getStatusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        Mockito.when(response.getContent()).thenReturn(
             new FileInputStream(
                new File("src/test/resources/esSearchResponse.json")
             )
        );
        SearchResultsPage page = new SearchResponseHandler().handle(response);
        assertTrue(page.getTotalHits() == 27);
        assertTrue(page.getResults().size() == 10);
        SearchResult first = page.getResults().get(0);
        assertTrue(first.link().equals("http://amihaiemil.com/page.html"));
        assertTrue(first.category().equals("tech"));
    
        SearchResult last = page.getResults().get(9);
        assertTrue(last.link().equals("http://amihaiemil.com/some/other/page.html"));
        assertTrue(last.category().equals("mischelaneous"));
    }
    
    /**
     * SearchResponseHandler can throw AmazonServiceException in case the response status is not
     * in the expected range.
     */
    @Test
    public void throwsAwsExceptionOnBadStatus() {
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getStatusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        try {
            new SearchResponseHandler().handle(response);
            fail("AmazonServiceException should have been thrown!");
        } catch (AmazonServiceException awsEx) {
               assertTrue(awsEx.getErrorMessage().equals("Unexpected status: 404"));
            assertTrue(awsEx.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND);
        }
    }
}
