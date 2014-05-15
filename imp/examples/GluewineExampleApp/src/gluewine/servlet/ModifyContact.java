package gluewine.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;

import gluewine.entities.Contact;

public class ModifyContact extends GluewineServlet {

	@Override
	public String getContextPath() {
		return "modifycontact";
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
        b.append("Modify contact"); //title in head
        b.append(html_prop.getProperty("head"));
        b.append(html_prop.getProperty("beginHeader"));
        b.append("Modify contact"); //header h1
        b.append(html_prop.getProperty("endHeader"));
                
        //table contacts
        b.append(html_prop.getProperty("tableHeaderModContacts"));
        
        for (Contact contact : contacts) {
        	b.append("<tr>");
        	b.append("	<form action='ModifyContact' method='POST'>");
        	b.append("	<td>");
        	b.append("			<input type='text' name='id' value='" + contact.getId() + "'/>");
        	b.append("	</td>");
        	b.append("	<td>");
        	b.append("			<input type='text' name='firstname' value='"+ contact.getFirstname() +"'/>" );
        	b.append("	</td>");
        	b.append("	<td>");
        	b.append("			<input type='lastname' name='lastname' value='"+ contact.getLastname() +"'/>");
        	b.append("	</td>");
        	b.append("	<td>");
        	b.append("			<input type='text' name='email' value='"+ contact.getEmail() +"'/>");
        	b.append("	</td>");
        	b.append("	<td>");
        	b.append("			<input type='text' name='phone' value='"+ contact.getPhoneNumber() +"'/>");
        	b.append("	</td>");
        	b.append("	<td>");
        	b.append("			<input type='submit' value='Modify contact' name='modifyContact' class='searchButton'/>");
        	b.append(	"</td>");
        	b.append("	</form>"); 
        	b.append("</tr>");
        }
        b.append(html_prop.getProperty("tableEnd"));        
 		b.append(" </br>"); 		
 		b.append(				html_prop.getProperty("btn_back"));        
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
		String id1 = req.getParameter("id");
		System.out.println("test " + id1);
		
		String firstname = req.getParameter("firstname");
        String lastname = req.getParameter("lastname");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        
        long id = Long.parseLong(id1);      
        
        Contact contact = (Contact) provider.getSession().get(Contact.class, id);
                
        
        if (contact != null) {
        	
        	contact.setFirstname(firstname);
        	contact.setLastname(lastname);
        	contact.setPhoneNumber(phone);
        	contact.setEmail(email);
        	
        	provider.getSession().update(contact);
        	provider.commitCurrentSession();
        }
        else {
        	System.out.println("There is no contact with id " + id);
        }
        
        resp.sendRedirect("http://localhost:8000/modifycontact/"); 
    }
}