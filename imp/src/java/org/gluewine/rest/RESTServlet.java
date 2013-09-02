package org.gluewine.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;
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
import org.gluewine.sessions.SessionManager;

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
     * The logger instance to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The list of available authenticators.
     */
    private List<RESTAuthenticator> authenticators = new ArrayList<RESTAuthenticator>();

    /**
     * The session manager to use.
     */
    private SessionManager sessionManager;

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
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            authenticate(req, resp);
            String path = getRESTPath(req);
            if (methods.containsKey(path))
            {
                String format = req.getParameter("format");
                if (format == null) format = "json";
                RESTSerializer serializer = serializers.get(format);
                if (serializer != null)
                {
                    RESTMethod rm = methods.get(path);
                    Class<?>[] paramTypes = rm.getMethod().getParameterTypes();
                    Annotation[][] annots = rm.getMethod().getParameterAnnotations();
                    Object[] params = new Object[paramTypes.length];
                    int i = 0;
                    for (Annotation[] ann : annots)
                    {
                        String id = getParameterId(ann);
                        if (id != null)
                        {
                            String[] val = req.getParameterValues(id);
                            if (val != null && val.length > 0) params[i] = serializer.deserialize(paramTypes[i], val);
                            else params[i] = null;
                        }
                        else params[i] = null;
                        i++;
                    }

                    try
                    {
                        Object result = executeMethod(rm, params);
                        if (!rm.getMethod().getReturnType().equals(Void.TYPE))
                        {
                            String s = serializer.serialize(result);
                            resp.setContentType(serializer.getResponseMIME());
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
            return method.getMethod().invoke(method.getObject(), params);
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
    /**
     * Returns the parameter id from the given annotation array, and returns it. If there is
     * no RESTID annotation found, null is returned.
     *
     * @param ann The array of annotations to process.
     * @return The (possibly null) parameter id.
     */
    private String getParameterId(Annotation[] ann)
    {
        String id = null;

        for (int i = 0; i < ann.length && id == null; i++)
        {
            if (ann[i] instanceof RESTID)
                id = ((RESTID) ann[i]).id();
        }

        return id;
    }

    @REST(path = "test")
    public void test(String s, int i, long[] l, Object ... o)
    {

    }

    // ===========================================================================
    @Override
    public void registered(Object t)
    {
        Class<?> cl = t.getClass();
        if (t.getClass().getName().indexOf("$$EnhancerByCGLIB$$") > 0) cl = t.getClass().getSuperclass();

        for (Method m : cl.getMethods())
        {
            if (m.isAnnotationPresent(REST.class))
            {
                REST r = m.getAnnotation(REST.class);
                RESTMethod rm = new RESTMethod();
                rm.setMethod(m);
                rm.setObject(t);
                methods.put(r.path(), rm);
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
    @Override
    public void unregistered(Object t)
    {
        for (Method m : t.getClass().getMethods())
        {
            if (m.isAnnotationPresent(REST.class))
            {
                REST r = m.getAnnotation(REST.class);
                methods.remove(r.path());
                logger.debug("Registering REST path " + r.path());
            }
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
