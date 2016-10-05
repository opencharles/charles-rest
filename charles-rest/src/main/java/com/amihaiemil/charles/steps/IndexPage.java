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
package com.amihaiemil.charles.steps;

import java.util.Arrays;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;

import com.amihaiemil.charles.DataExportException;
import com.amihaiemil.charles.LiveWebPage;
import com.amihaiemil.charles.SnapshotWebPage;
import com.amihaiemil.charles.WebPage;
import com.amihaiemil.charles.aws.AmazonEsRepository;

/**
 * Step to index a single page.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class IndexPage extends IntermediaryStep {

    /**
     * Action's logger.
     */
    private Logger logger;

	/**
	 * Command text.
	 */
	private String commandBody;

	/**
	 * Path to phantomJS.
	 */
	private String phantomPath;

	/**
	 * Es index.
	 */
	private String index;

	/**
	 * Ctor.
	 * @param commandBody Text of the index-page command.
	 * @param indexName Name of the Es index where the page will go.
	 * @param phantomjs Path to the phantomJS executable.
	 * @param logger Logger.
	 * @param next Next step to take.
	 */
    public IndexPage(
        String commandBody, String indexName,
        String phantomjs, Logger logger,
        Step next
    ) {
    	super(next);
        this.commandBody = commandBody;
        this.index = indexName;
        this.phantomPath = phantomjs;
        this.logger = logger;
    }

	@Override
	public void perform() {
		String link = this.getLink();
		WebPage snapshot = new SnapshotWebPage(
		    new LiveWebPage(this.phantomJsDriver(), link)
		);
        try {
            new AmazonEsRepository(this.index).export(Arrays.asList(snapshot));
        } catch (DataExportException e) {
            logger.error("Exception while indexing the page " + link, e);
            throw new IllegalStateException("Exception while indexing the page" + link, e);
		}
        this.next().perform();
    }

	private String getLink() {
		return this.commandBody.substring(commandBody.indexOf('('), commandBody.indexOf(')'));
	}

	public WebDriver phantomJsDriver() {
		DesiredCapabilities dc = new DesiredCapabilities();
        dc.setJavascriptEnabled(true);
        dc.setCapability(
            PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
            this.phantomPath
        );
        return new PhantomJSDriver(dc);
	}
}
