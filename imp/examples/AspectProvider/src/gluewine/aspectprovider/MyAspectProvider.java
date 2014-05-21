package gluewine.aspectprovider;

import java.lang.reflect.Method;

import org.gluewine.core.AspectProvider;

public class MyAspectProvider implements AspectProvider
{

	//This method will run itself before each method that is executed
    @Override
    public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
    {
    	//It will only print when a method of the class TestCommand is called on
        if (o instanceof TestCommand)
            System.out.println("--- About to invoke " + m.getName());
    }

    //This method will run itself after a method is executed successfully
    @Override
    public void afterSuccess(Object o, Method m, Object[] params, Object result)
    {
        if (o instanceof TestCommand)
            System.out.println("--- Method " + m.getName() + " WAS SUCCESFULL AND RETURNED " + result);
    }

    //This method will run itself after a method failed to execute
    @Override
    public void afterFailure(Object o, Method m, Object[] params, Throwable e)
    {
        if (o instanceof TestCommand)
            System.out.println("--- Method " + m.getName() + " FAILED with " + e.getMessage());
    }

    //This method will run itself after a method is executed, 
    //no matter whether the method failed or succeeded
    @Override
    public void after(Object o, Method m, Object[] params)
    {
        if (o instanceof TestCommand)
            System.out.println("--- After Method " + m.getName());
    }

}
