	
package gluewine.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.jetty.GluewineServlet;

public class TestServlets2 extends GluewineServlet {

	/*
	 * (non-Javadoc)
	 * @see org.gluewine.jetty.GluewineServlet#getContextPath()
	 * 
	 * To use this method in the browser, we will need to address the following link:
	 * 
	 * http://localhost:portnumber/TestServlet
	 * 
	 * We define this link in the method getContextPath()
	 */
		@Override
		public String getContextPath() {
			return "TestServlet2";
		}
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
		{
			final long now = new Date().getTime();
	        resp.setDateHeader("Date", now);
	        resp.setDateHeader("Expires", now);

	        resp.setContentType("text/html");
	        StringBuilder b = new StringBuilder("<HTML><HEAD>");
	        b.append("<TITLE>Gluewine framework</TITLE>");
	        b.append("</HEAD>");
	        //
	        b.append("<H1>Welcome to the Gluewine framework.</H1>");
	        b.append("<p>Please fill in your credentials in the form below: </p>");
	        b.append("<form action='TestServlet2' method='POST'>");
	        b.append("<label for='firstname'>FirstName:</label>");
	        b.append("<input type='text' name='firstname'/>");
	        b.append("</br><label for='lastname'>LastName:</label>");
	        b.append("<input type='text' name='lastname'/>");
	        b.append("</br><input type='submit' value='submit' name='submit'/>");
	        b.append("</form>");

	        b.append("</HEAD>");
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
			String firstname = req.getParameter("firstname");
			String lastname = req.getParameter("lastname");
			resp.setContentType("text/html");
			 StringBuilder b = new StringBuilder("<HTML><HEAD>");
		        b.append("<TITLE>Gluewine framework</TITLE>");
		        b.append("</HEAD>");
		        b.append("Welcome " + firstname + " " + lastname);
		        b.append("</HEAD>");
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
}
