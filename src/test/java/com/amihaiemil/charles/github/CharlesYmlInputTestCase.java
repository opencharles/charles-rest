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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit tests for {@link CharlesYmlInput}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.1
 */
public final class CharlesYmlInputTestCase {
    
    /**
     * CharlesYmlInput can read the commanders list.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void readsCommanders() throws IOException {
        final CharlesYml charles = new CharlesYmlInput(
            new ByteArrayInputStream(
                "commanders:\n  - charlesmike\n  - amihaiemil".getBytes()
            )
        );
        final List<String> commanders = charles.commanders();
        MatcherAssert.assertThat(commanders, Matchers.hasSize(2));
        MatcherAssert.assertThat(
            commanders.get(0), Matchers.equalTo("amihaiemil")
        );
        MatcherAssert.assertThat(
            commanders.get(1), Matchers.equalTo("charlesmike")
        );
        MatcherAssert.assertThat(charles.tweet(), Matchers.is(false));
    }
    
    /**
     * CharlesYmlInput can read the tweet option.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void readsTweetOption() throws IOException {
        final CharlesYml charles = new CharlesYmlInput(
            new ByteArrayInputStream("tweet: true".getBytes())
        );
        MatcherAssert.assertThat(charles.tweet(), Matchers.is(true));
        MatcherAssert.assertThat(charles.commanders(), Matchers.hasSize(0));
    }
    
    /**
     * CharlesYmlInput can read the commanders list.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void readsAllOptions() throws IOException {
        final CharlesYml charles = new CharlesYmlInput(
            new ByteArrayInputStream(
                "commanders:\n  - charlesmike\n  - amihaiemil\ntweet: true"
                .getBytes()
            )
        );
        final List<String> commanders = charles.commanders();
        MatcherAssert.assertThat(commanders, Matchers.hasSize(2));
        MatcherAssert.assertThat(
            commanders.get(0), Matchers.equalTo("amihaiemil")
        );
        MatcherAssert.assertThat(
            commanders.get(1), Matchers.equalTo("charlesmike")
        );
        MatcherAssert.assertThat(charles.tweet(), Matchers.is(true));
    }

    /**
     * CharlesYmlInput can read the specified driver.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void readsDriver() throws IOException {
        final CharlesYml charles = new CharlesYmlInput(
            new ByteArrayInputStream(
                "commanders:\n  - charlesmike\n  - amihaiemil\ndriver: phantomjs"
                .getBytes()
            )
        );
        MatcherAssert.assertThat(
            charles.driver(), Matchers.equalTo("phantomjs")
        );
    }

    /**
     * CharlesYmlInput can read the default driver if it's not specified.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void readsDefaultDriver() throws IOException {
        final CharlesYml charles = new CharlesYmlInput(
            new ByteArrayInputStream(
                "commanders:\n  - charlesmike\n  - amihaiemil\ntweet: true"
                .getBytes()
            )
        );
        MatcherAssert.assertThat(
            charles.driver(), Matchers.equalTo("chrome")
        );
    }
}
