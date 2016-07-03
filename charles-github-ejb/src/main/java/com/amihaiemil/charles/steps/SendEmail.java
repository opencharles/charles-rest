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
package com.amihaiemil.charles.steps;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;

/**
 * Step where an email is sent.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class SendEmail implements Step {

	/**
	 * Delivery system.
	 */
	private Postman postman;
	
	/**
	 * Email.
	 */
	private Envelope env;
	
	/**
	 * Logger.
	 */
	private Logger logger;
	
	/**
	 * Constructor.
	 * @param postman "postman" that delivers the email.
	 * @param env Email data in an envelope.
	 */
	public SendEmail(Postman postman, Envelope env) {
		this(postman, env, LoggerFactory.getLogger(SendEmail.class));
	}

	/**
	 * Constructor.
	 * @param postman "postman" that delivers the email.
	 * @param env Email data in an envelope.
	 * @param logger Action logger.
	 */
	public SendEmail(Postman postman, Envelope env, Logger logger) {
		this.postman = postman;
		this.env = env;
		this.logger = logger;
	}
	
	/**
	 * Send the email.
	 */
	@Override
	public boolean perform() {
		try {
			logger.info("Sending e-mail...");
			this.postman.send(env);
			logger.info("E-mail sent successfully!");
		} catch (IOException e) {
			logger.error("Error when sending the email " + e.getMessage(), e);
			e.printStackTrace();
		}
		return false;
	}

}
