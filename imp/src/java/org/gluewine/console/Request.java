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
 * Defines a command request.
 *
 * @author Gluewine/Serge de Schaetzen
 *
 */
public class Request implements Serializable
{
    // ===========================================================================
    /** The serial uid. */
    private static final long serialVersionUID = 7053224823436937363L;

    /** The actual command to execute. */
    private String command = null;

    /** If true, it indicates that the output will be routed to a file. */
    private boolean outputRouted = false;

    /** If true, it indicates that the command has been issued through an exec command. */
    private boolean batch = false;

    /** If true, indicates that the command options were entered interactively. */
    private boolean interactive = false;

    // ===========================================================================
    /**
     * @return the command.
     */
    public String getCommand()
    {
        return command;
    }

    // ===========================================================================
    /**
     * @param command the command to set.
     */
    public void setCommand(String command)
    {
        this.command = command;
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
     * @param outputRouted the outputRouted to set.
     */
    public void setOutputRouted(boolean outputRouted)
    {
        this.outputRouted = outputRouted;
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
     * @param batch the batch to set.
     */
    public void setBatch(boolean batch)
    {
        this.batch = batch;
    }

    // ===========================================================================
    /**
     * @return the interactive.
     */
    public boolean isInteractive()
    {
        return interactive;
    }

    // ===========================================================================
    /**
     * @param interactive the interactive to set.
     */
    public void setInteractive(boolean interactive)
    {
        this.interactive = interactive;
    }
}
