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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.amihaiemil.charles.steps.Step;
import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos.RepoCreate;
import com.jcabi.github.Stars;
import com.jcabi.github.mock.MkGithub;

/**
 * Unit tests for {@link StarRepo}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class StarRepoTestCase {

    /**
     * StarRepo can successfully star a given repository.
     * @throws Exception If something goes wrong.
     */
	@Test
	public void starsRepo() throws Exception {
		Logger logger = Mockito.mock(Logger.class);
		Mockito.doNothing().when(logger).info(Mockito.anyString());
		Mockito.doThrow(new IllegalStateException("Unexpected error; test failed")).when(logger).error(Mockito.anyString());

		Repo repo = this.mockGithubRepo();
        Step sr = new StarRepo(repo, logger, Mockito.mock(Step.class));
        assertFalse(repo.stars().starred());
        sr.perform();
        assertTrue(repo.stars().starred());
    }
	
	/**
     * StarRepo tries to star a repo twice.
     * @throws Exception If something goes wrong.
     */
	@Test
	public void starsRepoTwice() throws Exception {
		Logger logger = Mockito.mock(Logger.class);
		Mockito.doNothing().when(logger).info(Mockito.anyString());
		Mockito.doThrow(new IllegalStateException("Unexpected error; test failed")).when(logger).error(Mockito.anyString());

		Repo repo = this.mockGithubRepo();
        Step sr = new StarRepo(repo, logger, Mockito.mock(Step.class));
        assertFalse(repo.stars().starred());
        sr.perform();
        sr.perform();
        assertTrue(repo.stars().starred());
    }

    /**
     * StarRepo did not star the repository due to an IOException. 
     * StarRepo.perform() should return true anyway because it's not a critical operation and we
     * shouldn't fail the whole process just because of this.
     * 
     * This test expects an RuntimeException (we mock the logger in such a way) because it's the easiest way
     * to find out if the flow entered the catch block.
     * @throws IOException If something goes wrong.
     */
    @Test(expected = RuntimeException.class)
    public void repoStarringFails() throws IOException {
		Logger logger = Mockito.mock(Logger.class);
		Mockito.doNothing().when(logger).info(Mockito.anyString());
		Mockito.doThrow(new RuntimeException("Excpected excetion; all is ok!")).when(logger).error(
		    Mockito.anyString(), Mockito.any(IOException.class)
		);
		
		Repo repo = Mockito.mock(Repo.class);
		Stars stars = Mockito.mock(Stars.class);
		Mockito.when(stars.starred()).thenReturn(false);
		Mockito.doThrow(new IOException()).when(stars).star();
		Mockito.when(repo.stars()).thenReturn(stars);
		
		StarRepo sr = new StarRepo(repo, logger, Mockito.mock(Step.class));
		sr.perform();
	}
	/**
	 * Return a Github Repo mock for test.
	 * @return Repo.
	 * @throws Exception
	 */
	public Repo mockGithubRepo() throws Exception {
		Github gh = new MkGithub("amihaiemil");
    	return gh.repos().create(
    		new RepoCreate("amihaiemil.github.io", false)
    	);
	}
}
