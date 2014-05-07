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
        		+"<html>");
        b.append(" 	<head> ");
        b.append("		<title> Adminpanel </title> ");
        b.append("		<link rel='stylesheet' type='text/css' href='style.css' />");
        b.append("			<style type='text/css'>"
        		+ "				a:link { color: #000000; text-decoration: none; }"
        		+ "				.btn { border-radius:6px; text-indent:-1.08px; border:1px solid #dcdcdc; display:inline-block; color:#777777; font-family:arial; font-size:15px; font-weight:bold; font-style:normal; height:50px; line-height:50px; width:200px; text-decoration:none; text-align:center;}"
        		+ "				.lbl { width:120px; display: block; float: left;}"
        		+ "				.inpt { width:250px; }"
        		+ "				.h1 { width:100%; background-color:#a80321; height:20%; color:#ffffff; text-align:center; }"	
        		+ "			</style>");        		
        b.append("  	</head>");
        b.append("	<body>");
        b.append("		<h1 class='h1'>Add contact</h1>");
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
        b.append("				<a href='http://localhost:8000/adminpanel/'>");
 		b.append("					<input type='button' value='<- Back' class='btn'/>");
 		b.append("				</a>");
        b.append("				<input type='submit' value='Add contact' name='submit' class='btn'/>");
        b.append("			</form>");
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