package org.gluewine.mailer;

/**
 * Defines the mailer interface.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface Mailer
{
    // ===========================================================================
    /**
     * Sends out a mail to the recipients specified.
     *
     * @param subject The subject of the mail.
     * @param body The body (text) of the mail.
     * @param recipients The recipients.
     * @throws MailException If a problem occurs sending out the mail.
     */
    void sendMail(String subject, String body, String ... recipients) throws MailException;
}
