package gluewine.hibernate;

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

import gluewine.entities.User;

public class UserService implements CommandProvider
{
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
    public void _user_search(CommandContext cc)
    {
        String text = cc.getOption("-text");

        Filter filter = new Filter();
        FilterLine filterline = new FilterLine();
        filterline.setFieldName("username");
        filterline.setOperator(FilterOperator.ICONTAINS);
        filterline.setValue(text);
        filter.setLimit(10);
        filter.addFilterLine(filterline);

        List <User> l = provider.getSession().getFiltered(User.class, filter);

        cc.tableHeader("Id", "Username", "Is admin");
        for (User user : l) {
    		cc.tableRow(Long.toString(user.getId()), user.getUsername(), Boolean.toString(user.getRole()));
    	}

        cc.printTable();
    }
    
    @Transactional
    public void _testing(CommandContext cc) {
    	cc.tableHeader("testing");
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
        
        CLICommand cmd_testing = new CLICommand("testing","testing description");
        l.add(cmd_testing);

        return l;
    }
}
