package gluewine.hibernate;

//import gluewine.entities.Car;

import org.gluewine.persistence_jpa.QueryPreProcessor;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import gluewine.entities.Contact;

public class PreProcessor implements QueryPreProcessor
{

    @Override
    public Criteria preProcess(Criteria criteria, Class<?> clazz)
    {
        if (clazz.getName().equals(Contact.class.getName()))
        {
        	System.out.println("test");
        	
        	//([0]|\+32)\W*([0-9][0-9][0-9])\W*([0-9][0-9]{2})\W*([0-9]{3})?
        	
        	//criteria.add( Restrictions.like("PhoneNumber", "([0]|\\+32)\\W*([0-9][0-9][0-9])\\W*([0-9][0-9]{2})\\W*([0-9]{3})?"));
        }
    	
        return criteria;
    }
}
