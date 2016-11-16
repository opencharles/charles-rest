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
package com.amihaiemil.charles.github;

import javax.json.Json;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import com.amihaiemil.charles.steps.Step;

/**
 * Unit tests for {@link DeleteIndexCommandCheck}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class DeleteIndexCommandCheckTestCase {

    /**
     * DeleteIndexCommandCheck can see that given reponame equals the actual reponame
     * @throws Exception If something goes wrong.
     */
    @Test
    public void repoNameEqualsActual() throws Exception {
        Command com = Mockito.mock(Command.class);
        Mockito.when(com.json()).thenReturn(
            Json.createObjectBuilder()
                .add("body", "@charlesmike delete `eva` index")
                .build()
        );
        CommandedRepo crepo = Mockito.mock(CommandedRepo.class);
        Mockito.when(crepo.name()).thenReturn("eva");
        Mockito.when(com.repo()).thenReturn(crepo);

        Step onTrue = Mockito.mock(Step.class);
        Mockito.doNothing().when(onTrue).perform();
        Step onFalse = Mockito.mock(Step.class);
        Mockito.doThrow(new IllegalStateException("This step should not have been executed!")).when(onFalse).perform();

        DeleteIndexCommandCheck dc = new DeleteIndexCommandCheck(
            com, Mockito.mock(Logger.class), onTrue, onFalse
        );
        dc.perform();
    }

    /**
     * DeleteIndexCommandCheck can see that given reponame does not equal the actual reponame
     * @throws Exception If something goes wrong.
     */
    @Test
    public void repoNameNotEqualsActual() throws Exception {
        Command com = Mockito.mock(Command.class);
        Mockito.when(com.json()).thenReturn(
            Json.createObjectBuilder()
                .add("body", "@charlesmike delete `evamisspelled` index")
                .build()
        );
        CommandedRepo crepo = Mockito.mock(CommandedRepo.class);
        Mockito.when(crepo.name()).thenReturn("eva");
        Mockito.when(com.repo()).thenReturn(crepo);

        Step onTrue = Mockito.mock(Step.class);
        Mockito.doThrow(new IllegalStateException("This step should not have been executed!")).when(onTrue).perform();
        Step onFalse = Mockito.mock(Step.class);
        Mockito.doNothing().when(onFalse).perform();

        DeleteIndexCommandCheck dc = new DeleteIndexCommandCheck(
            com, Mockito.mock(Logger.class), onTrue, onFalse
        );
        dc.perform();
    }

    /**
     * DeleteIndexCommandCheck goes onFalse if the given repoName is not between back-apostrophes.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void missingBackApostrophes() throws Exception {
        Command com = Mockito.mock(Command.class);
        Mockito.when(com.json()).thenReturn(
            Json.createObjectBuilder()
                .add("body", "@charlesmike delete eva index")
                .build()
        );
        CommandedRepo crepo = Mockito.mock(CommandedRepo.class);
        Mockito.when(crepo.name()).thenReturn("eva");
        Mockito.when(com.repo()).thenReturn(crepo);

        Step onTrue = Mockito.mock(Step.class);
        Mockito.doThrow(new IllegalStateException("This step should not have been executed!")).when(onTrue).perform();
        Step onFalse = Mockito.mock(Step.class);
        Mockito.doNothing().when(onFalse).perform();

        DeleteIndexCommandCheck dc = new DeleteIndexCommandCheck(
            com, Mockito.mock(Logger.class), onTrue, onFalse
        );
        dc.perform();
    }
}
