package gluewine.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gluewine.core.Glue;
import org.gluewine.jetty.GluewineServlet;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;

import gluewine.entities.LoginSession;

public class Logout extends GluewineServlet {
	
	@Glue
    private HibernateSessionProvider provider;
	
	@Override
 	public String getContextPath() {
 		return "logout";
 	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
		List<LoginSession> sessions = provider.getSession().getAll(LoginSession.class);
		 
		for (LoginSession session : sessions) {
			if (session.getIsActive()) 
			{
				;
			}
		}
 	}
	
	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
 				
    }
}
