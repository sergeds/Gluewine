package gluewine.aspectprovider;

import java.lang.reflect.Method;

import org.gluewine.core.AspectProvider;

public class MyAspectProvider implements AspectProvider
{

    @Override
    public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
    {
        if (o instanceof TestCommand)
            System.out.println("--- About to invoke " + m.getName());
    }

    @Override
    public void afterSuccess(Object o, Method m, Object[] params, Object result)
    {
    	if (o instanceof TestCommand)
    		System.out.println("--- Method " + m.getName() + " WAS SUCCESFULL AND RETURNED " + result);
    }

    @Override
    public void afterFailure(Object o, Method m, Object[] params, Throwable e)
    {
    	if (o instanceof TestCommand)
    		System.out.println("--- Method " + m.getName() + " FAILED with " + e.getMessage());
    }

    @Override
    public void after(Object o, Method m, Object[] params)
    {
    	if (o instanceof TestCommand)
    		System.out.println("--- After Method " + m.getName());
    }

}
