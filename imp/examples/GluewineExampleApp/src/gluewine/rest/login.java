package gluewine.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import gluewine.entities.User;

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
		 final long now = new Date().getTime();
	        resp.setDateHeader("Date", now);
	        resp.setDateHeader("Expires", now);
			
	        resp.setContentType("text/html");
	        StringBuilder b = new StringBuilder("<HTML><HEAD>");
	        b.append("<TITLE>Login</TITLE>");
	        b.append("</HEAD>");
	        //
	        b.append("<H1>Login</H1>");
	        b.append("<form action='login' method='POST'>");
	        b.append("<label for='username'>Username:</label>");
	        b.append("<input type='text' name='username'/>");
	        b.append("</br><label for='password'>Password:</label>");
	        b.append("<input type='password' name='password'/>");
	        b.append("</br><input type='submit' value='submit' name='submit'/>");
	        b.append("</form>");
	        
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
		
		//resp.setContentType("text/html");
		//resp.getWriter().write("<html>"
        		//+ "<body>"
				//+ "<h1>login</h1>"
        		//+ "<form action='login' method='POST'>"
        		//+ "<label for='username'>Username:</label>"
        		//+ "<input type='text' name='username'/>"
        		//+ "</br><label for='password'>Password:</label>"
        		//+ "<input type='password' name='password'/>"
        		//+ "</br><input type='submit' value='submit' name='submit'/>"
        		//+ "</form>"
        		//+ "</body>"
        		//+ "</html>");
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        resp.setContentType("text/html");
		
        
        
        List<gluewine.entities.User> users = provider.getSession().getAll(User.class);
        
        if (users.isEmpty()) {
        	resp.getWriter().write(""
    				+ "<html>"
    					+ "<head>"
    						+ "<title>Login</title>"
    					+ "</head>"
            			+ "<body>"
            				+ "<h1>Testing</h1>"
            					+ "<p> I'm terribly sorry, but no users were found.</p>"
    					+ "</body>"
    				+ "</html>");
        }
        else {
        	resp.getWriter().write(""
    				+ "<html>"
    					+ "<head>"
    						+ "<title>Login</title>"
    					+ "</head>"
            			+ "<body>"
            				+ "<h1>Testing</h1>"
            					+ "<p> Dag " + username + "</p>");
        	
        	
    		resp.getWriter().write(""
    				    	+ "<h1>Users</h1>"
            					+ "<table border=\"1\">"
    	        					+ "<tr>"
    									+ "<th> Id </th>"
    									+ "<th> isAdmin </th>"
    									+ "<th> username </th>"
    								+ "</tr>");
    		
    		for (User user : users) {
        		resp.getWriter().write(""
        							+ "<tr>"
        								+ "<td> " + user.getId() + "</td>"
        								+ "<td> " + user.getUsername() + "</td>"
        								+ "<td> " + user.getRole()+ "</td>"   								    							
        							+ "</tr>");  
    		}
    		
    		resp.getWriter().write(""
    						+ "</table>"
    					+ "</body>"
            		+ "</html>");
    	
        }
        
        /*
        for(User user : users)
        {
        	if( user.getUsername().equals(username))
        	{
        		if (user.getPassword().equals(password))
        		{        			
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
        	} else resp.sendError(HttpServletResponse.SC_CONFLICT, "wrong username of password");
        }*/
        
    }
}