/**************************************************************************
 *
 * Gluewine Console Module
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
package org.gluewine.console;

/**
 * Response used when the invocation of the command failed.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class FailedResponse extends Response
{
    // ===========================================================================
    /** The serial uid. */
    private static final long serialVersionUID = 5251709691994054395L;

    /** The cause of the failure. */
    private Throwable exception = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param exception The failure.
     * @param routed Whether the output is routed or not.
     * @param batch Whether the command was entered from batch mode.
     * @param interactive Whether the option values were entered interactively.
     */
    public FailedResponse(Throwable exception, boolean routed, boolean batch, boolean interactive)
    {
        super(exception.getMessage(), routed, batch, interactive);
        this.exception = exception;
    }

    // ===========================================================================
    /**
     * Returns the exception.
     *
     * @return The exception.
     */
    public Throwable getException()
    {
        return exception;
    }
}
