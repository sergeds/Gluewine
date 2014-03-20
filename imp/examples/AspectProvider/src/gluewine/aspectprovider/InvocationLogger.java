package gluewine.aspectprovider;

import java.lang.reflect.Method;

import org.gluewine.core.AspectProvider;
import org.gluewine.core.InterceptChainStartOnly;

@InterceptChainStartOnly
public class InvocationLogger implements AspectProvider
{
    @Override
    public void beforeInvocation(Object o, Method m, Object[] params) throws Throwable
    {
    }

    @Override
    public void afterSuccess(Object o, Method m, Object[] params, Object result)
    {
    }

    @Override
    public void afterFailure(Object o, Method m, Object[] params, Throwable e)
    {
    }

    @Override
    public void after(Object o, Method m, Object[] params)
    {
    }

}
