package org.gluewine.mailer.impl;

import javax.activation.DataContentHandler;
import javax.activation.MailcapCommandMap;

/**
 * This command map allows to be used even if the Activation classes
 * are loaded by the BootClassLoader.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineMailcapCommandMap extends MailcapCommandMap
{
    // ===========================================================================
    @Override
    public DataContentHandler createDataContentHandler(String s)
    {
        switch (s)
        {
            case "text/html" :
                return new com.sun.mail.handlers.text_html();

            case "text/xml" :
                return new com.sun.mail.handlers.text_xml();

            case "text/plain" :
                return new com.sun.mail.handlers.text_plain();

            case "multipart/mixed" :
            case "multipart/*" :
                return new com.sun.mail.handlers.multipart_mixed();

            case "message/rfc822" :
                return new com.sun.mail.handlers.message_rfc822();

            default :
                return super.createDataContentHandler(s);
        }
    }
}
