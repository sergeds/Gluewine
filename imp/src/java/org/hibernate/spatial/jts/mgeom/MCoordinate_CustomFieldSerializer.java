package org.hibernate.spatial.jts.mgeom;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.core.com.vividsolutions.jts.geom.Coordinate_CustomFieldSerializer;

/**
 * Custom field serializer for MCoordinate.
 * @author fks/Frank Gevaerts
 */
public final class MCoordinate_CustomFieldSerializer
{
    /**
     * Serializes an instance.
     * @param streamWriter the writer
     * @param instance the instance to serialize
     * @throws SerializationException if serialization fails.
     */
    public static void serialize(SerializationStreamWriter streamWriter,
            MCoordinate instance) throws SerializationException
    {
        Coordinate_CustomFieldSerializer.serialize(streamWriter, instance);
    }
}

