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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Launcher that is initialized through a servlet.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class ServletLauncher implements ServletContextListener, Runnable
{
    // ===========================================================================
    /**
     * The reader from the stdout of the process.
     */
    private BufferedReader stdin = null;

    /**
     * The reader from the stderr of the process.
     */
    private BufferedReader stderr = null;

    /**
     * The writer to the stdin of the process.
     */
    private BufferedWriter stdout = null;

    /**
     * The current process.
     */
    private Process process = null;

    /**
     * Flag indicating that the listener should stop.
     */
    private boolean stopRequested = false;

    // ===========================================================================
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        String libdir = sce.getServletContext().getInitParameter("gluewine.libdir");
        String cfgdir = sce.getServletContext().getInitParameter("gluewine.cfgdir");

        if (libdir == null) libdir = sce.getServletContext().getContextPath() + "/lib";
        if (cfgdir == null) libdir = sce.getServletContext().getContextPath() + "/cfg";

        launchProcess(libdir, cfgdir);
    }

    // ===========================================================================
    /**
     * Launches the gluewine framework as a separate process.
     *
     * @param libdir The lib directory to use.
     * @param cfgdir The cfg directory to use.
     */
    private void launchProcess(String libdir, String cfgdir)
    {
        System.out.println("Initializing Gluewine framework");
        File f = new File(libdir, "org.gluewine.launcher-0.1.jar");
        String[] cmd = new String[] {"java", "-jar", f.getAbsolutePath(), "org.gluewine.core.glue.Gluer", "gwt"};
        String[] env = new String[] {"gluewine.libdir=" + libdir, "gluewine.cfgdir=" + cfgdir};
        try
        {
            process = Runtime.getRuntime().exec(cmd, env);
            stdin = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            stderr = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
            stdout = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
            Thread th = new Thread(this);
            th.setDaemon(false);
            th.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // ===========================================================================
    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        stopRequested = true;
        try
        {
            stdout.write("shutdown");
            stdout.newLine();
            stdout.flush();
            stdin.close();
            stdout.close();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    // ===========================================================================
    @Override
    public void run()
    {
        while (!stopRequested)
        {
            try
            {
                while (stdin.ready())
                    System.out.println(stdin.readLine());

                while (stderr.ready())
                    System.out.println(stderr.readLine());
            }
            catch (IOException e)
            {
                if (!stopRequested) e.printStackTrace();
                else break;
            }
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
        }
    }
}
