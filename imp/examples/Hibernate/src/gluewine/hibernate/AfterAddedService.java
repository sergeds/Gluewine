package gluewine.hibernate;

import gluewine.entities.Car;
import gluewine.entities.Color;

import java.io.Serializable;

import org.gluewine.persistence_jpa.QueryPostProcessor;

public class AfterAddedService implements QueryPostProcessor {

    //After an object is deleted, this function will be called on.
    @Override
    public void deleted(Serializable id, Object o)
    {
        //By using instanceof, we are able to give more detailed comments.
        if (o instanceof Car)        
            System.out.println("Car " + id + " has been deleted");        

        if (o instanceof Color)
            System.out.println("Color " + id + " has been deleted");
    }  

    //After an object is added, this function will be called on.
    @Override
    public void added(Serializable id, Object o)
    {
        if (o instanceof Car)
            System.out.println("Car " + id + " has been added");

        if (o instanceof Color)
            System.out.println("Color " + id + " has been added");
    }

    //After an object is added or updated, this function will be called on.
    @Override
    public void addedOrUpdated(Serializable id, Object o)
    {
        if (o instanceof Car)
            System.out.println("Car " + id + " has been added/updated");
      
        if (o instanceof Color)
            System.out.println("Color " + id + " has been added/updated");        
    }

    //After an object is updated, this function will be called on.
    @Override
    public void updated(Serializable id, Object o)
    {
        if (o instanceof Car)        
            System.out.println("Car " + id + " has been updated");
        
        if (o instanceof Color)        
            System.out.println("Color " + id + " has been updated");        
    }
    
    @Override
    public void persisted(Serializable id, Object o)
    {
    }
}
