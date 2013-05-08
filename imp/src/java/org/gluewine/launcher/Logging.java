package org.gluewine.launcher;

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

    // ===========================================================================
    /**
     * Use the static methods to access the class.
     */
    private Logging()
    {
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
        else System.out.println(b.toString().trim());
    }

    // ===========================================================================
    @Override
    public void debug(Class<?> invoker, String ... message)
    {
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");

        if (log != null) log.debug(invoker, b.toString().trim());
        else System.out.println(b.toString().trim());
    }

    // ===========================================================================
    @Override
    public void info(Class<?> invoker, String ... message)
    {
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");

        if (log != null) log.info(invoker, b.toString().trim());
        else System.out.println(b.toString().trim());
    }

    // ===========================================================================
    @Override
    public void warn(Class<?> invoker, String ... message)
    {
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");

        if (log != null) log.warn(invoker, b.toString().trim());
        else System.out.println(b.toString().trim());
    }

    // ===========================================================================
    @Override
    public void error(Class<?> invoker, String ... message)
    {
        StringBuilder b = new StringBuilder();
        for (String m : message)
            b.append(m).append(" ");

        if (log != null) log.error(invoker, b.toString().trim());
        else System.out.println(b.toString().trim());
    }
}
