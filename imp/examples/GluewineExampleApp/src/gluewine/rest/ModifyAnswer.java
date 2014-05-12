package gluewine.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
		/*
		HttpSession session = req.getSession();
		String newFirstname = (String) session.getAttribute("firstname");  
		String newLastname = (String) session.getAttribute("lastname");  
		String newEmail = (String) session.getAttribute("email");  
		String newPhone = (String) session.getAttribute("phone");  */
		
		
		String newFirstname = (String) req.getSession().getAttribute("firstname");  
		String newLastname = (String) req.getSession().getAttribute("lastname");  
		String newEmail = (String) req.getSession().getAttribute("email");  
		String newPhone = (String) req.getSession().getAttribute("phone");		
		
		System.out.println("firstname: " + newFirstname);
		
		String id1 = (String) req.getSession().getAttribute("id");  
		long id = 23;//Long.parseLong(id1);
		
        
		
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