package org.hibernate.spatial.jts.mgeom;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.core.com.vividsolutions.jts.geom.LineString_CustomFieldSerializer;

/**
 * Custom field serializer for MLineString.
 * @author fks/Frank Gevaerts
 */
public final class MLineString_CustomFieldSerializer
{
    /**
     * Serializes an instance.
     * @param streamWriter the writer
     * @param instance the instance to serialize
     * @throws SerializationException if serialization fails.
     */
    public static void serialize(SerializationStreamWriter streamWriter,
            MLineString instance) throws SerializationException
    {
        LineString_CustomFieldSerializer.serialize(streamWriter, instance);
    }
}

