/**************************************************************************
 *
 * Gluewine GXO Hibernate Module
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
