package gluewine.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane; 

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Properties;

import gluewine.entities.User;

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
 		b.append("					<input type='button' value='Skip login' class='btnLogin'/>");
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
        
        resp.setContentType("text/html");
        
        List<gluewine.entities.User> users = provider.getSession().getAll(gluewine.entities.User.class);

        
        if (users.isEmpty()) 
        {
        	resp.getWriter().write(""
        			+ "<html>"
        			+ "		<head>"
        			+ "			<title>Login</title>"
        			+ "		</head>"
        			+ "		<body>"
        			+ "			<h1>Testing</h1>"
        			+ "				<p> I'm terribly sorry, but no users were found.</p>"
        			+ "		</body>"
        			+ "</html>");
        }
        else {
        	/* With this boolean we know when the user is found.
        	 * When the user is not found, we redirect to the loginpage
        	 */
        	Boolean userFound = false;
        	
        	for (gluewine.entities.User user : users)
            {        		
            	if (user.getUsername().equals(username) && user.getPassword().equals(password)) 
            	{
            		userFound = true;
            		
            		StringBuilder b = new StringBuilder(""
            				+ "<html>");
            		b.append("		<head>");
            		b.append("			<title> Gluewine framework </title>");
            		b.append("		</head>");
            		b.append("		<body>");
            		b.append("			<p>Welcome, " + username + " </p>");
            		b.append("		</body>");
            		b.append("</html>");
            		resp.setContentLength(b.length());
            	
	            	try {
	            		resp.getWriter().println(b.toString());
	            	}
	            	catch(IOException e)
	            	{
	            		e.printStackTrace();
	            		
	            		try {
	            			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
	            		}
	            		catch (IOException e1)
	            		{
	            			e1.printStackTrace();
	            		}
	            	}
            	}
            }//end for
        	
        	if (!userFound) {
        		resp.sendRedirect("http://localhost:8000/login/");
        	}
        }// end else users.isEmpty()   
    }//end doPost
}