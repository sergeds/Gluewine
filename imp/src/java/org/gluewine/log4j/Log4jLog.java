/**************************************************************************
 *
 * Gluewine Log4j Module
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
package org.gluewine.log4j;

import org.apache.log4j.Logger;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RunOnDeactivate;
import org.gluewine.launcher.Log;
import org.gluewine.launcher.Logging;

/**
 * Forwards the logging calls to log4j.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class Log4jLog implements Log
{
    // ===========================================================================
    /**
     * Invoked when the module is activated.
     */
    @RunOnActivate
    public void activate()
    {
        Logging.getInstance().setLog(this);
    }

    // ===========================================================================
    /**
     * Invoked when the module is deactivated.
     */
    @RunOnDeactivate
    public void deactivate()
    {
        Logging.getInstance().setLog(null);
    }

    // ===========================================================================
    @Override
    public void trace(Class<?> invoker, String... message)
    {
        Logger log = Logger.getLogger(invoker);
        if (log.isTraceEnabled())
        {
            StringBuilder b = new StringBuilder();
            for (String m : message)
                b.append(m).append(" ");
            log.trace(b.toString().trim());
        }
    }

    // ===========================================================================
    @Override
    public void debug(Class<?> invoker, String... message)
    {
        Logger log = Logger.getLogger(invoker);
        if (log.isDebugEnabled())
        {
            StringBuilder b = new StringBuilder();
            for (String m : message)
                b.append(m).append(" ");
            log.debug(b.toString().trim());
        }
    }

    // ===========================================================================
    @Override
    public void info(Class<?> invoker, String... message)
    {
        Logger log = Logger.getLogger(invoker);
        if (log.isInfoEnabled())
        {
            StringBuilder b = new StringBuilder();
            for (String m : message)
                b.append(m).append(" ");
            log.info(b.toString().trim());
        }
    }

    // ===========================================================================
    @Override
    public void warn(Class<?> invoker, String... message)
    {
        Logger log = Logger.getLogger(invoker);
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");
        log.warn(b.toString().trim());
    }

    // ===========================================================================
    @Override
    public void error(Class<?> invoker, String... message)
    {
        Logger log = Logger.getLogger(invoker);
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");
        log.error(b.toString().trim());
    }
}
