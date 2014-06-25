
package gluewine.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.jetty.GluewineServlet;

public class TestServlets2 extends GluewineServlet {

    /*
     * To use this method in the browser, we will need to address the following link:
     * http://localhost:portnumber/TestServlet2
     *
     * We define this link in the method getContextPath()
     */
    @Override
    public String getContextPath() 
    {
        return "TestServlet2";
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html");
        StringBuilder b = new StringBuilder("");
        b.append("<html>");
        b.append("	<head>");
        b.append("		<title>Gluewine framework</title>");
        b.append("	</head>");
        b.append("	<body>");
        b.append("		<h1>Welcome to the Gluewine framework.</h1>");
        b.append("		<p>Please fill in your credentials in the form below: </p>");
        b.append("		<form action='TestServlet2' method='POST'>");
        b.append("			<label for='firstname'>FirstName:</label>");
        b.append("			<input type='text' name='firstname'/>");
        b.append("			</br><label for='lastname'>LastName:</label>");
        b.append("			<input type='text' name='lastname'/>");
        b.append("			</br><input type='submit' value='submit' name='submit'/>");
        b.append("		</form>");
        b.append("	</body>");
        b.append("</html>");
        
        resp.setContentLength(b.length());
        resp.getWriter().println(b.toString());        
    }
    
    
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
    	//Getting the paramaters that were filled in
        String firstname = req.getParameter("firstname");
        String lastname = req.getParameter("lastname");
        
        resp.setContentType("text/html");
        StringBuilder b = new StringBuilder("");
        b.append("<html>");
        b.append("	<head>");
        b.append("		<title>Gluewine framework</title>");
        b.append("	</head>");
        b.append("	<body>");
        b.append("		<h1> Welcome " + firstname + " " + lastname + "</h1>");
        b.append("	</body>");
        b.append("</html>");
        
        resp.setContentLength(b.length());        
        resp.getWriter().println(b.toString());        
    }
}
