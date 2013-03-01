/**************************************************************************
 *
 * Gluewine DSCL Enhancer Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version
 * 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 **************************************************************************/
package org.gluewine.dscl;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.log4j.Logger;
import org.gluewine.core.ClassEnhancer;
import org.gluewine.core.glue.Interceptor;

/**
 * Enhances classes by creating dynamic subclasses.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DSCLEnhancer implements ClassEnhancer
{
    // ===========================================================================
    /**
     * The new line character.
     */
    private static final char NL = '\n';

    /**
     * The interceptor to use.
     */
    private Interceptor interceptor = null;

    /**
     * The logger to use.
     */
    private Logger logger = Logger.getLogger(getClass());

    /**
     * The indexer to use.
     */
    private JarClassIndexer indexer = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param interceptor The interceptor.
     * @throws Throwable If the indexer could not be initialized.
     */
    public DSCLEnhancer(Interceptor interceptor) throws Throwable
    {
        this.interceptor = interceptor;
        this.indexer = new JarClassIndexer();
    }

    // ===========================================================================
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getEnhanced(Class<T> c) throws Throwable
    {
        T t = null;
        Class<?> cl = getSubclass(c);
        cl.getDeclaredField("interceptor").set(null, interceptor);
        cl.getDeclaredField("methods").set(null, c.getMethods());
        t = (T) cl.newInstance();
        return t;
    }

    // ===========================================================================
    /**
     * Creates a dynamic subclass of the class specified.
     *
     * @param cl The class to process.
     * @return The subclass.
     * @throws Throwable If an error occurs.
     */
    private Class<?> getSubclass(Class<?> cl) throws Throwable
    {
        logger.debug("Creating an enhanced class for: " + cl.getName());
        StandardJavaFileManager fileManager = null;
        try
        {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            fileManager = compiler.getStandardFileManager(null, null, null);
            DSCLJavaFileManager manager = new DSCLJavaFileManager(fileManager, indexer);

            JavaSourceFromString[] cus = new JavaSourceFromString[1];
            String code = getSubclassCode(cl);

            ClassLoader loader = new DSCLClassLoader(manager, cl.getClassLoader());

            cus[0] = new JavaSourceFromString(cl.getName() + "$$Enhanced", code);

            if (compiler.getTask(null, manager, new DSCLDiagnosticListener(), null, null, Arrays.asList(cus)).call())
                return loader.loadClass(cl.getName() + "$$Enhanced");

            else
                throw new Throwable("Compile error for class " + cl.getName());

        }
        catch (Throwable e)
        {
            logger.error("--------------------------------------------------------------------------------------------------------------------------------------");
            logger.error("Compile job failed for class: " + cl.getName());
            logger.error("--------------------------------------------------------------------------------------------------------------------------------------");
            logger.error(e);
            logger.error("--------------------------------------------------------------------------------------------------------------------------------------");
            throw e;
        }
        finally
        {
            if (fileManager != null) fileManager.close();
        }
    }

    // ===========================================================================
    /**
     * Returns the subclass code for the given class.
     *
     * @param cl The class to process.
     * @return The subclass code.
     */
    private String getSubclassCode(Class<?> cl)
    {
        StringBuilder b = new StringBuilder();
        b.append("package ").append(cl.getPackage().getName()).append(";").append(NL);
        b.append("public class ").append(cl.getSimpleName()).append("Enhanced extends ").append(cl.getName()).append(NL);
        b.append("{").append(NL);
        b.append("public static ").append(Interceptor.class.getName()).append(" interceptor;").append(NL);
        b.append("public static java.lang.reflect.Method[] methods = null;").append(NL);
        b.append(getConstructors(cl));
        b.append(getMethods(cl));
        b.append("}").append(NL);
        return b.toString();
    }

    // ===========================================================================
    /**
     * Gets all methods for the class specified.
     *
     * @param cl The class to use.
     * @return The String.
     */
    private String getMethods(Class<?> cl)
    {
        StringBuilder b = new StringBuilder();
        Method[] methods = cl.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            if (!Modifier.isFinal(methods[i].getModifiers()) && !methods[i].isSynthetic())
                b.append(getMethod(methods[i], cl, i));
        }

        /*
        for (Method m : cl.getDeclaredMethods())
        {
            int mod = m.getModifiers();
            if (!Modifier.isFinal(mod))
            {
                if (Modifier.isProtected(mod))
                    b.append(getMethod(m));
            }
        }
        */

        return b.toString();
    }



    // ===========================================================================
    /**
     * Returns a String representing the type. If the type contains generics
     * they are expanded.
     *
     * @param t The type to process.
     * @return The String.
     */
    private String getParameterType(Type t)
    {
        boolean array = false;
        StringBuilder b = new StringBuilder();
        Class<?> cl = null;
        Class<?> realClass = null;
        if (t instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) t;
            cl = (Class<?>) pt.getRawType();
            array = cl.isArray();
            if (array)
                realClass = cl.getComponentType();
            else
                realClass = cl;
        }
        else if (t instanceof GenericArrayType)
        {
            GenericArrayType gt = (GenericArrayType) t;
            cl = (Class<?>) gt.getGenericComponentType();
            array = true;
            realClass = cl;
        }

        else
        {
            if (t instanceof WildcardType)
                cl = t.getClass();

            else
                cl = (Class<?>) t;

            if (cl.isArray())
            {
                realClass = cl.getComponentType();
                array = true;
            }
            else
                realClass = cl;
        }

        if (realClass.isMemberClass())
            b.append(realClass.getName().replace('$', '.'));
        else
            b.append(realClass.getName());

        if (t instanceof ParameterizedType)
        {
            b.append('<');
            if (t instanceof WildcardType)
                b.append("?");

            else
            {
                ParameterizedType pt = (ParameterizedType) t;
                Type[] parameterArgTypes = pt.getActualTypeArguments();
                for (int i = 0; i < parameterArgTypes.length; i++)
                {
                    if (parameterArgTypes[i] instanceof WildcardType)
                        b.append("?");
                    else
                        b.append(getParameterType(parameterArgTypes[i]));
                    if (i < parameterArgTypes.length - 1)
                        b.append(", ");
                }
            }

            b.append('>');
        }

        if (array)
            b.append("[]");

        return b.toString();
    }

    // ===========================================================================
    /**
     * Returns the subclass code for the given method.
     *
     * @param m The method to process.
     * @param cl The class to process.
     * @param mi The method index.
     * @return The string containing the code.
     */
    private String getMethod(Method m, Class<?> cl, int mi)
    {
        StringBuilder b = new StringBuilder();

        b.append(getModifierString(m.getModifiers()));
        boolean isVoid = m.getReturnType().getSimpleName().equals("void");

        String returnType = null;

        if (m.getReturnType().isArray())
            returnType = m.getReturnType().getComponentType().getName() + "[]";
        else
            returnType = m.getReturnType().getName();

        returnType = getParameterType(m.getGenericReturnType());

        b.append(returnType).append(" ");
        b.append(m.getName()).append("(");


        StringBuilder sup = new StringBuilder();

        if (Modifier.isStatic(m.getModifiers()))
            sup.append(cl.getName()).append(".");
        else
            sup.append("super.");

        sup.append(m.getName()).append("(");

        Type[] cc = m.getGenericParameterTypes();
        int numberArgs = cc.length;
        for (int i = 0; i < cc.length; i++)
        {
            b.append(getParameterType(cc[i]));

            b.append(" arg").append(i);
            sup.append("arg").append(i);
            if (i < cc.length - 1)
            {
                b.append(", ");
                sup.append(", ");
            }
        }
        sup.append(")");
        b.append(")");

        Class<?>[] exc = m.getExceptionTypes();
        if (exc.length > 0)
            b.append(" throws ");
        for (int i = 0; i < exc.length; i++)
        {
            b.append(exc[i].getName());
            if (i < exc.length - 1)
                b.append(", ");
        }
        b.append(NL);

        b.append("{").append(NL);

        b.append(getMethodBody(sup.toString(), isVoid, returnType, exc, numberArgs, mi, Modifier.isStatic(m.getModifiers()))).append(NL);
        b.append("}").append(NL);

        return b.toString();
    }

    // ===========================================================================
    /**
     * Returns the body 'code' of the method.
     *
     * @param superInvoke The String containing the 'super.xxxx()' call.
     * @param isVoid True if the method is void.
     * @param returnType The type of return if the method is not void.
     * @param excTypes The array of exception types.
     * @param numberArgs The number of arguments.
     * @param mi The index of the method.
     * @param isStatic True of the method is static.
     * @return The String representing the code of the method.
     */
    private String getMethodBody(String superInvoke, boolean isVoid, String returnType, Class<?>[] excTypes, int numberArgs, int mi, boolean isStatic)
    {
        String thisName = null;
        if (!isStatic) thisName = "this";

        StringBuilder b = new StringBuilder("boolean firstInChain = interceptor.registerFirstInChain();").append(NL);
        b.append("java.util.Stack<org.gluewine.core.AspectProvider> stack = new java.util.Stack<org.gluewine.core.AspectProvider>();").append(NL);
        b.append("Object[] args = new Object[] {");

        for (int i = 0; i < numberArgs; i++)
        {
            b.append("arg").append(i);
            if (i < numberArgs - 1)
                b.append(",");
        }

        b.append("};").append(NL);

        b.append("try").append(NL);
        b.append("{").append(NL);
        b.append("    interceptor.invokeBefore(stack, ").append(thisName).append(", methods[").append(mi).append("], args, firstInChain);").append(NL);
        if (!isVoid)
            b.append("    ").append(returnType).append(" result = ");

        b.append(superInvoke).append(";").append(NL);

        if (!isVoid)
            b.append("    interceptor.invokeAfterSuccess(stack, ").append(thisName).append(", methods[").append(mi).append("], args, result);").append(NL);
        else
            b.append("    interceptor.invokeAfterSuccess(stack, ").append(thisName).append(", methods[").append(mi).append("], args, null);").append(NL);

        if (!isVoid)
            b.append("    return result;").append(NL);

        b.append("}").append(NL);

        boolean runtimeThrown = false;
        for (Class<?> exc : excTypes)
        {
            b.append("catch (").append(exc.getName()).append(" e)").append(NL);
            b.append("{").append(NL);
            b.append("    e.printStackTrace();").append(NL);
            b.append("    interceptor.invokeAfterFailure(stack, ").append(thisName).append(", methods[").append(mi).append("], args, e);").append(NL);
            b.append("    throw e;").append(NL);
            b.append("}").append(NL);
            if (exc == RuntimeException.class || exc == Exception.class || exc == Throwable.class)
                runtimeThrown = true;
        }

        if (!runtimeThrown)
        {
            b.append("catch (RuntimeException e)").append(NL);
            b.append("{").append(NL);
            b.append("    e.printStackTrace();").append(NL);
            b.append("    interceptor.invokeAfterFailure(stack, ").append(thisName).append(", methods[").append(mi).append("], args, e);").append(NL);
            b.append("    throw e;").append(NL);
            b.append("}").append(NL);
        }
        b.append("finally").append(NL);
        b.append("{").append(NL);
        b.append("    interceptor.clearThread(firstInChain);").append(NL);
        b.append("}").append(NL);

        return b.toString();
    }

    // ===========================================================================
    /**
     * Returns a String representation of the modifiers specified.
     *
     * @param mod The modifiers to process.
     * @return The String representation.
     */
    private String getModifierString(int mod)
    {
        StringBuilder b = new StringBuilder();

        if (Modifier.isPublic(mod))
            b.append("public ");

        else if (Modifier.isProtected(mod))
            b.append("protocted ");

        else if (Modifier.isPrivate(mod))
            b.append("private ");

        if (Modifier.isAbstract(mod))
            b.append("abstract ");

        if (Modifier.isFinal(mod))
            b.append("final ");

        if (Modifier.isStatic(mod))
            b.append("static ");

        return b.toString();
    }

    // ===========================================================================
    /**
     * Gets all constructors for the class specified.
     *
     * @param cl The class to use.
     * @return The String
     */
    private String getConstructors(Class<?> cl)
    {
        StringBuilder b = new StringBuilder();
        for (Constructor<?> c : cl.getConstructors())
        {
            int mod = c.getModifiers();
            if (!Modifier.isPrivate(mod) && !Modifier.isFinal(mod))
            {

                b.append(getModifierString(mod));
                b.append(cl.getSimpleName()).append("Enhanced (");

                StringBuilder sup = new StringBuilder("super(");

                Class<?>[] cc = c.getParameterTypes();
                for (int i = 0; i < cc.length; i++)
                {
                    b.append(cc[i].getName()).append(" arg").append(i);
                    sup.append("arg").append(i);
                    if (i < cc.length - 1)
                    {
                        b.append(", ");
                        sup.append(", ");
                    }
                }
                sup.append(");");
                b.append(")");

                Class<?>[] exc = c.getExceptionTypes();
                if (exc.length > 0)
                    b.append(" throws ");
                for (int i = 0; i < exc.length; i++)
                {
                    b.append(exc[i].getName());
                    if (i < exc.length - 1)
                        b.append(", ");
                }
                b.append(NL);

                b.append("{").append(NL);
                b.append(sup.toString()).append(NL);
                b.append("}").append(NL);
            }
        }

        return b.toString();
    }
}
