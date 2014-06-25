package gluewine.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gluewine.entities.Contact;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;

public class DeleteContact extends GluewineServlet {
	
	/* We call on this method in the browser by adressing the following link: 
	 * http://localhost:portnumber/deletecontact/
	 */
	@Override
	public String getContextPath() 
	{
		return "deletecontact";
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
        b.append("Delete contact"); //title in head
        b.append(html_prop.getProperty("head"));
        b.append(html_prop.getProperty("beginHeader"));
        b.append("Delete contact"); //header h1
        b.append(html_prop.getProperty("endHeader"));
        
        b.append("		<input type='text name='search' class='searchTextbox' /> ");
        b.append("		<input type='button' name='btnSearch' value='Search' class='searchButton' />");
        b.append("				</br>");
        b.append("				</br>");
        b.append("				</br>");
        b.append("			<form action='DeleteContact' method='POST'>");
        
        //table contacts
        b.append(html_prop.getProperty("tableHeaderDelContacts"));
        
        for (Contact contact : contacts)
        {
        	b.append("				<tr>");
        	b.append("					<td> " + contact.getId() + "</td>");
        	b.append("					<td> " + contact.getFirstname() + "</td>");
        	b.append("					<td> " + contact.getLastname() + "</td>");
        	b.append("					<td> " + contact.getEmail() + "</td>");
        	b.append("					<td> " + contact.getPhoneNumber() + "</td>");
        	b.append("					<td><center><input type='checkbox' name='delete' value='"+ contact.getId() +"'></center></td>");
        	b.append("				</tr>");
        }        
        b.append(html_prop.getProperty("tableEnd"));
        
        b.append("		<p> </p>");
        b.append(		html_prop.getProperty("btn_back"));
 		b.append("		<input type='submit' value='Delete' class='btn'/>");
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
        String[] checkedContacts = req.getParameterValues("delete");
        
        for(int i=0; i<checkedContacts.length; i++)
        {
        	
        	long id = Long.parseLong(checkedContacts[i]);
            Contact contact = (Contact) provider.getSession().get(Contact.class, id);
            
            if (contact != null)
            {
                provider.getSession().delete(contact);
                provider.commitCurrentSession();
            }
            else
               System.out.println("There is no contact with id " + id);
        }
        resp.sendRedirect("http://localhost:8000/deletecontact/"); 
    }
}