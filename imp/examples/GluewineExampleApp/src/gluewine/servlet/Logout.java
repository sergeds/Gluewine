package gluewine.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Calendar;

import java.text.*;

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
	
	@Transactional
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
 	{
		List<LoginSession> sessions = provider.getSession().getAll(LoginSession.class);
		
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm").format(Calendar.getInstance().getTime());
		
		LoginSession closeSession = new LoginSession();
		 
		for (LoginSession session : sessions) {
			if (session.getIsActive()) 
			{
				closeSession = session;
				
				closeSession.setIsActive(false);
				closeSession.setLogoutTime(timeStamp);
				
				provider.getSession().update(closeSession);
				provider.commitCurrentSession();
			}
		}
		
		resp.sendRedirect("http://localhost:8000/login/"); 
 	}
	
	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
 	 
    }
}
