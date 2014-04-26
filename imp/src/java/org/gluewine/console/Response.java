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

import java.io.Serializable;

/**
 * Defines the response received from the server when invoking a CLI Command.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class Response implements Serializable
{
    // ===========================================================================
    /** The serial uid. */
    private static final long serialVersionUID = -1294916548195304237L;

    /** The output of the command. */
    private String output = null;

    /** If true, it indicates that the output will be routed to a file. */
    private boolean outputRouted = false;

    /** If true, it indicates that the command has been issued through an exec command. */
    private boolean batch = false;

    /** If true, indicates that the command options were entered interactively. */
    private boolean interactive = false;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param output The output of the command.
     * @param routed Whether the output is routed or not.
     * @param batch Whether the command was entered from batch mode.
     * @param interactive Whether the option values were entered interactively.
     */
    public Response(String output, boolean routed, boolean batch, boolean interactive)
    {
        this.output = output;
        this.outputRouted = routed;
        this.batch = batch;
        this.interactive = interactive;
    }

    // ===========================================================================
    /**
     * Returns the output of the command.
     *
     * @return The output.
     */
    public String getOuput()
    {
        return output;
    }

    // ===========================================================================
    /**
     * @return the outputRouted.
     */
    public boolean isOutputRouted()
    {
        return outputRouted;
    }

    // ===========================================================================
    /**
     * @return the batch.
     */
    public boolean isBatch()
    {
        return batch;
    }

    // ===========================================================================
    /**
     * @return the interactive.
     */
    public boolean isInteractive()
    {
        return interactive;
    }
}
