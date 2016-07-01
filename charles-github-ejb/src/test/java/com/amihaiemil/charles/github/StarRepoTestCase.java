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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.amihaiemil.charles.github.StarRepo;
import com.amihaiemil.charles.steps.Step;
import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos.RepoCreate;
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
		Mockito.doNothing().when(logger).error(Mockito.anyString());

		Repo repo = this.mockGithubRepo();
        Step sr = new StarRepo(repo, logger);
        assertFalse(repo.stars().starred());
        assertTrue(sr.perform());
        assertTrue(repo.stars().starred());
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
