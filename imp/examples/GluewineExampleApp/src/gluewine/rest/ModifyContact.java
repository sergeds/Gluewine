package gluewine.rest;

import java.io.IOException;
import java.util.List;

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
	
	@Transactional
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		List<Contact> contacts = provider.getSession().getAll(Contact.class);
		
        resp.setContentType("text/html");
        
        StringBuilder b = new StringBuilder(""
        		+"<html>");
        b.append(" 	<head> ");
        b.append("		<title> Adminpanel </title> ");
        b.append("		<link rel='stylesheet' type='text/css' href='style.css' />");
        b.append("		<style type='text/css'>"
        		+ "				a:link { color: #000000; text-decoration: none; }"
        		+ "				.btn { border-radius:6px; text-indent:-1.08px; border:1px solid #dcdcdc; display:inline-block; color:#777777; font-family:arial; font-size:15px; font-weight:bold; font-style:normal; height:50px; line-height:50px; width:200px; text-decoration:none; text-align:center;}"
        		+ "				.lbl { width:120px; display: block; float: left; font-family:arial; }"
        		+ "				.inpt { width:250px; font-family:arial; }"
        		+ "				.h1 { width:100%; background-color:#a80321; height:20%; color:#ffffff; text-align:center; font-family:arial; }"	
        		+ "		</style>");        		
        b.append("  </head>");
        b.append("	<body>");
        b.append("		<h1 class='h1'>Modify contact</h1>");
        
        b.append("			<form action='ModifyContact' method='POST'>");
        b.append("			<table border=\"1\">");     
        b.append("<tr>");   
        b.append("<th> Id </th>"); 
        b.append("<th> Firstname </th>"); 
        b.append("<th> Lastname </th>"); 
        b.append("<th> Email </th>"); 
        b.append("<th> Phone number </th>"); 
        b.append("<th> Modify </th>");
        b.append("</tr>"); 
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
        b.append("</table>"); 
 		b.append(" </br>");
 		
        b.append("				<a href='http://localhost:8000/adminpanel/'>");
 	 	b.append("					<input type='button' value='<- Back' class='btn'/>");
 	 	b.append("				</a>");
        b.append("				<input type='submit' value='Modify contact' name='submit' class='btn'/>");
 		b.append("		</form>"); 
        
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
	
	@Transactional
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
		String modifyContact = req.getParameter("modify");
        	
        	long id = Long.parseLong(modifyContact);
            Contact contact = (Contact) provider.getSession().get(Contact.class, id);
            if (contact != null) {
                System.out.println(""+ id);
                
                List<Contact> contacts = provider.getSession().getAll(Contact.class);
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
                b.append("		<h1 class='h1'>Modify contact</h1>");
                b.append("			<form action='ModifyContact' method='POST'>");
                		for (Contact contac : contacts) 
                			{
                			 if(contac.getId() == id)
                			 {
                		
				                   b.append("				<label for='firstname' class='lbl'>Firstname:</label>");
				                   b.append("				<input type='text' name='firstname' value='"+ contac.getFirstname() +"' class='inpt'/>" );
				                   b.append("				</br>");
				                   b.append("				<label for='lastname' class='lbl'>Lastname:</label>");
				                   b.append("				<input type='lastname' name='lastname' value='"+ contac.getLastname() +"' class='inpt'/>");
				                   b.append("				</br>");
				                   b.append("				<label for='email' class='lbl'>Email Adress:</label>");
				                   b.append("				<input type='text' name='email' value='"+ contac.getEmail() +"' class='inpt'/>");
				                   b.append("				</br>");
				                   b.append("				<label for= 'phone' class='lbl'>Phone:</label>");
				                   b.append("				<input type='text' name='phone' value='"+ contac.getPhoneNumber() +"' class='inpt'/>");
				                   b.append("				</br></br>");
				                   b.append("				<a href='http://localhost:8000/modifycontact/'>");
				                   b.append("					<input type='button' value='<- Back' class='btn'/>");
				                   b.append("				</a>");
				                   b.append("				<input type='submit' value='Modify contact' name='submit' class='btn'/>");
				                   b.append("		</form>");
                			 }
                			}
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
            else
               System.out.println("There is no contact with id " + id);
        
    }
}