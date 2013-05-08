package org.gluewine.launcher;

/**
 * Defines a log interface.
 * This is to avoid a hard link between the launcher and a
 * Logging framework like Log4J.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface Log
{
    // ===========================================================================
    /**
     * Logs a trace message.
     *
     * @param invoker The invoker.
     * @param message The message to log.
     */
    void trace(Class<?> invoker, String ... message);

    // ===========================================================================
    /**
     * Logs a debug message.
     *
     * @param invoker The invoker.
     * @param message The message to log.
     */
    void debug(Class<?> invoker, String ... message);

    // ===========================================================================
    /**
     * Logs a informational message.
     *
     * @param invoker The invoker.
     * @param message The message to log.
     */
    void info(Class<?> invoker, String ... message);

    // ===========================================================================
    /**
     * Logs a warning message.
     *
     * @param invoker The invoker.
     * @param message The message to log.
     */
    void warn(Class<?> invoker, String ... message);

    // ===========================================================================
    /**
     * Logs an error message.
     *
     * @param invoker The invoker.
     * @param message The message to log.
     */
    void error(Class<?> invoker, String ... message);
}
