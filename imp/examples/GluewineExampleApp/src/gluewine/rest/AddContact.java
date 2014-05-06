package gluewine.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;

import gluewine.entities.Contact;


public class AddContact extends GluewineServlet {

	@Override
	public String getContextPath() {
		return "addcontact";
	}
	
	@Glue
    private HibernateSessionProvider provider;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
        resp.setContentType("text/html");
        
        StringBuilder b = new StringBuilder(""
        		+"<html>"
        		+ " <head> "
        		+ "		<title> Add contact </title> "
        		+ " </head>");
        b.append("	<body>");
        b.append("		<h1>Add contact</h1>");
        b.append("			<form action='AddContact' method='POST'>");
        b.append("				<label for='firstname'>Firstname:</label>");
        b.append("				<input type='text' name='firstname'/>");
        b.append("				</br><label for='lastname'>Lastname:</label>");
        b.append("				<input type='lastname' name='lastname'/>");
        b.append("				</br><label for='email'>Email Adress</label>");
        b.append("				<input type='text' name='email'/>");
        b.append("				</br><label for= 'phone'>Phone:</label>");
        b.append("				<input type='text' name='phone'/>");
        b.append("				</br><input type='submit' value='Add contact' name='submit'/>");
        b.append("				</form>");
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
        String newFirstname = req.getParameter("firstname");
        String newLastname = req.getParameter("lastname");
        String newEmail = req.getParameter("email");
        String newPhone = req.getParameter("phone");
        
        String regexPhone = "([0]|\\+32)\\W*([0-9][0-9][0-9])\\W*([0-9][0-9]{2})\\W*([0-9]{3})?";
        String regexEmail = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$";
        
        if (newPhone.matches(regexPhone) && newEmail.matches(regexEmail)) {
        	Contact newContact = new Contact();
	        newContact.setFirstname(newFirstname);
	        newContact.setLastname(newLastname);
	        newContact.setPhoneNumber(newPhone);
	        newContact.setEmail(newEmail);
	        
	        provider.getSession().add(newContact);
	        provider.commitCurrentSession();
	        
	        resp.sendRedirect("http://localhost:8000/addcontact/");
	        
        }
        else {	        
        	if (!newPhone.matches(regexPhone))
        		JOptionPane.showMessageDialog(null, "The phone number has to be like: +32 123 456 789 or 0123 456 789", "error", JOptionPane.ERROR_MESSAGE);
    			resp.sendRedirect("http://localhost:8000/addcontact/");
        	if (!newEmail.matches(regexEmail))
        		JOptionPane.showMessageDialog(null, "Incorrect email address", "error", JOptionPane.ERROR_MESSAGE);
				resp.sendRedirect("http://localhost:8000/addcontact/");
        }        
    }

}