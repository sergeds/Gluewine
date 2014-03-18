/**************************************************************************
 *
 * Gluewine GXO Server Module
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
package org.gluewine.gxo_server;

import com.thoughtworks.xstream.XStream;

/**
 * Defines a GxoServer.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface GxoServer
{
    // ===========================================================================
    /**
     * Registers a class that can be instantiated and made available through GXO.
     *
     * @param cl The class to register.
     */
    void setInstantiatableService(Class<?> cl);

    // ===========================================================================
    /**
     * Deregisters a class.
     *
     * @param cl The class to register.
     */
    void unsetInstantiatableService(Class<?> cl);

    // ===========================================================================
    /**
     * Returns the stream used.
     *
     * @return The XStream being used.
     */
    XStream getXStream();
}
