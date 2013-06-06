package org.gluewine.mailer;

/**
 * Exception thrown when a problem occurs sending out a mail.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class MailException extends Exception
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 7071648310283417673L;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param msg The cause of the exception.
     */
    public MailException(String msg)
    {
        super(msg);
    }

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param e The embedded cause.
     */
    public MailException(Throwable e)
    {
        super(e);
    }

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param msg The cause of the exception.
     * @param e The embedded cause.
     */
    public MailException(String msg, Throwable e)
    {
        super(msg, e);
    }
}
