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
