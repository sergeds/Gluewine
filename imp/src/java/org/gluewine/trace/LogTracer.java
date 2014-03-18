/**************************************************************************
 *
 * Gluewine Trace Module
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
package org.gluewine.trace;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Allows to trace services, and outputs the trace information in standard log4j.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class LogTracer extends AbstractTracer
{
    // ===========================================================================
    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * Map of indents per thread.
     */
    private Map<Thread, Integer> indents = new HashMap<Thread, Integer>();

    // ===========================================================================
    @Override
    public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
    {
        if (isSuppressed()) return;
        int indent = 0;
        if (indents.containsKey(Thread.currentThread())) indent = indents.get(Thread.currentThread()).intValue();

        StringBuilder b = new StringBuilder();

        for (int i = 0; i < indent; i++)
            b.append("  ");

        indent++;
        indents.put(Thread.currentThread(), Integer.valueOf(indent));

        b.append("(").append(Thread.currentThread().getId()).append(") ===> ");
        b.append(getClassName(o.getClass())).append(".");
        b.append(m.getName());
        b.append("(");

        for (int i = 0; i < params.length; i++)
        {
            if (params[i] != null) b.append(params[i].toString());
            else b.append("null");

            if (i < params.length - 1) b.append(",");
        }

        b.append(")");
        logger.info(b.toString());
        clearSuppression();
    }

    // ===========================================================================
    @Override
    public void afterSuccess(Object o, Method m, Object[] params, Object result)
    {
        if (isSuppressed()) return;

        int indent = 0;
        if (indents.containsKey(Thread.currentThread())) indent = indents.get(Thread.currentThread()).intValue();

        StringBuilder b = new StringBuilder();
        indent--;

        for (int i = 0; i < indent; i++)
            b.append("  ");

        indents.put(Thread.currentThread(), Integer.valueOf(indent));

        b.append("(").append(Thread.currentThread().getId()).append(") <=== ");
        if (result != null) b.append("(").append(result.toString()).append(")");
        else if (m.getReturnType().equals(Void.TYPE)) b.append("(void)");
        else b.append("(null)");

        b.append(" ");
        b.append(getClassName(o.getClass())).append(".");
        b.append(m.getName());
        b.append("(...)");
        logger.info(b.toString());
        clearSuppression();
    }

    // ===========================================================================
    @Override
    public void afterFailure(Object o, Method m, Object[] params, Throwable e)
    {
        if (isSuppressed()) return;
        int indent = 0;
        if (indents.containsKey(Thread.currentThread())) indent = indents.get(Thread.currentThread()).intValue();

        StringBuilder b = new StringBuilder();
        indent--;

        for (int i = 0; i < indent; i++)
            b.append("  ");

        indents.put(Thread.currentThread(), Integer.valueOf(indent));

        b.append("(").append(Thread.currentThread().getId()).append(") <=== ");

        b.append(e.getClass().getName()).append(" ");

        b.append(" ");
        b.append(getClassName(o.getClass())).append(".");
        b.append(m.getName());
        b.append("(...)");
        logger.info(b.toString());
        clearSuppression();
    }

    // ===========================================================================
    @Override
    public void after(Object o, Method m, Object[] params)
    {
    }

    // ===========================================================================
    @Override
    public void close()
    {
        indents.clear();
    }
}
