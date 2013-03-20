/**************************************************************************
 *
 * Gluewine GXO Server Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version
 * 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 **************************************************************************/
package org.gluewine.gxo_server;

import com.thoughtworks.xstream.XStream;

/**
 * Defines a class that offers additional XStream converters to the current stream.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface XStreamConverterProvider
{
    // ===========================================================================
    /**
     * Requests the class to register additional converters to the
     * given stream.
     *
     * @param stream The stream to update.
     */
    void registerConverters(XStream stream);
}
