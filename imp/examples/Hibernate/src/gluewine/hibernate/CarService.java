package gluewine.hibernate;

import gluewine.entities.Car;
import gluewine.entities.Color;

import java.util.ArrayList;
import java.util.List;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;
import org.gluewine.persistence.TransactionCallback;
import org.gluewine.persistence.Transactional;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class CarService implements CommandProvider {
	
    @Glue
    private HibernateSessionProvider provider;

    /*
     *The method car_list.
     * With this method we can print a list of all the cars in the database
     */
    @Transactional
    public void _car_list(CommandContext cc)
    {
        //We need to get all the cars that are in the database
        List<Car> cars = provider.getSession().getAll(Car.class);
        cc.tableHeader("Id", "Brand", "Model", "Color");

        for (Car car : cars)
        {
            cc.tableRow(Long.toString(car.getId()), car.getBrand(), car.getModel(), car.getColor().getName());
        }

        cc.printTable();
    }

    /*
     * The method car_add.
     * With this method we can add a car.
     */
    @Transactional
    public void _car_add(final CommandContext cc)
    {
        String newBrand = cc.getOption("-brand");
        String newModel = cc.getOption("-model");
        long newColor= Long.parseLong(cc.getOption("-color"));

        Car newCar = new Car();
        newCar.setBrand(newBrand);
        newCar.setModel(newModel);

        //Check if the chosen color is available
        Color color = (Color) provider.getSession().get(Color.class, newColor);

        if (color != null)
        {
            newCar.setColor(color);

            //Before we add the car, we check if everthing went the way it should have.
            provider.getSession(new TransactionCallback() {
                /*
                 * Something went wrong with the transaction, 
                 * the database was rolled back meaning the car wasn't added.
                 */
                @Override
                public void transactionRolledBack()
            {
                cc.println("Transaction was rolled back.");
            }

            @Override
            public void transactionCommitted()
            {
                cc.println("Car has been added successfully");
            }
            }).add(newCar);
            provider.commitCurrentSession();
        }
        else
            cc.println("Color does not exist.");
    }

    /*
     * The metohd car_search.
     * With this method we can search for a car on criteria 'brand' and 'model'.
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
        Criteria cr = provider.getSession().createCriteria(Car.class);
        cr.add(Restrictions.or(Restrictions.ilike("brand", text), Restrictions.ilike("model", text)));
        List<Car> carList = cr.list();

        cc.tableHeader("Id", "Brand", "Model", "Color");

        for (Car car : carList)
        {
            cc.tableRow(Long.toString(car.getId()), car.getBrand(), car.getModel(), car.getColor().getName());
        }

        cc.printTable();
    }

    /*
     * The method car_delete.
     * With this method we can delete a car from the database.
     */
    @Transactional
    public void _car_delete(CommandContext cc)
    {
        long id = Long.parseLong(cc.getOption("-id"));
        Car car = (Car) provider.getSession().get(Car.class, id);
        if (car != null) {
            provider.getSession().delete(car);
            provider.commitCurrentSession();
        }
        else
            cc.println("There is no car with id " + id);
    }

    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<>();

        //adding a car
        CLICommand cmd_car_add = new CLICommand("car_add", "Adds a car");
        cmd_car_add.addOption("-brand", "Brand of the car", true, true);
        cmd_car_add.addOption("-model", "Model of the car", true, true);
        cmd_car_add.addOption("-color", "Color", true, true);
        l.add(cmd_car_add);

        //delete a car
        CLICommand cmd_car_delete = new CLICommand("car_delete", "Deletes a car");
        cmd_car_delete.addOption("-id", "The id of the car you want to delete", true, true);
        l.add(cmd_car_delete);

        //list all the cars in the db
        CLICommand cmd_car_list = new CLICommand("car_list", "Lists the cars");
        l.add(cmd_car_list);

        //search a car
        CLICommand cmd_car_search = new CLICommand("car_search", "Searches a car on criteria 'brand' and 'model'");
        cmd_car_search.addOption("-text", "%criteria%", true, true);
        l.add(cmd_car_search);

        return l;
    }

}
