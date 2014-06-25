package gluewine.hibernate;

import org.gluewine.persistence_jpa.QueryPreProcessor;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import gluewine.entities.Contact;
import gluewine.entities.User;
import java.lang.reflect.*;

public class PreProcessor implements QueryPreProcessor {

    @Override
    public Criteria preProcess(Criteria criteria, Class<?> clazz)
    {
        if (clazz.getName().equals(Contact.class.getName()))
        	System.out.println("testpreprocessor_contact ");
        
        if (clazz.getName().equals(User.class.getName()))
        	System.out.println("testpreprocessor_user ");
        
        return criteria;
    }
}