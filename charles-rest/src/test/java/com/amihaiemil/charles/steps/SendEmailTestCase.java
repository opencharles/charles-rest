package com.amihaiemil.charles.steps;


import java.io.IOException;
import java.net.ServerSocket;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

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

import static org.junit.Assert.*;

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
