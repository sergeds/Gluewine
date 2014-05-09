package gluewine.rest;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;

import gluewine.entities.Contact;

public class AddContact extends GluewineServlet {

	/*
	 * We call on this method in the browser by adressing the following link:
	 * 
	 * http://localhost:8000/addcontact/
	 */
	@Override
	public String getContextPath() {
		return "addcontact";
	}
	
	@Glue
    private HibernateSessionProvider provider;
	
	@Glue(properties = "html.properties")
    private Properties html_prop;
	
	@Transactional
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
        resp.setContentType("text/html");
        
        StringBuilder b = new StringBuilder();
        
        b.append(html_prop.getProperty("beginDoc"));
        b.append("Add contact"); //title in head
        b.append(html_prop.getProperty("head"));
        b.append(html_prop.getProperty("beginHeader"));
        b.append("Add contact"); //header h1
        b.append(html_prop.getProperty("endHeader"));
        
        b.append("			<form action='AddContact' method='POST'>");
        b.append("				<label for='firstname' class='lbl'>Firstname:</label>");
        b.append("				<input type='text' name='firstname' class='inpt'/>");
        b.append("				</br>");
        b.append("				<label for='lastname' class='lbl'>Lastname:</label>");
        b.append("				<input type='lastname' name='lastname' class='inpt'/>");
        b.append("				</br>");
        b.append("				<label for='email' class='lbl'>Email Adress:</label>");
        b.append("				<input type='text' name='email' class='inpt'/>");
        b.append("				</br>");
        b.append("				<label for= 'phone' class='lbl'>Phone:</label>");
        b.append("				<input type='text' name='phone' class='inpt'/>");
        b.append("				</br></br>");
        b.append(				html_prop.getProperty("btn_back"));
        b.append("				<input type='submit' value='Add contact' name='submit' class='btn'/>");
        b.append("			</form>");
        
        b.append(html_prop.getProperty("endDoc"));
        resp.setContentLength(b.length());
        
        try
        {
            resp.getWriter().println(b.toString()); //print b
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
        String newFirstname = req.getParameter("firstname");
        String newLastname = req.getParameter("lastname");
        String newEmail = req.getParameter("email");
        String newPhone = req.getParameter("phone");
        
        String regexPhone = "([0]|\\+32)\\W*([0-9][0-9][0-9])\\W*([0-9][0-9]{2})\\W*([0-9]{3})?";
        String regexEmail = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$";
        
        if (newPhone.matches(regexPhone) && newEmail.matches(regexEmail)) 
        {
        	Contact newContact = new Contact();
	        newContact.setFirstname(newFirstname);
	        newContact.setLastname(newLastname);
	        newContact.setPhoneNumber(newPhone);
	        newContact.setEmail(newEmail);
	        
	        provider.getSession().add(newContact);
	        provider.commitCurrentSession();
	        
	        resp.sendRedirect("http://localhost:8000/addcontact/");	        
        }
        else 
        {	        
        	if (!newPhone.matches(regexPhone))         	
        		JOptionPane.showMessageDialog(null, "The phone number has to be like: +32 123 456 789 or 0123 456 789", "error", JOptionPane.ERROR_MESSAGE);
    			        	
        	if (!newEmail.matches(regexEmail))
        		JOptionPane.showMessageDialog(null, "Incorrect email address", "error", JOptionPane.ERROR_MESSAGE);
			
        	resp.sendRedirect("http://localhost:8000/addcontact/");        	
        }        
    }
}