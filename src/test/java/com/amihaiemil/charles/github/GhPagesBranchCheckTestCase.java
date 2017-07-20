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

package com.amihaiemil.charles.github;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

/**
 * Unit tests for {@link GhPagesBranchCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class GhPagesBranchCheckTestCase {

    /**
     * GhPagesBranchCheck can tell if the gh-pages branch exists in the repo.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void ghpagesBranchExists() throws Exception {
        Command com = Mockito.mock(Command.class);
        CachedRepo crepo = Mockito.mock(CachedRepo.class);
        Mockito.when(crepo.hasGhPagesBranch()).thenReturn(true);
        Mockito.when(com.repo()).thenReturn(crepo);

        GhPagesBranchCheck gpc = new GhPagesBranchCheck(
            new Step.Fake(true), new Step.Fake(false)
        );
        gpc.perform(com, Mockito.mock(Logger.class));
    }

    /**
     * GhPagesBranchCheck can tell if the gh-pages branch does not exist in the repo.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void ghpagesBranchDoesntExist() throws Exception {
        Command com = Mockito.mock(Command.class);
        CachedRepo crepo = Mockito.mock(CachedRepo.class);
        Mockito.when(crepo.hasGhPagesBranch()).thenReturn(false);
        Mockito.when(com.repo()).thenReturn(crepo);

        GhPagesBranchCheck gpc = new GhPagesBranchCheck(
            new Step.Fake(false), new Step.Fake(true)
        );
        gpc.perform(com, Mockito.mock(Logger.class));
    }

}
