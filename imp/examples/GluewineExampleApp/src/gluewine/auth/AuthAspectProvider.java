package gluewine.auth;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.gluewine.core.AspectProvider;
import org.gluewine.core.Glue;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;

import gluewine.rest.Login;
import gluewine.entities.Contact;

public class AuthAspectProvider implements AspectProvider{

	@Glue
    private HibernateSessionProvider provider;
	
	@Override
	@Transactional
	public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
	{
		if (o instanceof Login) {
			System.out.println("(Login)About to Invoke --" + m.getName());
			
			if (m.equals("doPost")) {
				
				Object obj = params[0];
				
				System.out.println("" + obj.toString());
				
				/*
				String username = req.getParameter("username");
		        String password = req.getParameter("password");
		        
		        List<gluewine.entities.User> users = provider.getSession().getAll(gluewine.entities.User.class);
		        	        
		        
			    if (users.isEmpty()) 
			    {
			    	System.out.println("The userlist is empty");
			    }
			    else {
				    for (gluewine.entities.User user : users)
				    {
				    	if (user.getUsername().equals(username) && user.getPassword().equals(password)) 
		            	{
						    if (user.getRole()) //true => user is admin		            	
						    	System.out.println("isadmin");		            	
						    else 
						    	System.out.println("noadmin");
		            	}
				    }
			    }*/
            	
			}
		}
		
	}
	
	@Override
	public void afterSuccess(Object o, Method m, Object[] params, Object result)
	{
		if (o instanceof Login)
			System.out.println("(Login)SUCCES! Method -- " + m.getName() + " returned -- " + result);
		
	}
	
	@Override
	public void afterFailure(Object o, Method m, Object[] params, Throwable e)
	{
		if (o instanceof Login)
			System.out.println("(Login)FAIL! Method -- " + m.getName() + " failed with -- " + e.getMessage());
		if (o instanceof Contact)
			System.out.println("(Contact)Method -- " + m.getName() + " failed with -- " + e.getMessage());
	}
	
	@Override
	public void after(Object o, Method m, Object[] params)
	{
		if (o instanceof Login)
			System.out.println("(Login)After Method -- " + m.getName());
	}
}
