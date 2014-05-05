package gluewine.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;


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
        
    }

}