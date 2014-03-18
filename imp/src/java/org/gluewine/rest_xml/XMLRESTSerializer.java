/**************************************************************************
 *
 * Gluewine REST XML Module
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
package org.gluewine.rest_xml;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.gluewine.core.RepositoryListener;
import org.gluewine.gxo_server.XStreamConverterProvider;
import org.gluewine.gxo_server.XStreamProvider;
import org.gluewine.rest.AbstractRESTSerializer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Serializes/deserializes to/from XML.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class XMLRESTSerializer extends AbstractRESTSerializer implements RepositoryListener<Object>
{
    // ===========================================================================
    /**
     * The actual serializer.
     */
    private XStream stream = new XStream(new StaxDriver());

    /**
     * Stream converters.
     */
    private Set<XStreamConverterProvider> providers = new HashSet<XStreamConverterProvider>();

    // ===========================================================================
    @Override
    public String getFormat()
    {
        return "xml";
    }

    // ===========================================================================
    @Override
    public String getResponseMIME()
    {
        return "application/xml";
    }

    // ===========================================================================
    @Override
    public String serialize(Object o) throws IOException
    {
        if (o instanceof String) return (String) o;
        return stream.toXML(o);
    }

    // ===========================================================================
    @Override
    protected Object deserializeObject(Class<?> cl, String str) throws IOException
    {
        if (cl.equals(String.class)) return str;
        return stream.fromXML(str);
    }

    // ===========================================================================
    @Override
    public void registered(Object t)
    {
        if (t instanceof XStreamProvider)
            stream = ((XStreamProvider) t).getXStream();

        if (t instanceof XStreamConverterProvider)
        {
            providers.add((XStreamConverterProvider) t);
            relinkConverters();
        }
    }

    // ===========================================================================
    /**
     * Relinks all registered converters.
     */
    private void relinkConverters()
    {
        for (XStreamConverterProvider prov : providers)
            prov.registerConverters(stream);
    }

    // ===========================================================================
    @Override
    public void unregistered(Object t)
    {
        if (t instanceof XStreamConverterProvider) providers.remove(t);
    }
}
