package gluewine.rest;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
        
        b.append("	<form action='ModifyContact' method='POST'>");
        
        //table contacts
        b.append(html_prop.getProperty("tableHeaderModContacts"));
        
        for (Contact contact : contacts) {
        	b.append("<tr>");
        	b.append("<td> " + contact.getId() + "</td>");
        	b.append("<td> " + contact.getFirstname() + "</td>");
        	b.append("<td> " + contact.getLastname() + "</td>");
        	b.append("<td> " + contact.getEmail() + "</td>");
        	b.append("<td> " + contact.getPhoneNumber() + "</td>");
        	b.append("<td><center><input type='radio' name='modify' value='"+ contact.getId() +"'</center></td>");
        	b.append("</tr>");
        }
        b.append(html_prop.getProperty("tableEnd"));
        
 		b.append(" </br>");
 		
 		b.append(				html_prop.getProperty("btn_back"));
        b.append("				<input type='submit' value='Modify contact' name='submit' class='btn'/>");
 		b.append("		</form>"); 
        
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
		String modifyContact = req.getParameter("modify");
        	
        long id = Long.parseLong(modifyContact);
        
        Contact contact = (Contact) provider.getSession().get(Contact.class, id);
        
        //EDIT
        if (contact != null) {
        	System.out.println(""+ id);
        	
        	String firstname = contact.getFirstname();
        	String lastname = contact.getLastname();
        	String email = contact.getEmail();
        	String phone = contact.getPhoneNumber();
        	
        	//making sure we have the contactdata in the new servlet modifyanswer
        	req.getSession().setAttribute("id", id);
        	req.getSession().setAttribute("firstname", firstname);
        	req.getSession().setAttribute("lastname", lastname);
        	req.getSession().setAttribute("email", email);
        	req.getSession().setAttribute("phone", phone);
        	
        	/*HttpSession session = req.getSession();          	
        	session.setAttribute("id", id);
        	session.setAttribute("firstname", firstname);
        	session.setAttribute("lastname", lastname);
        	session.setAttribute("email", email);
        	session.setAttribute("phone", phone);*/
        	        	
        	resp.setContentType("text/html");
            
            StringBuilder b = new StringBuilder();
                
            b.append(html_prop.getProperty("beginDoc"));
            b.append("Modify contact"); //title in head
            b.append(html_prop.getProperty("head"));
            b.append(html_prop.getProperty("beginHeader"));
            b.append("Modify contact"); //header h1
            b.append(html_prop.getProperty("endHeader"));
            
            //form contact
            b.append("			<form action='/modifyanswer' method='POST'>");
            b.append("				<label for='id' class='lbl'> Id: </label>");
    		b.append("				<input type='text' name='id' value='" + id + "' class='inpt' disabled='true'/>");
    		b.append("				</br>");
    		b.append("				<label for='firstname' class='lbl'>Firstname:</label>");
		    b.append("				<input type='text' name='firstname' value='"+ firstname +"' class='inpt'/>" );
		    b.append("				</br>");
		    b.append("				<label for='lastname' class='lbl'>Lastname:</label>");
		    b.append("				<input type='lastname' name='lastname' value='"+ lastname +"' class='inpt'/>");
		    b.append("				</br>");
		    b.append("				<label for='email' class='lbl'>Email Adress:</label>");
		    b.append("				<input type='text' name='email' value='"+ email +"' class='inpt'/>");
		    b.append("				</br>");
		    b.append("				<label for= 'phone' class='lbl'>Phone:</label>");
		    b.append("				<input type='text' name='phone' value='"+ phone +"' class='inpt'/>");
		    b.append("				</br></br>");
		    b.append(				html_prop.getProperty("btn_back"));
		    b.append("				<input type='submit' value='Modify contact' name='submit' class='btn'/>");
		    b.append("		</form>");
		    
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
        else
            System.out.println("There is no contact with id " + id);
    }
}