/**************************************************************************
 *
 * Gluewine Launcher Module
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
package org.gluewine.launcher;

import java.util.Locale;

/**
 * Static logging that can be used by the classes of the launcher
 * package.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class Logging implements Log
{
    // ===========================================================================
    /**
     * The actual log to use.
     */
    private Log log = null;

    /**
     * The logging instance.
     */
    private static Logging instance = null;

    /** Error level. */
    private static final int LEVEL_ERROR = 4;
    /** Warn level. */
    private static final int LEVEL_WARN = 3;
    /** Info level. */
    private static final int LEVEL_INFO = 2;
    /** Debug level. */
    private static final int LEVEL_DEBUG = 1;
    /** Trace level. */
    private static final int LEVEL_TRACE = 0;

    /** The current loglevel. */
    private int level = LEVEL_INFO;

    // ===========================================================================
    /**
     * Use the static methods to access the class.
     */
    private Logging()
    {
        String l = System.getProperty("log.level", "info").toLowerCase(Locale.US);
        switch (l)
        {
            case "error" :
                level = LEVEL_ERROR;
                break;

            case "warn" :
                level = LEVEL_WARN;
                break;

            case "debug" :
                level = LEVEL_DEBUG;
                break;

            case "trace" :
                level = LEVEL_TRACE;
                break;

            default :
                level = LEVEL_INFO;
        }
    }

    // ===========================================================================
    /**
     * Returns the instance to be used.
     *
     * @return The instance to use.
     */
    public static synchronized Logging getInstance()
    {
        if (instance == null) instance = new Logging();
        return instance;
    }

    // ===========================================================================
    /**
     * Assigns an actual logger to be used.
     *
     * @param l The actual log.
     */
    public void setLog(Log l)
    {
        this.log = l;
    }

    // ===========================================================================
    @Override
    public void trace(Class<?> invoker, String ... message)
    {
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");

        if (log != null) log.trace(invoker, b.toString().trim());
        else if (level == LEVEL_TRACE) System.out.println(b.toString().trim());
    }

    // ===========================================================================
    @Override
    public void debug(Class<?> invoker, String ... message)
    {
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");

        if (log != null) log.debug(invoker, b.toString().trim());
        else if (level <= LEVEL_DEBUG) System.out.println(b.toString().trim());
    }

    // ===========================================================================
    @Override
    public void info(Class<?> invoker, String ... message)
    {
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");

        if (log != null) log.info(invoker, b.toString().trim());
        else if (level <= LEVEL_INFO) System.out.println(b.toString().trim());
    }

    // ===========================================================================
    @Override
    public void warn(Class<?> invoker, String ... message)
    {
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");

        if (log != null) log.warn(invoker, b.toString().trim());
        else if (level <= LEVEL_WARN) System.out.println(b.toString().trim());
    }

    // ===========================================================================
    @Override
    public void error(Class<?> invoker, String ... message)
    {
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");

        if (log != null) log.error(invoker, b.toString().trim());
        else  if (level <= LEVEL_ERROR) System.out.println(b.toString().trim());
    }
}
