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
 * A reponse that failed due to a syntax exception.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SyntaxResponse extends Response
{
    // ===========================================================================
    /** The serial uid. */
    private static final long serialVersionUID = -7101539852928081970L;

    /** The command that failed. */
    private CLICommand cmd = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param output The output of the command.
     * @param cmd The command that failed.
     * @param routed Whether the output is routed or not.
     * @param batch Whether the command was entered from batch mode.
     * @param interactive Whether the option values were entered interactively.
     */
    public SyntaxResponse(String output, CLICommand cmd, boolean routed, boolean batch, boolean interactive)
    {
        super(output, routed, batch, interactive);
        this.cmd = cmd;
    }

    // ===========================================================================
    /**
     * Returns the command that failed.
     *
     * @return The command that failed.
     */
    public CLICommand getCommand()
    {
        return cmd;
    }
}
