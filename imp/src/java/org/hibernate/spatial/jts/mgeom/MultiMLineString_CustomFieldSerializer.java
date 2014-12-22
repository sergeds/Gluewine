package org.hibernate.spatial.jts.mgeom;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.core.com.vividsolutions.jts.geom.MultiLineString_CustomFieldSerializer;

/**
 * Custom field serializer for MultiMLineString.
 * @author fks/Frank Gevaerts
 */
public final class MultiMLineString_CustomFieldSerializer
{
    /**
     * Serializes an instance.
     * @param streamWriter the writer
     * @param instance the instance to serialize
     * @throws SerializationException if serialization fails.
     */
    public static void serialize(SerializationStreamWriter streamWriter,
            MultiMLineString instance) throws SerializationException
    {
        MultiLineString_CustomFieldSerializer.serialize(streamWriter, instance);
    }
}

