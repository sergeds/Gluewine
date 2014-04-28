package Gluewine.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.core.Glue;
import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence.Transactional;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import Gluewine.entities.User;

public class UserService {
	@Glue
    private HibernateSessionProvider provider;


    /*
     *The method user_list.
     * With this method we can print a list of all the users in the database
     */
    @Transactional
    public void _user_list(CommandContext cc)
    {
    	//We need to get all the users that are in the database
    	List<User> users = provider.getSession().getAll(User.class);
    	cc.tableHeader("Id", "Username", "Is admin");
    	
    	for (User user : users) {
    		cc.tableRow(Long.toString(user.getId()), user.getUsername(), Boolean.toString(user.getRole()));
    	}

        cc.printTable();
    }

    
    /*
     * The metohd user_search.
     * With this method we can search for a user on criteria 'username'.
     */
    @Transactional
    public void _car_search(CommandContext cc)
    {
        String text = cc.getOption("-text");

        /*
         * First we need to get a list of all the cars in the database.
         * We will use this list to search cars.
         * With criteria we are able to put restrictions on the list off cars.
         */
        /*
        Criteria cr = provider.getSession().createCriteria(Car.class);
        cr.add(Restrictions.or(Restrictions.ilike("brand", text), Restrictions.ilike("model", text)));
        List<Car> carList = cr.list();

        cc.tableHeader("Id", "Brand", "Model", "Color");

        for (Car car : carList)
        {
            cc.tableRow(Long.toString(car.getId()), car.getBrand(), car.getModel(), car.getColor().getName());
        }

        cc.printTable();*/
    }

    

    /*
     * (non-Javadoc)
     * @see org.gluewine.console.CommandProvider#getCommands()
     */
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<>();

        //list all the users in the db
        CLICommand cmd_user_list = new CLICommand("user_list", "Lists the users");
        l.add(cmd_user_list);

        //search a car
        CLICommand cmd_user_search = new CLICommand("user_search", "Searches a user on criteria 'username'");
        cmd_user_search.addOption("-text", "%criteria%", true, true);
        l.add(cmd_user_search);

        return l;
    }
}
