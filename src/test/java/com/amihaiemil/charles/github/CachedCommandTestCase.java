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
package com.amihaiemil.charles.github;

import java.io.IOException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit tests for {@link CachedCommand}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
public final class CachedCommandTestCase {
    
    /**
     * CachedCommand can cache the agent login.
     * @throws IllegalStateException If something goes wrong.
     * @throws IOException If something goes wrong
     */
    @Test
    public void cachesAgentLogin() throws IOException {
        final Command uncached = Mockito.mock(Command.class);
        Mockito.when(uncached.agentLogin())
            .thenReturn("charlesmike")
            .thenThrow(new IllegalStateException("Agent login should have been cached!"));
        final Command cached = new CachedCommand(uncached);
        MatcherAssert.assertThat(
            cached.agentLogin(), Matchers.equalTo("charlesmike")
        );
        MatcherAssert.assertThat(
            cached.agentLogin(), Matchers.equalTo("charlesmike")
        );
    }
    
    /**
     * CachedCommand can cache the author's email.
     * @throws IllegalStateException If something goes wrong.
     * @throws IOException If something goes wrong
     */
    @Test
    public void cachesAuthorEmail() throws IOException {
        final Command uncached = Mockito.mock(Command.class);
        Mockito.when(uncached.authorEmail())
            .thenReturn("amihaiemil@gmail.com")
            .thenThrow(new IllegalStateException("Author's email should have been cached!"));
        final Command cached = new CachedCommand(uncached);
        MatcherAssert.assertThat(
            cached.authorEmail(), Matchers.equalTo("amihaiemil@gmail.com")
        );
        MatcherAssert.assertThat(
            cached.authorEmail(), Matchers.equalTo("amihaiemil@gmail.com")
        );
    }
}
