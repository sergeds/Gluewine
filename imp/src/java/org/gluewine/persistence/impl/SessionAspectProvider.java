/**************************************************************************
 *
 * Gluewine Persistence Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version
 * 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 **************************************************************************/
package org.gluewine.persistence.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.AspectProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.JarListener;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.RunWhenGlued;
import org.gluewine.core.utils.SHA1Utils;
import org.gluewine.launcher.Launcher;
import org.gluewine.persistence.QueryPostProcessor;
import org.gluewine.persistence.QueryPreProcessor;
import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * Aspect Provider used for the Transactional chains.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SessionAspectProvider implements AspectProvider, CommandProvider, JarListener
{
    // ===========================================================================
    /**
     * Comparator that compare classes based on their name.
     */
    private static class ClassComparator implements Comparator<Class<?>>
    {
        @Override
        public int compare(Class<?> o1, Class<?> o2)
        {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /**
     * The session provider to use.
     */
    @Glue
    private SessionProviderImpl provider = null;

    /**
     * The actual Hibernate session factory.
     */
    private SessionFactory factory = null;

    /**
     * The current registry.
     */
    @Glue
    private Repository registry = null;

    /**
     * The set of registered preprocessors.
     */
    private Set<QueryPreProcessor> preProcessors = new HashSet<QueryPreProcessor>();

    /**
     * The set of registered preprocessors.
     */
    private Set<QueryPostProcessor> postProcessors = new HashSet<QueryPostProcessor>();

    /**
     * The logger to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The list of statements.
     */
    private List<SQLStatement> statements = new ArrayList<SQLStatement>();

    /**
     * Name of the property file for Hibernate.
     */
    private static final String HIBERNATE_FILE = "hibernate.properties";

    /**
     * The set of registered entities.
     */
    private Set<Class<?>> entities = new TreeSet<Class<?>>(new ClassComparator());

    /**
     * The date formatter for outputting dates.
     */
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * A lock to synchronize the access to the session factory.
     */
    private Object factoryLocker = new Object();

    // ===========================================================================
    /**
     * Creates an instance. This will initialize the Hibernate
     * persistency library.
     * During initialization, all JAR files will be parsed, and all classes
     * defined in the sds-entity Manifest entry will be added to the hibernate
     * configurator.
     *
     * @throws IOException If the property file fails to be read, or one of the jar files
     * cannot be accessed.
     * @throws ClassNotFoundException If one of the entities could not be loaded.
     * @throws NoSuchAlgorithmException If the SHA1 algorithm is not implemented.
     */
    public SessionAspectProvider() throws IOException, ClassNotFoundException, NoSuchAlgorithmException
    {
        jarsAdded(Launcher.getInstance().getJarFiles());
    }

    // ===========================================================================================
    /**
     * Updates the sql statements with the statements specified in the given list.
     *
     * @param list The list to process.
     * @throws NoSuchAlgorithmException If an error occurs computing the id.
     */
    private void updateSQLStatements(List<String> list) throws NoSuchAlgorithmException
    {
        synchronized (this)
        {
            StringBuilder b = new StringBuilder();
            for (String s : list)
            {
                s = s.trim();
                if (s.length() > 0 && !s.startsWith("--"))
                {
                    b.append(s);
                    if (b.toString().endsWith(";"))
                    {
                        String st = b.toString();
                        b.delete(0, b.length());
                        String id = SHA1Utils.getSHA1HashCode(st);
                        SQLStatement stmt = new SQLStatement(id);
                        stmt.setStatement(st);
                        statements .add(stmt);
                    }
                    else
                        b.append("\n");
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Checks whether the statements have already been executed, and if not are executed.
     */

    private void checkStatements()
    {
        final Session session = factory.openSession();
        Iterator<SQLStatement> stiter = statements.iterator();
        while (stiter.hasNext())
        {
            final SQLStatement st = stiter.next();
            stiter.remove();
            SQLStatement st2 = (SQLStatement) session.get(SQLStatement.class, st.getId());
            if (st2 == null)
            {
                session.doWork(new org.hibernate.jdbc.Work()
                {
                    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
                    @Override
                    public void execute(Connection conn) throws SQLException
                    {
                        session.beginTransaction();
                        Statement sqlst = null;
                        try
                        {
                            logger.info("Executing SQL statement " + st.getStatement());
                            sqlst = conn.createStatement();
                            sqlst.execute(st.getStatement());
                            st.setSuccess(true);
                            session.getTransaction().commit();
                        }
                        catch (Throwable e)
                        {
                            st.setMessage(e.getMessage());
                            st.setSuccess(false);
                            logger.warn(e);
                            session.getTransaction().rollback();
                        }
                        finally
                        {
                            if (sqlst != null) sqlst.close();
                        }
                        st.setExecutionTime(new Date());
                    }
                });

                session.beginTransaction();
                session.save(st);
                session.getTransaction().commit();
            }
        }
        session.close();
    }

    // ===========================================================================
    /**
     * Initialization method.
     */
    @RunWhenGlued
    public void initialize()
    {
        registry.addListener(new RepositoryListener<QueryPreProcessor>()
        {
            @Override
            public void registered(QueryPreProcessor t)
            {
                preProcessors.add(t);
            }

            @Override
            public void unregistered(QueryPreProcessor t)
            {
                preProcessors.remove(t);
            }
        });

        registry.addListener(new RepositoryListener<QueryPostProcessor>()
        {

            @Override
            public void registered(QueryPostProcessor t)
            {
                postProcessors.add(t);
            }

            @Override
            public void unregistered(QueryPostProcessor t)
            {
                postProcessors.remove(t);
            }
        });
    }

    // ===========================================================================
    @Override
    public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
    {
        if (m.isAnnotationPresent(Transactional.class))
            before(true);
    }

    // ===========================================================================
    /**
     * Invoked to initialize a session and increases the counter if the flag is set
     * to true.
     *
     * @param increase True to increase the reference count.
     */
    void before(boolean increase)
    {
        HibernateTransactionalSessionImpl session = provider.getBoundSession();
        if (session == null)
        {
            synchronized (factoryLocker)
            {
                Session hibernateSession = factory.openSession();
                hibernateSession.beginTransaction();
                session = new HibernateTransactionalSessionImpl(hibernateSession, preProcessors, postProcessors);
                provider.bindSession(session);
            }
        }

        if (increase) session.increaseReferenceCount();
    }

    // ===========================================================================
    @Override
    public void afterSuccess(Object o, Method m, Object[] params, Object result)
    {
        if (m.isAnnotationPresent(Transactional.class))
            afterSuccess();
    }

    // ===========================================================================
    /**
     * Invoked when a method has successfully been executed. It will commit
     * the connection if this was the last entry in the stack.
     */
    void afterSuccess()
    {
        HibernateTransactionalSessionImpl session = provider.getBoundSession();
        session.decreaseReferenceCount();
        if (session.getReferenceCount() == 0)
        {
            provider.unbindSession();
            try
            {
                session.getHibernateSession().getTransaction().commit();
                notifyCommitted(session.getRegisteredCallbacks());
                session.getHibernateSession().close();
            }
            catch (Throwable e)
            {
                logger.error("An error occurred during a transaction rollback, " + e.getMessage());
                session.getHibernateSession().getTransaction().rollback();
                notifyRolledback(session.getRegisteredCallbacks());
            }
        }
    }

    // ===========================================================================
    @Override
    public void afterFailure(Object o, Method m, Object[] params, Throwable e)
    {
        if (m.isAnnotationPresent(Transactional.class))
            afterFailure();
    }

    // ===========================================================================
    /**
     * Returns the set of registered PreProcessors.
     *
     * @return The set of PreProcessors.
     */
    Set<QueryPreProcessor> getPreProcessors()
    {
        return preProcessors;
    }

    // ===========================================================================
    /**
     * Returns the set of registered PostProcessors.
     *
     * @return The set of PostProcessors.
     */
    Set<QueryPostProcessor> getPostProcessors()
    {
        return postProcessors;
    }

    // ===========================================================================
    /**
     * Invoked when a method has failed. It will rollback the connection if this
     * was the last entry in the stack.
     */
    void afterFailure()
    {
        HibernateTransactionalSessionImpl session = provider.getBoundSession();
        session.decreaseReferenceCount();
        if (session.getReferenceCount() == 0)
        {
            provider.unbindSession();
            try
            {
                session.getHibernateSession().getTransaction().commit();
                notifyRolledback(session.getRegisteredCallbacks());
                session.getHibernateSession().close();
            }
            catch (Throwable t)
            {
                logger.error("An error occurred during a rolling back a transaction, " + t.getMessage());
            }
        }
    }

    // ===========================================================================
    @Override
    public void after(Object o, Method m, Object[] params)
    {
        // Not used.
    }

    // ===========================================================================
    /**
     * Notifies all registered callbacks that a transaction has been committed.
     *
     * @param callbacks The callbacks to notify.
     */
    private void notifyCommitted(Stack<TransactionCallback> callbacks)
    {
        while (!callbacks.isEmpty())
        {
            try
            {
                TransactionCallback cb = callbacks.pop();
                cb.transactionCommitted();
            }
            catch (Throwable e)
            {
                logger.error("An error occurred during notification of commit: " + e.getMessage());
            }
        }
    }

    // ===========================================================================
    /**
     * Notifies all registered callbacks that a transaction has been rolled back.
     *
     * @param callbacks The callbacks to notify.
     */
    private void notifyRolledback(Stack<TransactionCallback> callbacks)
    {
        while (!callbacks.isEmpty())
        {
            try
            {
                TransactionCallback cb = callbacks.pop();
                cb.transactionRolledBack();
            }
            catch (Throwable e)
            {
                logger.error("An error occurred during notification of rollback: " + e.getMessage());
            }
        }
    }

    // ===========================================================================
    /**
     * Executes the pers_entities command.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    public void _pers_entities(CommandContext ci) throws Throwable
    {
        ci.tableHeader("Hibernate Entities");
        for (Class<?> s : entities)
            ci.tableRow(s.getName());

        ci.printTable();
    }

    // ===========================================================================
    /**
     * Executes the pers_statements command.
     *
     * @param ci The current context.
     * @throws Throwable If a problem occurs.
     */
    @SuppressWarnings("unchecked")
    public void _pers_statements(CommandContext ci) throws Throwable
    {
        Session hibernateSession = factory.openSession();
        hibernateSession.beginTransaction();

        try
        {
            ci.tableHeader("Id", "Date", "Successful", "Statement", "Message");
            ci.tableMaxColumnWidth(0, 0, 0, 40, 0);
            List<SQLStatement> l = hibernateSession.createCriteria(SQLStatement.class).list();
            for (SQLStatement s : l)
            {
                String stmt = s.getStatement().replace('\r', ' ');
                stmt = s.getStatement().replace('\n', ' ');
                ci.tableRow(s.getId(), format.format(s.getExecutionTime()), Boolean.toString(s.isSuccess()), stmt, s.getMessage());
            }

            ci.printTable();

            hibernateSession.getTransaction().commit();
        }
        catch (Throwable e)
        {
            hibernateSession.getTransaction().rollback();
            throw e;
        }
    }

    // ===========================================================================
    @Override
    public Map<String, String> getCommandsSyntax()
    {
        Map<String, String> m = new HashMap<String, String>();
        m.put("pers_entities", "Lists all registered entities");
        m.put("pers_statements", "Lists all executed statements");
        return m;
    }

    // ===========================================================================
    @Override
    public void jarsAdded(List<File> files)
    {
        synchronized (factoryLocker)
        {
            try
            {
                Configuration config = new Configuration();
                config.setProperties(Launcher.getInstance().getProperties(HIBERNATE_FILE));
                ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();

                for (File file : files)
                {
                    JarFile jar = null;
                    try
                    {
                        jar = new JarFile(file);
                        Manifest manifest = jar.getManifest();
                        if (manifest != null)
                        {
                            Attributes attr = manifest.getMainAttributes();
                            String ent = attr.getValue("Gluewine-Entities");
                            if (ent != null)
                            {
                                ent = ent.trim();
                                String[] cl = ent.split(",");
                                for (String c : cl)
                                {
                                    c = c.trim();
                                    Class<?> clazz = getClass().getClassLoader().loadClass(c);
                                    entities.add(clazz);
                                }
                            }
                        }

                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements())
                        {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName().toLowerCase(Locale.getDefault());
                            if (name.endsWith(".sql"))
                            {
                                BufferedReader reader = null;
                                try
                                {
                                    List<String> content = new ArrayList<String>();
                                    reader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)));
                                    while (reader.ready())
                                        content.add(reader.readLine());

                                    updateSQLStatements(content);
                                }
                                finally
                                {
                                    if (reader != null) reader.close();
                                }
                            }
                        }
                    }
                    finally
                    {
                        if (jar != null) jar.close();
                    }
                }

                for (Class<?> cl : entities)
                    config.addAnnotatedClass(cl);

                factory = config.buildSessionFactory(serviceRegistry);
                checkStatements();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
    }

    // ===========================================================================
    @Override
    public void jarsRemoved(List<File> files)
    {
        synchronized (factoryLocker)
        {
            try
            {
                Configuration config = new Configuration();
                config.setProperties(Launcher.getInstance().getProperties(HIBERNATE_FILE));
                ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();

                for (File file : files)
                {
                    JarFile jar = null;
                    try
                    {
                        jar = new JarFile(file);
                        Manifest manifest = jar.getManifest();
                        if (manifest != null)
                        {
                            Attributes attr = manifest.getMainAttributes();
                            String ent = attr.getValue("Gluewine-Entities");
                            if (ent != null)
                            {
                                ent = ent.trim();
                                String[] cl = ent.split(",");
                                for (String c : cl)
                                {
                                    c = c.trim();
                                    Class<?> clazz = getClass().getClassLoader().loadClass(c);
                                    entities.remove(clazz);
                                }
                            }
                        }
                    }
                    finally
                    {
                        if (jar != null) jar.close();
                    }
                }

                for (Class<?> cl : entities)
                    config.addAnnotatedClass(cl);

                factory = config.buildSessionFactory(serviceRegistry);
                checkStatements();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
    }
}
