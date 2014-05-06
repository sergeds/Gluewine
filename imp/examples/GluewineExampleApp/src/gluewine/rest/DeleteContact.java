package gluewine.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gluewine.entities.Contact;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;


public class DeleteContact extends GluewineServlet {

	@Override
	public String getContextPath() {
		return "deletecontact";
	}
	
	@Glue
    private HibernateSessionProvider provider;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		List<Contact> contacts = provider.getSession().getAll(Contact.class);
		
        resp.setContentType("text/html");
        
        StringBuilder b = new StringBuilder(""
        		+"<html>");
        b.append(" 	<head> ");
        b.append("		<title> Adminpanel </title> ");
        b.append("		<link rel='stylesheet' type='text/css' href='style.css' />");
        b.append("		<style type='text/css'>"
        		+ "				a:link { color: #000000; text-decoration: none; }"
        		+ "				.btn { border-bottom-left-radius:6px; text-indent:-1.08px; border:1px solid #dcdcdc; display:inline-block; color:#777777; font-family:arial; font-size:15px; font-weight:bold; font-style:normal; height:50px; line-height:50px; width:200px; text-decoration:none; text-align:center;}"
        		+ "		</style>");        		
        b.append("  </head>");
        b.append("	<body>");
        b.append("		<h1>Delete contact</h1>");
        b.append("			<table border=\"1\">");     
        b.append("<tr>");   
        b.append("<th> Id </th>"); 
        b.append("<th> Firstname </th>"); 
        b.append("<th> Lastname </th>"); 
        b.append("<th> Email </th>"); 
        b.append("<th> Phone number </th>"); 
        b.append("</tr>"); 
        for (Contact contact : contacts) {
        	b.append("<tr>");
        	b.append("<td> " + contact.getId() + "</td>");
        	b.append("<td> " + contact.getFirstname() + "</td>");
        	b.append("<td> " + contact.getLastname() + "</td>");
        	b.append("<td> " + contact.getEmail() + "</td>");
        	b.append("<td> " + contact.getPhoneNumber() + "</td>");
        	b.append("</tr>");
        }
        b.append("</table>"); 
        b.append("	</body>");
        b.append("</html>");
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
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        
    }

}