package gluewine.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane; 

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Properties;
import java.util.Calendar;

import java.text.*;

import gluewine.entities.User;
import gluewine.entities.LoginSession;

import org.gluewine.core.Glue;
import org.gluewine.persistence_jpa.Filter;
import org.gluewine.persistence_jpa.FilterLine;
import org.gluewine.persistence_jpa.FilterOperator;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;
import org.gluewine.persistence.Transactional;
import org.gluewine.jetty.GluewineServlet;

public class Login extends GluewineServlet {
	
	@Override
 	public String getContextPath() {
 		return "login";
 	}
 	
 	@Glue
    private HibernateSessionProvider provider;
 	
 	@Glue(properties = "html.properties")
    private Properties html_prop;
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
 		resp.setContentType("text/html");

 		StringBuilder b = new StringBuilder();
        
        b.append(html_prop.getProperty("beginDoc"));
        b.append("Login"); //title in head
        b.append(html_prop.getProperty("head"));
        b.append(html_prop.getProperty("beginHeader"));
        b.append("Login"); //header h1
        b.append(html_prop.getProperty("endHeader"));
        
  		b.append("			<form action='login' method='POST'>");
 		b.append("				<label for='username' class='lbl'>Username:</label>");
 		b.append("				<input type='text' name='username' class='inpt'/>");
 		b.append("				<br/>");
 		b.append("				<label for='password' class='lbl'>Password:</label>");
 		b.append("				<input type='password' name='password' class='inpt'/>");
 		b.append("				<br/><br/>");
 		
 		b.append("				<a href='http://localhost:8000/contacts/'>");
 		b.append("				</a>");
 		b.append("				<input type='submit' value='Login' name='submit' class='btnLogin'/>");
 		b.append("			</form>");		
 		
 		b.append(html_prop.getProperty("endDoc"));

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
 	
 	@Transactional
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {		
 		String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        List<gluewine.entities.User> users = provider.getSession().getAll(gluewine.entities.User.class);
        
        //We check if the userlist is empty, just in case something went worng in the database
	    if (users.isEmpty()) 
	    {
	    	resp.setContentType("text/html");
	 		StringBuilder b = new StringBuilder();
	 		
	    	b.append(html_prop.getProperty("beginDoc"));
	    	b.append(html_prop.getProperty("head"));
	    	b.append(html_prop.getProperty("no_users"));
	    	b.append(html_prop.getProperty("btn_login_back"));
	    	b.append(html_prop.getProperty("endDoc"));
	    	resp.setContentLength(b.length());
	 		resp.getWriter().println(b.toString());
	    }
	    else {
	    	//With this boolean we know when the user is found.
        	//When the user is not found, we redirect to the loginpage        	 
        	Boolean userFound = false;
        	
		    for (gluewine.entities.User user : users)
		    {				    	
		    	if (user.getUsername().equals(username) && user.getPassword().equals(password)) 
            	{
		    		userFound = true;
		    		//user toevoegen aan de sessie
		    		
		    		addUserSession(username, user.getRole());
		    		
				    if (user.getRole()) { //true => user is admin		            	
				    	resp.sendRedirect("http://localhost:8000/adminpanel/");
				    }
				    else {
				    	resp.sendRedirect("http://localhost:8000/contacts/");
				    }
            	}
		    }
		    
		    if (!userFound) {
		    	//JOptionPane.showMessageDialog(null, "Wrong login or password", "error", JOptionPane.ERROR_MESSAGE);
		    	resp.sendRedirect("http://localhost:8000/login/");
        	}
	    }
    }
 	
 	
 	@Transactional
 	private void addUserSession(String username, Boolean isAdmin) {
 		LoginSession newSession = new LoginSession();
 		String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm").format(Calendar.getInstance().getTime());
 		
 		//A user logged in, we save the username, the role and put the isActive value on true
 		newSession.setUsername(username);
 		newSession.setIsAdmin(isAdmin);
 		newSession.setIsActive(true);
 		newSession.setLoginTime(timeStamp);
        
 		provider.getSession().add(newSession);
 		provider.commitCurrentSession();
 	}
}