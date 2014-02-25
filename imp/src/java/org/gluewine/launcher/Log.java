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
