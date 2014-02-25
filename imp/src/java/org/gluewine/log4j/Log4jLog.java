/**************************************************************************
 *
 * Gluewine Log4j Module
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
