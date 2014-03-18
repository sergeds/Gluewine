/**************************************************************************
 *
 * Gluewine Core Module
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
package org.gluewine.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

/**
 * Allows to log an error in structured way.
 *
 * @author fks/Serge de Schaetzen
 *
 */
@Deprecated
public final class ErrorLogger
{
    // ===========================================================================
    /**
     * Use the static methods to access the class.
     */
    private ErrorLogger()
    {
    }

    // ===========================================================================
    /**
     * Logs the given error on behalf of the class specified.
     *
     * @param cl The class where the exception occurred.
     * @param e The exception to log.
     */
    public static void log(Class<?> cl, Throwable e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        Logger.getLogger(cl).error(sw.toString());
    }
}
