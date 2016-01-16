package org.hibernate.collection.internal;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.core.java.util.Map_CustomFieldSerializerBase;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;

/**
 * Custom field serializer for org.hibernate.collection.PersistentSortedMap.
 *
 * @author fks/Frank Gevaerts
 */
@SuppressWarnings({"rawtypes", "checkstyle:typename" })
public final class PersistentSortedMap_CustomFieldSerializer extends CustomFieldSerializer<PersistentSortedMap>
{
    /**
     * Deserializes the content of the object from the
     * {@link SerializationStreamReader}.
     *
     * @param streamReader the {@link SerializationStreamReader} to read the
     *        object's content from
     * @param instance the object instance to deserialize
     *
     * @throws SerializationException if the deserialization operation is not
     *        successful
     */
    public static void deserialize(SerializationStreamReader streamReader, PersistentSortedMap instance) throws SerializationException
    {
        Map_CustomFieldSerializerBase.deserialize(streamReader, instance);
    }

    /**
     * Serializes the content of the object into the
     * {@link SerializationStreamWriter}.
     *
     * @param streamWriter the {@link SerializationStreamWriter} to write the
     *        object's content to
     * @param instance the object instance to serialize
     *
     * @throws SerializationException if the serialization operation is not
     *        successful
     */
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

