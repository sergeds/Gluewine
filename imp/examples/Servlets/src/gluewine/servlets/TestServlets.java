package gluewine.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.jetty.GluewineServlet;

public class TestServlets extends GluewineServlet {

	/*
     * To use this method in the browser, we will need to address the following link:
     * http://localhost:portnumber/TestServlet
     *
     * We define this link in the method getContextPath()
     */
    @Override
    public String getContextPath() 
    {
        return "TestServlet";
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html");
        resp.getWriter().write(""
        		+ "<html>"
        		+ "		<head>"
        		+ "			<title> Testing a servlet </title>"
        		+ "		</head>"
                + "		<body>"
                + "			<p> testing a servlet </p>"
                + "		</body>"
                + "</html>");
    }
}
