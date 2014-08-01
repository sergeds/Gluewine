package org.hibernate.collection.internal;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.core.java.util.Map_CustomFieldSerializerBase;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;

/**
 * Custom field serializer for {@link java.util.PersistentSortedMap}.
 *
 * @author fks/Frank Gevaerts
 */
@SuppressWarnings("rawtypes")
public final class PersistentSortedMap_CustomFieldSerializer extends CustomFieldSerializer<PersistentSortedMap>
{
    public static void deserialize(SerializationStreamReader streamReader, PersistentSortedMap instance) throws SerializationException
    {
        Map_CustomFieldSerializerBase.deserialize(streamReader, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, PersistentSortedMap instance) throws SerializationException
    {
        Map_CustomFieldSerializerBase.serialize(streamWriter, instance);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, PersistentSortedMap instance) throws SerializationException
    {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, PersistentSortedMap instance) throws SerializationException
    {
        serialize(streamWriter, instance);
    }
}

