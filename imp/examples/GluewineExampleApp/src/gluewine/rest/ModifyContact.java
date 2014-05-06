package gluewine.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;


public class ModifyContact extends GluewineServlet {

	@Override
	public String getContextPath() {
		return "modifycontact";
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
        b.append("		<style type='text/css'>"
        		+ "				a:link { color: #000000; text-decoration: none; }"
        		+ "				.btn { border-radius:6px; text-indent:-1.08px; border:1px solid #dcdcdc; display:inline-block; color:#777777; font-family:arial; font-size:15px; font-weight:bold; font-style:normal; height:50px; line-height:50px; width:200px; text-decoration:none; text-align:center;}"
        		+ "		</style>");        		
        b.append("  </head>");
        b.append("	<body>");
        b.append("		<h1>Modify contact</h1>");
        b.append("			<form action='ModifyContact' method='POST'>");
        b.append("				<label for='firstname'>Firstname:</label>");
        b.append("				<input type='text' name='firstname'/>");
        b.append("				</br><label for='lastname'>Lastname:</label>");
        b.append("				<input type='lastname' name='lastname'/>");
        b.append("				</br><label for='email'>Email Adress</label>");
        b.append("				<input type='text' name='email'/>");
        b.append("				</br><label for= 'phone'>Phone:</label>");
        b.append("				<input type='text' name='phone'/>");
        b.append("				</br><input type='submit' value='Modify contact' name='submit' class='btn'/>");
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
        
    }
}