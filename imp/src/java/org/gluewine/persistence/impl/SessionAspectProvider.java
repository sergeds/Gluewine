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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.log4j.Logger;
import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.AspectProvider;
import org.gluewine.core.Glue;
import org.gluewine.core.Repository;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.RunOnActivate;
import org.gluewine.launcher.CodeSource;
import org.gluewine.launcher.CodeSourceListener;
import org.gluewine.launcher.Launcher;
import org.gluewine.launcher.sources.JarCodeSource;
import org.gluewine.launcher.utils.FileUtils;
import org.gluewine.persistence.QueryPostProcessor;
import org.gluewine.persistence.QueryPreProcessor;
import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * Aspect Provider used for the Transactional chains.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SessionAspectProvider implements AspectProvider, CommandProvider, CodeSourceListener
{
    // ===========================================================================
    /**
     * Comparator that compare classes based on their name.
     */
    private static class ClassComparator implements Comparator<Class<?>>, Serializable
    {
        /**
         * The serial uid.
         */
        private static final long serialVersionUID = 3213199939953097064L;

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
    private Map<CodeSource, Map<String, List<SQLStatement>>> statements = new HashMap<CodeSource, Map<String, List<SQLStatement>>>();

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

    /**
     * The properties. They cannot be glued as they are required to
     * be present BEFORE any @RunOnActivate is invoked.
     */
    private Properties properties = null;

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
        properties = Launcher.getInstance().getProperties("hibernate.properties");
        codeSourceAdded(Launcher.getInstance().getSources());
    }

    // ===========================================================================================
    /**
     * Updates the sql statements with the statements specified in the given list.
     *
     * @param list The list to process.
     * @param stmts The list to update.
     * @throws NoSuchAlgorithmException If an error occurs computing the id.
     */
    private void updateSQLStatements(List<String> list, List<SQLStatement> stmts) throws NoSuchAlgorithmException
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
                        String id = FileUtils.getSHA1HashCode(st);
                        SQLStatement stmt = new SQLStatement(id);
                        stmt.setStatement(st);
                        stmts.add(stmt);
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
        session.doWork(new Work()
        {
            @Override
            @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
            public void execute(Connection conn) throws SQLException
            {
                Iterator<Map<String, List<SQLStatement>>> stiter = statements.values().iterator();
                while (stiter.hasNext()) // Bundle Level.
                {
                    Map<String, List<SQLStatement>> entry = stiter.next();
                    stiter.remove();
                    for (List<SQLStatement> stmts : entry.values()) // File level within a bundle, executing in the same transaction.
                    {
                        session.beginTransaction();
                        boolean commit = true;
                        for (SQLStatement st : stmts) // Statement level.
                        {
                            SQLStatement st2 = (SQLStatement) session.get(SQLStatement.class, st.getId());
                            if (st2 == null)
                            {
                                try
                                {
                                    logger.info("Executing SQL statement " + st.getStatement());
                                    conn.createStatement().execute(st.getStatement());
                                    st.setSuccess(true);
                                }
                                catch (Throwable e)
                                {
                                    st.setMessage(e.getMessage());
                                    st.setSuccess(false);
                                    logger.warn(e);
                                    commit = false;
                                }
                                try
                                {
                                    st.setExecutionTime(new Date());
                                    Session session2 = factory.openSession();
                                    session2.beginTransaction();
                                    session2.save(st);
                                    session2.getTransaction().commit();
                                    session2.close();
                                }
                                catch (Throwable e)
                                {
                                    commit = false;
                                }
                            }
                        }

                        if (commit) session.getTransaction().commit();
                        else session.getTransaction().rollback();
                    }
                }
            }
        });
        session.close();

    }

    // ===========================================================================
    /**
     * Initialization method.
     */
    @RunOnActivate
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
                stmt = stmt.replace('\n', ' ');
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
    public List<CLICommand> getCommands()
    {
        List<CLICommand> commands = new ArrayList<CLICommand>();
        commands.add(new CLICommand("pers_entities", "Lists all registered entities."));
        commands.add(new CLICommand("pers_statements", "Lists all executed statements."));
        return commands;
    }

    // ===========================================================================
    @Override
    public void codeSourceAdded(List<CodeSource> sources)
    {
        synchronized (factoryLocker)
        {
            try
            {
                Configuration config = new Configuration();
                config.setProperties(properties);
                ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();

                for (CodeSource source : sources)
                {
                    for (String entity : source.getEntities())
                    {
                        Class<?> clazz = source.getSourceClassLoader().loadClass(entity);
                        entities.add(clazz);
                    }

                    if (source instanceof JarCodeSource)
                    {
                        JarInputStream jar = null;
                        try
                        {
                            jar = new JarInputStream(((JarCodeSource) source).getURLs()[0].openStream());
                            JarEntry entry = jar.getNextJarEntry();
                            while (entry != null)
                            {
                                String name = entry.getName().toLowerCase(Locale.getDefault());
                                if (name.endsWith(".sql"))
                                {
                                    BufferedReader reader = null;
                                    List<String> content = new ArrayList<String>();
                                    reader = new BufferedReader(new InputStreamReader(jar, "UTF-8"));
                                    while (reader.ready())
                                        content.add(reader.readLine());

                                    List<SQLStatement> stmts = new ArrayList<SQLStatement>();

                                    if (statements.containsKey(source))
                                    {
                                        Map<String, List<SQLStatement>> m = statements.get(source);
                                        m.put(name, stmts);
                                    }
                                    else
                                    {
                                        Map<String, List<SQLStatement>> m = new TreeMap<String, List<SQLStatement>>();
                                        statements.put(source, m);
                                        m.put(name, stmts);
                                    }

                                    updateSQLStatements(content, stmts);
                                }
                                entry = jar.getNextJarEntry();
                            }
                        }
                        finally
                        {
                            if (jar != null) jar.close();
                        }
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
    public void codeSourceRemoved(List<CodeSource> sources)
    {
        synchronized (factoryLocker)
        {
            try
            {
                Configuration config = new Configuration();
                config.setProperties(properties);
                ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(config.getProperties()).buildServiceRegistry();

                for (CodeSource source : sources)
                {
                    Iterator<Class<?>> iter = entities.iterator();
                    while (iter.hasNext())
                    {
                        if (iter.next().getClassLoader() == source.getSourceClassLoader())
                            iter.remove();
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
