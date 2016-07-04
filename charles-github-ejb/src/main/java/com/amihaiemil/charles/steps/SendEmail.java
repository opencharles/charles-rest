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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.email.Protocol;
import com.jcabi.email.Token;
import com.jcabi.email.enclosure.EnPlain;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.email.stamp.StSubject;
import com.jcabi.email.wire.SMTP;

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
	 * @param to Recipient of this mail.
	 * @param subject Mail subject.
	 * @param message Contents of the mail.
	 */
	public SendEmail(String to,  String subject, String message) {
		this(to, subject, message, LoggerFactory.getLogger(SendEmail.class));
	}
	
	/**
	 * Constructor.
	 * @param to Recipient of this mail.
	 * @param subject Mail subject.
	 * @param message Contents of the mail.
	 * @param logger Action logger.
	 */
	public SendEmail(String to,  String subject, String message, Logger logger) {
		String username = System.getProperty("charles.smtp.username");
		String pwd = System.getProperty("charles.smtp.password");
		if(username != null && pwd != null) {
            String host = System.getProperty("charles.smtp.host");
            String port = System.getProperty("charles.smtp.port");
            if(!StringUtils.isEmpty(host) && !StringUtils.isEmpty(port)) {
            	Protocol prot = new Protocol.SMTP(host, Integer.valueOf(port));
            	if(Boolean.valueOf(System.getProperty("charles.smtp.secure"))) {
            		prot = new Protocol.SMTPS(host, Integer.valueOf(port));
            	}
            	this.postman = new Postman.Default(
                    new SMTP(new Token(username, pwd).access(prot))
                );
            } else {
            	this.postman = new Postman.Default(
                    new SMTP(
                        new Token(username, pwd)
                            .access(new Protocol.SMTPS("smtp.gmail.com", 465))
                    )
                );
            }
            this.env = new Envelope.MIME()
                .with(new StSender(username))
                .with(new StRecipient(to))
                .with(new StSubject(subject))
                .with(new EnPlain(message));
		}
		this.logger = logger;
	}
	
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
		logger.info("Sending e-mail...");
		if(this.postman == null) {
			logger.warn("Uninitialized postman (username and/or password missing). Cannot send email!");
			return false;
		} else {
		    try {
			    this.postman.send(env);
			    logger.info("E-mail sent successfully!");
			    return true;
		    } catch (IOException e) {
			    logger.error("Error when sending the email " + e.getMessage(), e);
		    }
		}
		return false;
	}

}
