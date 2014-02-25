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
