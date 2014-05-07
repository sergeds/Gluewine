package gluewine.auth;

import java.lang.reflect.Method;

import org.gluewine.core.AspectProvider;

import gluewine.rest.Login;
import gluewine.entities.Contact;

public class AuthAspectProvider implements AspectProvider{

	@Override
	public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
	{
		if (o instanceof Login)
			System.out.println("(Login)About to Invoke --" + m.getName());
		if (o instanceof Contact)
			System.out.println("(Contact)About to Invoke --" + m.getName());
	}
	
	@Override
	public void afterSuccess(Object o, Method m, Object[] params, Object result)
	{
		if (o instanceof Login)
			System.out.println("(Login)Method -- " + m.getName() + " returned -- " + result);
		if (o instanceof Contact)
			System.out.println("(Contact)Method -- " + m.getName() + " returned -- " + result);
	}
	
	@Override
	public void afterFailure(Object o, Method m, Object[] params, Throwable e)
	{
		if (o instanceof Login)
			System.out.println("(Login)Method -- " + m.getName() + " failed with -- " + e.getMessage());
		if (o instanceof Contact)
			System.out.println("(Contact)Method -- " + m.getName() + " failed with -- " + e.getMessage());
	}
	
	@Override
	public void after(Object o, Method m, Object[] params)
	{
		if (o instanceof Login)
			System.out.println("(Login)After Method -- " + m.getName());
		if (o instanceof Contact)
			System.out.println("(Contact)After Method -- " + m.getName());
	}
}
