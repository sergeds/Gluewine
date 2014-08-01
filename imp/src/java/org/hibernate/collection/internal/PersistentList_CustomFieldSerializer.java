package org.hibernate.collection.internal;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.core.java.util.Collection_CustomFieldSerializerBase;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;

/**
 * Custom field serializer for {@link java.util.PersistentList}.
 *
 * @author fks/Frank Gevaerts
 */
@SuppressWarnings("rawtypes")
public final class PersistentList_CustomFieldSerializer extends CustomFieldSerializer<PersistentList>
{
    public static void deserialize(SerializationStreamReader streamReader, PersistentList instance) throws SerializationException
    {
        Collection_CustomFieldSerializerBase.deserialize(streamReader, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, PersistentList instance) throws SerializationException
    {
        Collection_CustomFieldSerializerBase.serialize(streamWriter, instance);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, PersistentList instance) throws SerializationException
    {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, PersistentList instance) throws SerializationException
    {
        serialize(streamWriter, instance);
    }
}

