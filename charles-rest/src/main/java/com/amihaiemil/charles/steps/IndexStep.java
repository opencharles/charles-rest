package com.amihaiemil.charles.steps;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class IndexStep extends IntermediaryStep{

	public IndexStep(Step next) {
		super(next);
	}

	protected WebDriver phantomJsDriver() {
		String phantomJsExecPath =  System.getProperty("phantomjsExec");
	    if(phantomJsExecPath == null || "".equals(phantomJsExecPath)) {
	        phantomJsExecPath = "/usr/local/bin/phantomjs";
	    }
		DesiredCapabilities dc = new DesiredCapabilities();
        dc.setJavascriptEnabled(true);
        dc.setCapability(
            PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
            phantomJsExecPath
        );
        return new PhantomJSDriver(dc);
	}
	
}
