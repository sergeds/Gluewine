package org.gluewine.rest_server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.gluewine.authentication.AuthenticationException;
import org.gluewine.core.RepositoryListener;
import org.gluewine.core.utils.ErrorLogger;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.rest.REST;
import org.gluewine.rest.RESTID;
import org.gluewine.rest.RESTMethod;
import org.gluewine.rest.RESTSerializer;
import org.gluewine.sessions.SessionManager;
import org.gluewine.sessions.Unsecured;
import org.gluewine.utils.AnnotationUtility;

/**
 * Handles all REST requests, and dispatches them to the correct objects.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class RESTServlet extends GluewineServlet implements RepositoryListener<Object>
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -5416068629723455281L;

    /**
     * The map of all registered serializers.
     */
    private Map<String, RESTSerializer> serializers = new HashMap<String, RESTSerializer>();

    /**
     * Map of registerd, annotated, methods.
     */
    private Map<String, RESTMethod> methods = new HashMap<String, RESTMethod>();

    /**
     * The list of available authenticators.
     */
    private List<RESTAuthenticator> authenticators = new ArrayList<RESTAuthenticator>();

    /**
     * The session manager to use.
     */
    private SessionManager sessionManager;

    /**
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    @Override
    public String getContextPath()
    {
        return "REST";
    }

    // ===========================================================================
    /**
     * Returns the JSon context parsed from the request uri.
     *
     * @param req The request to process.
     * @return The json context.
     */
    private String getRESTPath(HttpServletRequest req)
    {
        String base = "/REST/";

        // Check the uri:
        String uri = req.getRequestURI();
        if (uri.length() > base.length())
        {
            String path = uri.substring(base.length());
            int i = path.indexOf('?');
            if (i > -1) path = path.substring(0, i);
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            return path;
        }
        else
            return "";
    }

    // ===========================================================================
    /**
     * Performs an authentication. If all available authenticators fail to authenticate
     * the request, a 401 (unauthorized) is send with a header WWW-Authenticate set to
     * Basic.
     *
     * @param req The current request.
     * @param resp The current response.
     * @throws AuthenticationException Throw if none of the available authenticators succeeded.
     */
    private void authenticate(HttpServletRequest req, HttpServletResponse resp) throws AuthenticationException
    {
        for (RESTAuthenticator auth : authenticators)
        {
            try
            {
                String session = auth.authenticate(req, resp);
                if (sessionManager != null) sessionManager.setCurrentSessionId(session);
                return;
            }
            catch (AuthenticationException e)
            {
                // Ignore, as we will check ALL available authenticators.
            }
        }
        if (authenticators.size() > 0) throw new AuthenticationException("Authentication Required");
    }

    // ===========================================================================
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        doGet(req, resp);
    }

    // ===========================================================================
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            String path = getRESTPath(req);
            if (methods.containsKey(path))
            {
                String format = req.getParameter("format");
                if (format == null) format = "json";
                RESTSerializer serializer = serializers.get(format);
                if (serializer != null)
                {
                    RESTMethod rm = methods.get(path);

                    if (AnnotationUtility.getAnnotation(Unsecured.class, rm.getMethod(), rm.getObject()) == null)
                        authenticate(req, resp);

                    Class<?>[] paramTypes = rm.getMethod().getParameterTypes();
                    Object[] params = new Object[paramTypes.length];
                    for (int i = 0; i < params.length; i++)
                    {
                        RESTID id = AnnotationUtility.getAnnotations(RESTID.class, rm.getObject(), rm.getMethod(), i);
                        if (id != null)
                        {
                            String[] val = req.getParameterValues(id.id());
                            if (logger.isTraceEnabled()) traceParameter(id.id(), val);
                            if (val != null && val.length > 0) params[i] = serializer.deserialize(paramTypes[i], val);
                            else params[i] = null;
                        }
                        else params[i] = null;
                    }

                    try
                    {
                        Object result = executeMethod(rm, params);
                        if (!rm.getMethod().getReturnType().equals(Void.TYPE))
                        {
                            String s = serializer.serialize(result);
                            if (logger.isTraceEnabled()) logger.trace("Serialized response: " + s);
                            resp.setContentType(serializer.getResponseMIME());
                            resp.setCharacterEncoding("utf8");
                            resp.getWriter().write(s);
                            resp.getWriter().flush();
                            resp.getWriter().close();
                        }
                    }
                    catch (IOException e)
                    {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Method execution failed: " + e.getMessage());
                    }
                }
                else resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unsupported format " + format);
            }
            else resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "There is no method registered with path " + path);
        }
        catch (AuthenticationException e)
        {
            resp.setHeader("WWW-Authenticate", "BASIC realm=\"SecureFiles\"");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please provide username and password");
        }
        finally
        {
            if (sessionManager != null) sessionManager.clearCurrentSessionId();
        }
    }

    // ===========================================================================
    /**
     * Traces the parameter given.
     *
     * @param name The name of the parameter.
     * @param val The value of the parameter.
     */
    private void traceParameter(String name, String[] val)
    {
        StringBuilder b = new StringBuilder();
        for (String v : val)
            b.append(v).append(" ");
        logger.trace("Parameter: " + name + " : " + b.toString());
    }

    // ===========================================================================
    /**
     * Executes the {@link RESTMethod} specified using the given parameters and returns the
     * result.
     *
     * @param method The method to execute.
     * @param params The parameters.
     * @return The result of the method invocation.
     * @throws IOException If an error occurs.
     */
    private Object executeMethod(RESTMethod method, Object[] params) throws IOException
    {
        try
        {
            if (logger.isDebugEnabled()) logger.debug("Executing method: " + method.getMethod());
            Object result = method.getMethod().invoke(method.getObject(), params);
            if (logger.isDebugEnabled()) logger.debug("Method returned: " + result);
            return result;
        }
        catch (InvocationTargetException e)
        {
            Throwable c = e.getCause();
            ErrorLogger.log(getClass(), c);
            throw new IOException(c.getMessage());
        }
        catch (Throwable e)
        {
            ErrorLogger.log(getClass(), e);
            if (e instanceof IOException) throw (IOException) e;
            else throw new IOException(e.getMessage());
        }
    }

    // ===========================================================================
    @Override
    public void registered(Object t)
    {
        for (Method m : t.getClass().getMethods())
        {
            REST r = AnnotationUtility.getAnnotation(REST.class, m, t);
            if (r != null)
            {
                if (logger.isDebugEnabled()) logger.debug("Registered REST Method " + fixPath(r));
                RESTMethod rm = new RESTMethod();
                rm.setMethod(m);
                rm.setObject(t);
                methods.put(fixPath(r), rm);
            }
        }

        if (t instanceof RESTSerializer)
        {
            RESTSerializer rs = (RESTSerializer) t;
            serializers.put(rs.getFormat(), rs);
        }

        if (t instanceof RESTAuthenticator)
            authenticators.add((RESTAuthenticator) t);

        if (t instanceof SessionManager)
            sessionManager = (SessionManager) t;
    }

    // ===========================================================================
    /**
     * Returns the path (fixed) from the annotation given.
     *
     * @param r The annotation to process.
     * @return The fixed path.
     */
    private String fixPath(REST r)
    {
        String path = r.path();
        if (path.startsWith("/")) path = path.substring(1);
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }

    // ===========================================================================
    @Override
    public void unregistered(Object t)
    {
        for (Method m : t.getClass().getMethods())
        {
            REST r = AnnotationUtility.getAnnotation(REST.class, m, t);
            if (r != null) methods.remove(fixPath(r));
        }

        if (t instanceof RESTSerializer)
        {
            RESTSerializer rs = (RESTSerializer) t;
            serializers.remove(rs.getFormat());
        }

        if (t instanceof RESTAuthenticator) authenticators.remove(t);

        if (t == sessionManager) sessionManager = null;
    }
}
