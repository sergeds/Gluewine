import java.util.ArrayList;
import java.util.List;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa.Filter;
import org.gluewine.persistence_jpa.FilterLine;
import org.gluewine.persistence_jpa.FilterOperator;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import gluewine.entities.Contact;

public class ContactService implements CommandProvider
{
	@Glue
    private HibernateSessionProvider provider;


    /*
     *The method contact_list.
     * With this method we can print a list of all the contacts in the database
     */
    @Transactional
    public void _Contact_list(CommandContext cc)
    {
        //We need to get all the contacts that are in the database
        List<Contact> contacts = provider.getSession().getAll(Contact.class);
        cc.tableHeader("Id", "FirstName", "LastName", "Phone", "Email");

        for (Contact contact : contacts)
        {
            cc.tableRow(Long.toString(contact.getId()), contact.getFirstname() , contact.getLastname(), contact.getPhoneNumber(), contact.getEmail();
        }

        cc.printTable();
    }

    /*
     * The method contact_add.
     * With this method we can add a contact.
     */
    @Transactional
    public void _contact_add(final CommandContext cc)
    {
        String newFirstName = cc.getOption("-firstname");
        String newLastName = cc.getOption("-lastname");
        String newPhone= cc.getOption("-phone");
        String newEmail= cc.getOption("-email");

        Contact newContact = new Contact();
        newContact.setFirstname(newFirstName);
        newCar.setLastname(newLastName);
        newCar.setPhoneNumber(newPhone);
        newCar.setEmail(newEmail);

        provider.getSession().add(newCar);
        provider.commitCurrentSession();
        

    /*
     * The metohd contact_search.
     * With this method we can search for a contact on criteria 'firstName' and 'lastName'.
     */
    @Transactional
    public void _contact_search(CommandContext cc)
    {
        String text = cc.getOption("-text");

        /*
         * First we need to get a list of all the contacts in the database.
         * We will use this list to search cars.
         * With criteria we are able to put restrictions on the list off contacts.
         */
        Criteria cr = provider.getSession().createCriteria(Contact.class);
        cr.add(Restrictions.or(Restrictions.ilike("firstname", text), Restrictions.ilike("lastname", text)));
        List<Contact> contactList = cr.list();

        cc.tableHeader("Id", "firstName", "lastName", "phoneNumber", "Email");

        for (Contact contact : contactList)
        {
            cc.tableRow(Long.toString(contact.getId()), contact.getFirstname(), contact.getLastname(), contact.getPhoneNumber(), contact.getEmail();
        }

        cc.printTable();
    }

    /*
     * The method contact_delete.
     * With this method we can delete a contact from the database.
     */
    @Transactional
    public void _contact_delete(CommandContext cc)
    {
        long id = Long.parseLong(cc.getOption("-id"));
        Contact contact = (Contact) provider.getSession().get(Contact.class, id);
        if (contact != null) {
            provider.getSession().delete(contact);
            provider.commitCurrentSession();
        }
        else
            cc.println("There is no contact with id " + id);
    }

    /*
     * (non-Javadoc)
     * @see org.gluewine.console.CommandProvider#getCommands()
     */
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<>();

        //adding a car
        CLICommand cmd_contact_add = new CLICommand("contact_add", "Adds a contact");
        cmd_contact_add.addOption("-firstName", "first name of the contact", true, true);
        cmd_contact_add.addOption("-lastName", "last name of the contact", true, true);
        cmd_contact_add.addOption("-phoneNumber", "phonenumber of the contact", true, true);
        cmd_contact_add.addOption("-email", "email of the contact", true, true);
        l.add(cmd_contact_add);

        //delete a car
        CLICommand cmd_contact_delete = new CLICommand("contact_delete", "Deletes a contact");
        cmd_contact_delete.addOption("-id", "The id of the contact you want to delete", true, true);
        l.add(cmd_contact_delete);

        //list all the cars in the db
        CLICommand cmd_contact_list = new CLICommand("contact_list", "Lists the contacts");
        l.add(cmd_contact_list);

        //search a car
        CLICommand cmd_contact_search = new CLICommand("contact_search", "Searches a contact on criteria 'firstName' and 'lastName'");
        cmd_contact_search.addOption("-text", "%criteria%", true, true);
        l.add(cmd_contact_search);

        return l;
    }
}
