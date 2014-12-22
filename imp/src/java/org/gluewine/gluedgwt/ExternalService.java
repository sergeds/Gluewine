package org.gluewine.gluedgwt;

import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.impl.StandardSerializationPolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.gluewine.core.ContextInitializer;
import org.gluewine.core.Glue;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.RunOnActivate;
import org.gluewine.core.RemoteCallValidator;
import org.gluewine.core.Repository;
import org.gluewine.jetty.GluewineJettyLauncher;
import org.gluewine.jetty.GluewineServletProperties;
import org.gluewine.launcher.Launcher;
import org.gluewine.utils.AnnotationUtility;

/**
 * Baseclass for GWT services that run outside the gwt war.
 *
 * @author fks/Frank Gevaerts
 */
public abstract class ExternalService extends RemoteServiceServlet implements GluewineServletProperties
{
    /**
     * The logger instance.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * List of registered call validators.
     * */
    private Set<RemoteCallValidator> validators = new HashSet<RemoteCallValidator>();

    /**
     * The current registry.
     */
    @Glue
    private Repository registry = null;

    /**
     * The jetty launcher to register with.
     */
    @Glue
    private GluewineJettyLauncher launcher;

    /**
     * listener for RemoteCallValidators.
     */
    private class ValidatorListener implements RepositoryListener<RemoteCallValidator>
    {
        @Override
        public void registered(RemoteCallValidator validator)
        {
            validators.add(validator);
        }

        @Override
        public void unregistered(RemoteCallValidator validator)
        {
            validators.remove(validator);
        }
    }

    /** The listener. */
    private ValidatorListener listener = new ValidatorListener();

    /**
     * Register the servlet.
     */
    @RunOnActivate
    public void activate()
    {
        ServicePath sp = AnnotationUtility.getAnnotationRecursively(ServicePath.class, this);
        if (sp != null)
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put(RESOURCE_BASE, new File(Launcher.getInstance().getConfigDirectory(), "jetty/default").getAbsolutePath());
            launcher.register(sp.path(), this, params);
        }
        registry.addListener(listener);
    }

    @Override
    protected synchronized SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL, String strongName)
    {
        // The request can tell you the path of the web app relative to the
        // container root.
        SerializationPolicy serializationPolicy = null;
        // Strip off the context path from the module base URL. It should be a
        // strict prefix.
        String serializationPolicyFilePath = SerializationPolicyLoader.getSerializationPolicyFileName(strongName);
        // Open the RPC resource file read its contents.
        InputStream is = null;
        try
        {
            is = launcher.getWarContent(serializationPolicyFilePath);
            if (is == null)
            {
                is = new FileInputStream(new File(new File(Launcher.getInstance().getRoot(), "gwt-rpc"), serializationPolicyFilePath));
            }
            serializationPolicy = SerializationPolicyLoader.loadFromStream(is);
            return new GluewineSerializationPolicy((StandardSerializationPolicy) serializationPolicy);
        }
        catch (ParseException e)
        {
            getServletContext().log(
                    "ERROR: Failed to parse the policy file '"
                    + serializationPolicyFilePath + "'", e);
        }
        catch (ClassNotFoundException e)
        {
            getServletContext().log(
                    "ERROR: Could not find class '" + e.getMessage()
                    + "' listed in the serialization policy file '"
                    + serializationPolicyFilePath + "'"
                    + "; your server's classpath may be misconfigured", e);
        }
        catch (IOException e)
        {
            getServletContext().log(
                    "ERROR: Could not read the policy file '"
                    + serializationPolicyFilePath + "'", e);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    // Ignore this error
                }
            }
        }
        return null;
    }

    @ContextInitializer
    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, java.io.IOException
    {
        super.service(req, resp);
    }

    /**
     * Thrown when validation fails.
     */
    private static class RemoteCallNotAllowedException extends RuntimeException
    {
        /**
         * Builds a new RemoteCallNotAllowedException.
         * @param message the message.
         * @param cause the cause.
         */
        public RemoteCallNotAllowedException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

    @Override
    protected void onAfterRequestDeserialized(RPCRequest rpcRequest)
    {
        try
        {
            for (RemoteCallValidator validator: validators)
            {
                validator.validateCall("GWT", this, rpcRequest.getMethod(), rpcRequest.getParameters());
            }
        }
        catch (Exception e)
        {
            throw new RemoteCallNotAllowedException("Call to " + rpcRequest.getMethod() + " is not allowed", e);
        }
    }

    @Override
    protected void doUnexpectedFailure(Throwable e)
    {
        if (e instanceof RemoteCallNotAllowedException)
        {
            try
            {
                getThreadLocalResponse().reset();
                getThreadLocalResponse().sendError(403, e.getMessage());
            }
            catch (Exception ex)
            {
                throw new RuntimeException("Unable to report failure", ex);
            }
        }
        else
        {
            super.doUnexpectedFailure(e);
        }
    }
}
