package gluewine.auth;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.gluewine.core.AspectProvider;
import org.gluewine.core.Glue;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;

import gluewine.servlet.Login;
import gluewine.entities.User;


public class AuthAspectProvider implements AspectProvider{

	@Glue
    private HibernateSessionProvider provider;
		
	/*
	 * When the users logs in, we check in the beforeInvocation-method if he is an admin or a simple user.
	 * Based on this, we redirect him to either the homepage, or the adminpannel.
	 */
	@Override
	@Transactional
	public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
	{
		//We only need to check the user when the login-servlet is called
		if (o instanceof Login) {
			System.out.println("(Login)About to Invoke --" + m.getName());
			
			/*
			 * Because all the methodes that are called on in the Login-class will be invoked,
			 * we need to check if the method doPost is called on, 
			 * because this is where the user logs on.
			 */
			if (m.getName().equals("doPost")) {
				
				/*Object is an array with all the parameters of the method that is invoked
				 * In this case, the parameters are: HttpServletRequest and HttpServletResponse
				 */
				Object objRequest = params[0];
				Object objResponse = params[1];
				
				HttpServletRequest req = (HttpServletRequest) objRequest;
				HttpServletResponse resp = (HttpServletResponse) objResponse;
				
				String username = req.getParameter("username");
		        String password = req.getParameter("password");
		        
		        //CONTROL MOET NOG WEG!
		        System.out.println("username: " + username + "  pasword: " + password);
		        
		        List<gluewine.entities.User> users = provider.getSession().getAll(gluewine.entities.User.class);
		        
		        //We check if the userlist is empty, just in case something went worng in the database
			    if (users.isEmpty()) 
			    {
			    	System.out.println("The userlist is empty");
			    }
			    else {
				    for (gluewine.entities.User user : users)
				    {				    	
				    	if (user.getUsername().equals(username) && user.getPassword().equals(password)) 
		            	{
				    		//System.out.println("userrole: " + user.getRole());
				    		
						    if (user.getRole()) { //true => user is admin		            	
						    	resp.sendRedirect("http://localhost:8000/adminpanel/");
						    }
						    else {
						    	resp.sendRedirect("http://localhost:8000/contacts/");
						    }
		            	}//end if - check username & passwd
				    }// end for
			    }// end else - userlist not empty       	
			}//end if - method doPost
		}//end if - instanceof Login		
	}//end method beforeInvocation
	
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
	}
	
	@Override
	public void after(Object o, Method m, Object[] params)
	{
		if (o instanceof Login)
			System.out.println("(Login)After Method -- " + m.getName());
	}
}
