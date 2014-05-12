package gluewine.rest;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;
import gluewine.entities.Contact;


public class ModifyAnswer extends GluewineServlet {
	
	@Override
	public String getContextPath() {
		return "modifyanswer";
	}
	
	@Glue
    private HibernateSessionProvider provider;
	
	@Transactional
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
		
		String newFirstname = req.getParameter("firstname");
		System.out.println("firstname: " + newFirstname);
        String newLastname = req.getParameter("lastname");
        String newEmail = req.getParameter("email");
        String newPhone = req.getParameter("phone");
        
        String id1 = req.getParameter("id");
		long id = Long.parseLong(id1);
		
        Contact contact = (Contact) provider.getSession().get(Contact.class, id);
        
        if (contact != null) {
        	System.out.println("test " + id);
        	contact.setFirstname(newFirstname);
        	contact.setLastname(newLastname);
        	contact.setPhoneNumber(newPhone);
        	contact.setEmail(newEmail);
        	provider.getSession().update(contact);
        	provider.commitCurrentSession();
        }
        else
        	System.out.println("There is no contact with id " + id);
    }

    
	
	@Transactional
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
    }
		
}