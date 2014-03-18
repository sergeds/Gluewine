/**************************************************************************
 *
 * Gluewine Mailing Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/
package org.gluewine.mailer.impl;

import java.util.Properties;

import javax.activation.CommandMap;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.gluewine.core.Glue;
import org.gluewine.core.RunOnActivate;
import org.gluewine.mailer.MailException;
import org.gluewine.mailer.Mailer;
import org.gluewine.utils.ErrorLogger;

/**
 * The MailImpl is a processor that is invoked every time a mail needs to be sent out.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class MailerImpl implements Mailer
{
    // ===========================================================================
    /**
     * The name of the section containing the properties for the mailer.
     */
    @Glue(properties = "mail.properties")
    private Properties properties;

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    /**
     * Initialize the mailer.
     */
    @RunOnActivate
    public void launch()
    {
        CommandMap.setDefaultCommandMap(new GluewineMailcapCommandMap());
    }

    // ===========================================================================
    @Override
    public void sendMail(String subject, String body, String... recipients) throws MailException
    {
        try
        {
            launch();
            // Create a session object, using the properties (smtp server).
            Session session = null;

            if (Boolean.parseBoolean(properties.getProperty("authentication", "false")))
            {
                session = Session.getDefaultInstance(properties, new javax.mail.Authenticator()
                {
                    protected PasswordAuthentication getPasswordAuthentication()
                    {
                        return new PasswordAuthentication(properties.getProperty("userid"), properties.getProperty("password"));
                    }
                });
            }
            else
                session = Session.getDefaultInstance(properties);

            session.setDebug(false);

            Transport transport = session.getTransport(properties.getProperty("protocol"));

            MimeMessage mimemessage = new MimeMessage(session);
            mimemessage.setFrom(new InternetAddress(properties.getProperty("sender"))); // Set sender address

            InternetAddress[] recAdresses = new InternetAddress[recipients.length];
            int i = 0;
            for (String recipient : recipients)
                recAdresses[i++] = new InternetAddress(recipient);

            mimemessage.setRecipients(RecipientType.TO, recAdresses); // Set the recipients of the mail.

            MimeBodyPart mimebodypart = new MimeBodyPart();
            if (body != null)
                mimebodypart.setText(body); // Set the message as mail body.
            else
                mimebodypart.setText(""); // No message ==> Empty message body.

            MimeMultipart mimemultipart = new MimeMultipart();
            mimemultipart.addBodyPart(mimebodypart);

            if (subject != null)
                mimemessage.setSubject(subject); // If subject is present, set it.
            else
                mimemessage.setSubject(""); // No subject param present ==> Empty mail subject.
            mimemessage.setContent(mimemultipart);

            if (logger.isDebugEnabled())
            {
                StringBuilder b = new StringBuilder();
                for (String s : recipients)
                    b.append(s).append(" ");
                logger.debug("Sending mail to : " + b.toString().trim());
            }

            transport.connect();
            transport.sendMessage(mimemessage, mimemessage.getRecipients(Message.RecipientType.TO));
            transport.close();
        }
        catch (Throwable e)
        {
            try
            {
                Class<?> c = Class.forName("com.sun.mail.smtp.SMTPTransport");
                System.out.println("com.sun.mail.smtp.SMTPTransport: " + c.getClassLoader());

                System.out.println("CommandMap: " + CommandMap.class.getClassLoader());

                c = Class.forName("javax.activation.UnsupportedDataTypeException");
                System.out.println("javax.activation.UnsupportedDataTypeException: " + c.getClassLoader());
            }
            catch (Throwable e1)
            {
                e1.printStackTrace();
            }


            ErrorLogger.log(getClass(), e);
            throw new MailException("An error occurred sending a mail.", e);
        }
    }
}
