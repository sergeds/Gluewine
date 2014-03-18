package org.gluewine.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

/**
 * Allows to log an error in structured way.
 *
 * @author fks/Serge de Schaetzen
 *
 */
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
