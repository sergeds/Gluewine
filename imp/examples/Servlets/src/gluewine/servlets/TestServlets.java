package gluewine.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.jetty.GluewineServlet;

public class TestServlets extends GluewineServlet {

    @Override
    public String getContextPath() {
        return "TestServlet";
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html");
        resp.getWriter().write("<html>"
                + "<body>"
                + "<p> testing a servlet </p>"
                + "</body>"
                + "</html>");
    }
}
