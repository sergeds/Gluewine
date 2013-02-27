/**************************************************************************
 *
 * Gluewine GXO Protocol Module
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
package org.gluewine.gxo;

import java.io.Serializable;

/**
 * Base class for GXO communication.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public abstract class GxoBean implements Serializable
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 494258388345503404L;

    /**
     * The id of the thread this bean was initiated in.
     */
    private String threadId = null;

    // ===========================================================================
    /**
     * Creates an instance.
     */
    protected GxoBean()
    {
    }

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param threadId The id of the thread that created the bean.
     */
    protected GxoBean(String threadId)
    {
        this.threadId = threadId;
    }

    // ===========================================================================
    /**
     * Returns the thread id.
     *
     * @return The thread id.
     */
    public String getThreadId()
    {
        return threadId;
    }
}

