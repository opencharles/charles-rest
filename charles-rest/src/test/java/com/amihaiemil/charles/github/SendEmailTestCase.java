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


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Test;

import com.amihaiemil.charles.github.SendEmail;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
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
 * Unit tests for {@link SendEmail}
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public class SendEmailTestCase {

    /**
     * SendEmail can send an email envelope to the SMTP server.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void sendsEmaiLToSmtpServer() throws Exception  {
        String bind = "localhost";
        int port = this.port();
        GreenMail server = this.smtpServer(bind, port);
        server.start();

        SendEmail se = new SendEmail(
            new Postman.Default(
                new SMTP(
                    new Token("", "")
                        .access(new Protocol.SMTP(bind, port))
                )
            ), 
            new Envelope.MIME()
                .with(new StSender("Charles Michael <amihai.emil@gmail.com>"))
                .with(new StRecipient("commandergithub@test.com"))
                .with(new StSubject("test subject: test email"))
                .with(new EnPlain("hello, how are you? This is a test email..."))
        );

        try {
            se.perform();
            final MimeMessage[] messages = server.getReceivedMessages();
            assertTrue(messages.length == 1);
            for (final Message msg : messages) {
                assertTrue(msg.getFrom()[0].toString().contains("<amihai.emil@gmail.com>"));
                assertTrue(msg.getSubject().contains("test email"));
            }
        } finally {
            server.stop();
        }
    }
    
    /**
     * SendEmail can send an email when username, password, host and port are specified.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void sendsEmailFromGivenUser() throws Exception {
        String bind = "localhost";
        int port = this.port();
        GreenMail server = this.smtpServer(bind, port);
        server.start();
        
        System.setProperty("charles.smtp.username","mihai");
        System.setProperty("charles.smtp.password","");
        System.setProperty("charles.smtp.host","localhost");
        System.setProperty("charles.smtp.port", String.valueOf(port));

        SendEmail se = new SendEmail("amihaiemil@gmail.com", "hello", "hello, how are you?");

        try {
            se.perform();
            final MimeMessage[] messages = server.getReceivedMessages();
            assertTrue(messages.length == 1);
            for (final Message msg : messages) {
                assertTrue(msg.getFrom()[0].toString().contains("mihai"));
                assertTrue(msg.getSubject().contains("hello"));
            }
        } finally {
            server.stop();
        }
    }

    /**
     * SendEmail does not send any email if username and/or password are not specified.
     * @throws Exception If something goes wrong.
     */
    @Test(expected = IllegalStateException.class)
    public void uninitializedPostman() {
        SendEmail se = new SendEmail("amihaiemil@gmail.com", "hello", "hello, how are you?");
        se.perform();
    }

    /**
     * Mock a smtp server.
     * @return GreenMail smtp server.
     * @throws IOException If something goes wrong.
     */
    public GreenMail smtpServer(String bind, int port) throws IOException {
        return new GreenMail(
            new ServerSetup(
                port, bind,
                ServerSetup.PROTOCOL_SMTP
            )
        );
    }
    
    /**
     * Find a free port.
     * @return A free port.
     * @throws IOException If something goes wrong.
     */
    private int port() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @After
    public void clean() {
        System.clearProperty("charles.smtp.username");
        System.clearProperty("charles.smtp.password");
        System.clearProperty("charles.smtp.host");
        System.clearProperty("charles.smtp.port");
    }
}
