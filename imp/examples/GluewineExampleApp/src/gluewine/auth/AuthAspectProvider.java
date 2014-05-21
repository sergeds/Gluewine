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

public class AuthAspectProvider implements AspectProvider {

	@Override
	public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
	{
		if (o instanceof Login)
			System.out.println("-- (Login)About to Invoke --" + m.getName());				
	}
	
	@Override
	public void afterSuccess(Object o, Method m, Object[] params, Object result)
	{
		if (o instanceof Login)
			System.out.println("-- (Login)SUCCES! Method -- " + m.getName() + " returned -- " + result);
	}
	
	@Override
	public void afterFailure(Object o, Method m, Object[] params, Throwable e)
	{
		if (o instanceof Login)
			System.out.println("-- (Login)FAIL! Method -- " + m.getName() + " failed with -- " + e.getMessage());
	}
	
	@Override
	public void after(Object o, Method m, Object[] params)
	{
		if (o instanceof Login)
			System.out.println("-- (Login)AFTER Method -- " + m.getName());
	}
}