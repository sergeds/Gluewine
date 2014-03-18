/**************************************************************************
 *
 * Gluewine GXO Hibernate Module
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
package org.gluewine.gxo_hibernate;

import org.gluewine.gxo_server.XStreamConverterProvider;
import org.gluewine.gxo_server.XStreamProvider;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedSetConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Registers Hibernate converters.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class HibernateConverterProvider implements XStreamConverterProvider, XStreamProvider
{
    // ===========================================================================
    @Override
    public void registerConverters(XStream stream)
    {
        stream.registerConverter(new HibernateProxyConverter());
        stream.registerConverter(new HibernatePersistentCollectionConverter(stream.getMapper()));
        stream.registerConverter(new HibernatePersistentMapConverter(stream.getMapper()));
        stream.registerConverter(new HibernatePersistentSortedMapConverter(stream.getMapper()));
        stream.registerConverter(new HibernatePersistentSortedSetConverter(stream.getMapper()));
    }

    // ===========================================================================
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SIC_INNER_SHOULD_BE_STATIC_ANON")
    public XStream getXStream()
    {
        return new XStream(new StaxDriver())
        {
            protected MapperWrapper wrapMapper(final MapperWrapper next)
            {
                return new HibernateMapper(next);
            }
        };
    }
}
