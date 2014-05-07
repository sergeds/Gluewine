package gluewine.hibernate;

//import gluewine.entities.Car;

import org.gluewine.persistence_jpa.QueryPreProcessor;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import gluewine.entities.Contact;
import gluewine.entities.User;
import java.lang.reflect.*;

public class PreProcessor implements QueryPreProcessor
{

    @Override
    public Criteria preProcess(Criteria criteria, Class<?> clazz)
    {
    	System.out.print("testpreprocessor ");
        if (clazz.getName().equals(Contact.class.getName()))
        {
        	System.out.print("testpreprocessor_contact ");
        	Field [] testing =  clazz.getFields();
        	
        	for (int i = 0 ; i < testing.length ; i++) {
        		System.out.print(" " + testing[i]);
        	}
        }
        
        if (clazz.getName().equals(User.class.getName()))
        {
        	System.out.print("testpreprocessor_user ");
        	Field [] testing =  clazz.getFields();
        	
        	for (int i =0 ; i < testing.length ; i++) {
        		System.out.print(" " + testing[i]);
        	}
        }
    	
        return criteria;
    }
}
