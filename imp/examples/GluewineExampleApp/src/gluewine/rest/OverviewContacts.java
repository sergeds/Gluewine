package gluewine.rest;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;

import gluewine.entities.Contact;



public class OverviewContacts extends GluewineServlet {

	@Override
	public String getContextPath() {
		return "contacts";
	}
	
	@Glue
    private HibernateSessionProvider provider;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		List<Contact> contacts = provider.getSession().getAll(Contact.class);
    	
    	resp.setContentType("text/html");
		resp.getWriter().write(""
				+ "<html>"
					+ "<head>"
						+ "<title>Contacts </title>"
					+ "</head>"
        			+ "<body>"
        				+ "<h1>Contacts</h1>"
        					+ "<table border=\"1\">"
	        					+ "<tr>"
									+ "<th> Id </th>"
									+ "<th> Firstname </th>"
									+ "<th> Lastname </th>"
									+ "<th> Email </th>"
									+ "<th> Phone number </th>"
								+ "</tr>");
		
		for (Contact contact : contacts) {
    		resp.getWriter().write(""
    							+ "<tr>"
    								+ "<td> " + contact.getId() + "</td>"
    								+ "<td> " + contact.getFirstname() + "</td>"
    								+ "<td> " + contact.getLastname() + "</td>"
    								+ "<td> " + contact.getEmail() + "</td>"
    								+ "<td> " + contact.getPhoneNumber() + "</td>"    								    							
    							+ "</tr>");  
		}
		
		resp.getWriter().write(""
						+ "</table>"
					+ "</body>"
        		+ "</html>");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        
    }
	

}
