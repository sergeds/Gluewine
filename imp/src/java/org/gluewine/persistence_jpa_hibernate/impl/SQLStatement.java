/**************************************************************************
 *
 * Gluewine Persistence Module
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
package org.gluewine.persistence_jpa_hibernate.impl;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Defines an SQL statement.
 *
 * @author fks/Serge de Schaetzen
 *
 */
@Entity
@Table(name = "hibernate_statements")
public class SQLStatement
{
    // ===========================================================================
    /**
     * The timestamp of execution of the statement.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "execution_time")
    private Date executionTime = null;

    /**
     * The id of the statement. This is a String translation
     * of an SHA1 computation of the statement itself.
     */
    @Id
    private String id = null;

    /**
     * The error message if unsuccessfull.
     */
    private String message = null;

    /**
     * The actual statement.
     */
    @Column(length = 4096)
    private String statement = null;

    /**
     * Flag indicating that the execution was successfull.
     */
    private boolean success = false;

    // ===========================================================================
    /**
     * Creates an instance.
     */
    public SQLStatement()
    {
    }

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param id The unique id.
     */
    public SQLStatement(String id)
    {
        this.id = id;
    }

    // ===========================================================================
    /**
     * Returns the timestamp of execution.
     *
     * @return The timestamp.
     */
    public Date getExecutionTime()
    {
        if (executionTime != null) return new Date(executionTime.getTime());
        else return null;
    }

    // ===========================================================================
    /**
     * Returns the id.
     *
     * @return The id.
     */
    public String getId()
    {
        return id;
    }

    // ===========================================================================
    /**
     * Returns the message.
     *
     * @return The message.
     */
    public String getMessage()
    {
        return message;
    }

    // ===========================================================================
    /**
     * Returns the statement to be executed.
     *
     * @return The statement to execute.
     */
    public String getStatement()
    {
        return statement;
    }

    // ===========================================================================
    /**
     * Returns true if successfull.
     *
     * @return True if successfull.
     */
    public boolean isSuccess()
    {
        return success;
    }

    // ===========================================================================
    /**
     * Sets the timestamp of execution.
     *
     * @param executionTime The timestamp.
     */
    public void setExecutionTime(Date executionTime)
    {
        this.executionTime = executionTime;
    }

    // ===========================================================================
    /**
     * Sets the message.
     *
     * @param message The message.
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    // ===========================================================================
    /**
     * Sets the statement to be executed.
     *
     * @param statement The statement to execute.
     */
    public void setStatement(String statement)
    {
        this.statement = statement;
    }

    // ===========================================================================
    /**
     * Sets the success flag.
     *
     * @param success The flag.
     */
    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    // ===========================================================================
    @Override
    public int hashCode()
    {
        StringBuilder b = new StringBuilder("SQLSTATEMENT:").append(getId());
        return b.toString().hashCode();
    }

    // ===========================================================================
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof SQLStatement)
            return getId().equals(((SQLStatement) o).getId());

        else
            return false;
    }
}
