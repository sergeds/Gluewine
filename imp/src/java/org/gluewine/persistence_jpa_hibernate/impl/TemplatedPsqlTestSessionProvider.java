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

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;

/**
 * Provider that can be used for testing postgresql-using code, using template
 * databases for performance.
 *
 * The following properties are expected in the hibernate properties file:
 *
 * hibernate.connection.template = The logical name for the connection
 *
 * The following properties are expected as system properties:
 * templatedb.&lt;logicalname&gt; = the name of the created template database
 * templatedb.baseurl = The base of the jdbc url. The database names will be appended to this
 * templatedb.superuser = username allowed to create the new database
 * templatedb.superpass = superuser password
 *
 * @author fks/Frank Gevaerts
 *
 */
public class TemplatedPsqlTestSessionProvider extends TestSessionProvider
{
    /**
     * The database name.
     */
    private String dbname = null;

    // ===========================================================================
    /**
     * Creates an instance. The config file given must point to the file containing
     * the hibernate test configuration.
     *
     * All classes that will need to be used by Hibernate during the test should be
     * specified.
     *
     * @param config The config to use.
     * @param classes The annotated classes to include in the Hibernate config.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    public TemplatedPsqlTestSessionProvider(File config, Class<?> ... classes)
    {
        if (open)
            closeProvider();

        if (config.exists())
        {
            try
            (
                FileInputStream fis = new FileInputStream(config);
            )
            {
                Properties props = new Properties();
                props.load(fis);
                String basename = props.getProperty("hibernate.connection.template");
                String template = System.getProperty("templatedb." + basename);
                String baseurl = System.getProperty("templatedb.baseurl", "jdbc:postgresql://localhost:5432/");
                dbname = UUID.randomUUID().toString();

                Class.forName("org.postgresql.Driver");
                try
                (
                    Connection con = DriverManager.getConnection(baseurl + "postgres", System.getProperty("templatedb.superuser"), System.getProperty("templatedb.superpass"));
                    Statement stmt = con.createStatement();
                )
                {
                    stmt.execute("CREATE DATABASE \"" + dbname + "\" OWNER " + props.getProperty("hibernate.connection.username") + " TEMPLATE \"" + template + "\"");
                }

                props.setProperty("hibernate.connection.url", baseurl + dbname);

                configure(props, classes);

                setOpen(true);

                Runtime.getRuntime().addShutdownHook(new Thread()
                        {
                            public void run()
                            {
                                closeProvider();
                            }
                        });
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }
        else
            throw new RuntimeException("The config " + config.getAbsolutePath() + " does not exist.");
    }

    // ===========================================================================
    /**
     * Closes the provider.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    public void closeProvider()
    {
        super.closeProvider();

        try
        {
            if (dbname != null)
            {
                String baseurl = System.getProperty("templatedb.baseurl", "jdbc:postgresql://localhost:5432/");
                Class.forName("org.postgresql.Driver");
                try
                (
                    Connection con = DriverManager.getConnection(baseurl + "postgres", System.getProperty("templatedb.superuser"), System.getProperty("templatedb.superpass"));
                    Statement stmt = con.createStatement();
                )
                {
                    stmt.execute("DROP DATABASE \"" + dbname + "\"");
                }
                dbname = null;
            }
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }

        setOpen(false);
    }
}
