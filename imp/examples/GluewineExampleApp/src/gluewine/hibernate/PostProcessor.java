package gluewine.hibernate;

import gluewine.entities.Contact;

import java.io.Serializable;

import org.gluewine.persistence_jpa.QueryPostProcessor;

public class PostProcessor implements QueryPostProcessor
{
    //After an object is deleted, this function will be called on.
    @Override
    public void deleted(Serializable id, Object o)
    {
        //By using instanceof, we are able to give more detailed comments.
        if (o instanceof Contact)
        {
            System.out.println("Contact " + id + " has been deleted");
        }
    }

    @Override
    public void persisted(Serializable id, Object o)
    {
    }

    //After an object is added, this function will be called on.
    @Override
    public void added(Serializable id, Object o)
    {
        if (o instanceof Contact)
        {
            System.out.println("Contact" + id + " has been added");
        }
    }

    //After an object is added or updated, this function will be called on.
    @Override
    public void addedOrUpdated(Serializable id, Object o)
    {
        if (o instanceof Contact)
        {
            System.out.println("Contact" + id + " has been added/updated");
        }
    }

    //After an object is updated, this function will be called on.
    @Override
    public void updated(Serializable id, Object o)
    {
        if (o instanceof Contact)
        {
            System.out.println("Contact " + id + " has been updated");
        }
    }
}
