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
