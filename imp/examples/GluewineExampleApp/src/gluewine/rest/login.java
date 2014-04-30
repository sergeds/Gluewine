package gluewine.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.persistence_jpa.Filter;
import org.gluewine.persistence_jpa.FilterLine;
import org.gluewine.persistence_jpa.FilterOperator;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;
import org.gluewine.persistence.Transactional;

import org.gluewine.jetty.GluewineServlet;



public class login extends GluewineServlet {

	@Override
	public String getContextPath() {
		return "login";
	}
	
	@Glue
    private HibernateSessionProvider provider;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType("text/html");
		resp.getWriter().write("<html>"
        		+ "<body>"
				+ "<h1>login</h1>"
        		+ "<form action='login' method='POST'>"
        		+ "<label for='username'>Username:</label>"
        		+ "<input type='text' name='username'/>"
        		+ "</br><label for='password'>Password:</label>"
        		+ "<input type='password' name='password'/>"
        		+ "</br><input type='submit' value='submit' name='submit'/>"
        		+ "</form>"
        		+ "</body>"
        		+ "</html>");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        
        
        resp.setContentType("text/html");
        StringBuilder b = new StringBuilder("<HTML><HEAD>");
        b.append("<TITLE>Gluewine framework</TITLE>");
        b.append("</HEAD>");
        b.append("Welcome " + username);
        b.append("</HEAD>");
        resp.setContentLength(b.length());
        try
        {
            resp.getWriter().println(b.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }
}