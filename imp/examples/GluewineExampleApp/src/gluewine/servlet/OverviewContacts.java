package gluewine.servlet;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;

import gluewine.entities.Contact;

public class OverviewContacts extends GluewineServlet {

	/* We call on this method in the browser by adressing the following link: 
	 * http://localhost:portnumber/contacts/
	 */
	@Override
	public String getContextPath() 
	{
		return "contacts";
	}
	
	@Glue
    private HibernateSessionProvider provider;
	
	@Glue(properties = "html.properties")
    private Properties html_prop;
	
	@Transactional
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		List<Contact> contacts = provider.getSession().getAll(Contact.class);
    	
		resp.setContentType("text/html");
        
		StringBuilder b = new StringBuilder();
        
        b.append(html_prop.getProperty("beginDoc"));
        b.append("Contacts"); //title in head
        b.append(html_prop.getProperty("head"));
        b.append(html_prop.getProperty("beginHeader"));
        b.append("Contacts"); //header h1
        b.append(html_prop.getProperty("endHeader"));
        
        b.append(html_prop.getProperty("btn_logout"));
        
        //table contacts
        b.append(html_prop.getProperty("tableHeaderContacts"));     
        
        for (Contact contact : contacts) {
        	b.append("		<tr>");
        	b.append("			<td> " + contact.getId() + "</td>");
    		b.append("			<td> " + contact.getFirstname() + "</td>");
    		b.append("			<td> " + contact.getLastname() + "</td>");
    		b.append("			<td> " + contact.getEmail() + "</td>");
    		b.append("			<td> " + contact.getPhoneNumber() + "</td>");
    		b.append("		</tr>"); 
        }
        b.append(html_prop.getProperty("tableEnd"));       
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
		
    }
}