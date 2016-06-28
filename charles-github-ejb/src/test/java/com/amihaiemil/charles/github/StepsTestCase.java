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
 *  3)Neither the name of charles-github-ejb nor the names of its
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

import java.util.Arrays;


import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

/**
 * Unit tests for {@link Steps}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 */
public class StepsTestCase {

	/**
	 * Steps can perform 1 single step.
	 */
	@Test
    public void oneStepIsPerformed() {
    	Step s = Mockito.mock(Step.class);
    	Mockito.when(s.perform()).thenReturn(true);

    	Steps steps = new Steps(Arrays.asList(s));
    	assertTrue(steps.perform());
    }
	
	/**
	 * Steps can perform more steps.
	 */
	@Test
    public void moreStepsArePeformed() {
    	Step s1 = Mockito.mock(Step.class);
    	Mockito.when(s1.perform()).thenReturn(true);
    	Step s2 = Mockito.mock(Step.class);
    	Mockito.when(s2.perform()).thenReturn(true);
    	Step s3 = Mockito.mock(Step.class);
    	Mockito.when(s3.perform()).thenReturn(true);

    	Steps steps = new Steps(Arrays.asList(s1, s2, s3));
    	assertTrue(steps.perform());
    }

	/**
	 * Steps stops performing the steps and returns false when 1 step fails.
	 */
	@Test
	public void stopsPeformingWhenOneStepFails() {
		Step s1 = Mockito.mock(Step.class);
    	Mockito.when(s1.perform()).thenReturn(true);
    	Step s2 = Mockito.mock(Step.class);
    	Mockito.when(s2.perform()).thenReturn(false);
    	Step s3 = Mockito.mock(Step.class);
    	Mockito.when(s3.perform()).thenReturn(true);

    	Steps steps = new Steps(Arrays.asList(s1, s2, s3));
    	assertFalse(steps.perform());
	}
}
