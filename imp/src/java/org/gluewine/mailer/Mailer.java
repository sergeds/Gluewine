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
