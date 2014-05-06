package gluewine.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;


public class Adminpanel extends GluewineServlet {

	@Override
	public String getContextPath() {
		return "adminpanel";
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
        b.append("		<h1>Adminpanel</h1>");
        b.append("			<a href='http://localhost:8000/contacts/'>");
        b.append("				<input type='button' value='Overview contacts' class='btn'/>");
        b.append("			</a> </br>");
        b.append("			<a href='http://localhost:8000/addcontact/'>");
        b.append("				<input type='button' value='Add contact' class='btn'/>");
        b.append("			</a> </br>");
        b.append("			<a href='http://localhost:8000/deletecontact/'>");
        b.append("				<input type='button' value='Delete contact' class='btn'/>");
        b.append("			</a> </br>");
        b.append("			<a href='http://localhost:8000/modifycontact/'>");
        b.append("				<input type='button' value='Modify contact' class='btn'/>");
        b.append("			</a> </br>");
        
        
        
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
