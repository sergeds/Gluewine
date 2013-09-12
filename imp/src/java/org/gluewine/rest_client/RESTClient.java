package org.gluewine.rest_client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.gluewine.rest.REST;
import org.gluewine.rest.RESTID;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Allows to access the REST Server API through the
 * user of proxies.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class RESTClient implements InvocationHandler
{
    // ===========================================================================
    /**
     * The base url to use.
     */
    private String baseURL = null;

    /**
     * The session id.
     */
    private String sessionId = null;

    /**
     * Map of clients indexed on their base urls.
     */
    private static Map<String, RESTClient> clients = new HashMap<String, RESTClient>();

    /**
     * The xstream to use.
     */
    private XStream stream = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param url The url to use.
     */
    private RESTClient(String url)
    {
        this.baseURL = url;

        if (!baseURL.endsWith("/")) baseURL = baseURL + "/";
        if (!baseURL.endsWith("REST/")) baseURL = baseURL + "REST/";
        stream = new XStream(new StaxDriver());
    }

    // ===========================================================================
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        REST annot = method.getAnnotation(REST.class);
        if (annot != null)
        {
            String path = annot.path();
            if (path.startsWith("/")) path = path.substring(1);
            URL obj = new URL(baseURL + path);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "RESTClient/API");
            con.setRequestProperty("Accept-Language", "utf-8");
            if (sessionId != null) con.setRequestProperty("Gluewine-Session", sessionId);

            try
            {
                StringBuilder b = new StringBuilder();
                Annotation[][] panns = method.getParameterAnnotations();
                for (int i = 0; i < panns.length; i++)
                {
                    Annotation[] anns = panns[i];
                    for (Annotation ann : anns)
                    {
                        if (ann instanceof RESTID)
                        {
                            RESTID rid = (RESTID) ann;
                            b.append(rid.id()).append("=").append(toString(args[i]));
                            break;
                        }
                    }

                    if (i < panns.length - 1) b.append("&");
                }
                if (b.length() > 0) b.append("&");
                b.append("format=xml");

                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(b.toString());
                wr.flush();
                Object result = null;
                if (!method.getReturnType().equals(Void.TYPE))
                {
                    // If the return type is either Input- or Outpustream, we return it, but we must
                    // make sure that the connection is not closed!.
                    if (method.getReturnType().equals(InputStream.class)) return con.getInputStream();
                    else if (method.getReturnType().equals(OutputStream.class))
                    {
                        con.getInputStream();
                        return con.getOutputStream();
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null)
                        response.append(inputLine);
                    in.close();
                    result = fromString(response.toString(), method.getReturnType());
                }

                wr.close();

                if (con.getResponseCode() != HttpServletResponse.SC_OK) throw new RuntimeException(con.getResponseMessage());
                else return result;
            }
            catch (Throwable e)
            {
                if (con.getResponseCode() == 401) throw new RESTAuthenticationRequiredException();
                else throw new RuntimeException(e);
            }
        }
        else throw new RuntimeException("The method " + method.getName() + " is not @REST annotated!");
    }

    // ===========================================================================
    /**
     * Deserializes the string given to an object of the given class.
     *
     * @param value The String to deserialize.
     * @param target The target class.
     * @return The resulting object.
     */
    private Object fromString(String value, Class<?> target)
    {
        if (target.equals(String.class)) return value;
        else return stream.fromXML(value);
    }

    // ===========================================================================
    /**
     * Returns the String representation of an object.
     *
     * @param value The string representation.
     * @return The String.
     * @throws UnsupportedEncodingException If the parameter cannot be urlencoded.
     */
    private String toString(Object value) throws UnsupportedEncodingException
    {
        String res = null;
        switch (value.getClass().getSimpleName().toLowerCase(Locale.getDefault()))
        {
            case "boolean" :
            case "byte" :
            case "char" :
            case "character" :
            case "double" :
            case "float" :
            case "long" :
            case "int" :
            case "short" :
                res = value.toString();
                break;
            case "string" :
                res = (String) value;
                break;

            default :
                res = stream.toXML(value);
        }

        res = URLEncoder.encode(res, "utf8");
        return res;

    }

    // ===========================================================================
    /**
     * Sets the session id.
     *
     * @param session The sessionid.
     */
    public void setSessionId(String session)
    {
        this.sessionId = session;
    }

    // ===========================================================================
    /**
     * Clears the session id.
     */
    public void clearSessionId()
    {
        this.sessionId = null;
    }

    // ===========================================================================
    /**
     * Creates and returns a proxy to the service specified by the given interface.
     * The session id is used when security is activated (at the server side).
     *
     * @param <T> The class to return.
     * @param t The interface to be proxied.
     * @return The Proxy to the interface.
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> t)
    {
        return (T) Proxy.newProxyInstance(t.getClassLoader(), new Class<?>[] {t}, this);
    }

    // ===========================================================================
    /**
     * Returns a client that will connect to the given url.
     *
     * @param url The url to connect to.
     * @return The client.
     */
    public static synchronized RESTClient getClient(String url)
    {
        RESTClient client = clients.get(url);
        if (client == null)
        {
            client = new RESTClient(url);
            clients.put(url, client);
        }
        return client;
    }
}
