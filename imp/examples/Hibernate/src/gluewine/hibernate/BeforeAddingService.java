package gluewine.hibernate;

import org.gluewine.persistence_jpa.QueryPreProcessor;
import org.hibernate.Criteria;
//import org.hibernate.criterion.Restrictions;
//import gluewine.entities.Car;

public class BeforeAddingService implements QueryPreProcessor {

    @Override
    public Criteria preProcess(Criteria criteria, Class<?> clazz)
    {
        /*if (clazz.getName().equals(Car.class.getName()))
        {
            criteria.add(Restrictions.eq("merk", "Ford"));
        }*/
        return criteria;
    }
}