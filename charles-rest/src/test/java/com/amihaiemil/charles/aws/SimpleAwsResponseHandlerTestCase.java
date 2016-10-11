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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.http.HttpResponse;

/**
 * Test cases for {@link SimpleAwsResponseHandler}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class SimpleAwsResponseHandlerTestCase {

	/**
	 * SimpleAwsResponseHandler returns the plain response as is, since the status is
	 * in range [200, 300)
	 * @throws Exception If something goes wrong.
	 */
	@Test
	public void responseStatusInRange() throws Exception {
		HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getStatusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        String content = "This content should not be affected!";
        Mockito.when(response.getContent()).thenReturn(
            new ByteArrayInputStream(content.getBytes())
        );
        SimpleAwsResponseHandler handler = new SimpleAwsResponseHandler(false);
        assertFalse(handler.needsConnectionLeftOpen());
        HttpResponse handled = handler.handle(response);
        assertTrue(handled.getStatusCode() == HttpURLConnection.HTTP_OK);
        String expected = new String(IOUtils.toByteArray(handled.getContent()));
        assertTrue(expected.equals(content));
    }

    /**
     * {@link SimpleAwsResponseHandler} can throw AmazonServiceException
     * in case the response status is not in the expected range.
     */
    @Test
    public void throwsAwsExceptionOnBadStatus() {
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getStatusCode()).thenReturn(HttpURLConnection.HTTP_BAD_METHOD);
        try {
	        new SimpleAwsResponseHandler(true).handle(response);
            fail("AmazonServiceException should have been thrown!");
        } catch (AmazonServiceException awsEx) {
	   	    assertTrue(awsEx.getErrorMessage().equals("Unexpected status: 405"));
	        assertTrue(awsEx.getStatusCode() == HttpURLConnection.HTTP_BAD_METHOD);
        }
	}

}
