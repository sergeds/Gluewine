package gluewine.hibernate;

import gluewine.entities.Color;

import java.util.ArrayList;
import java.util.List;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.persistence_jpa.Filter;
import org.gluewine.persistence_jpa.FilterLine;
import org.gluewine.persistence_jpa.FilterOperator;
import org.gluewine.persistence_jpa_hibernate.HibernateSessionProvider;
import org.gluewine.persistence.Transactional;

public class ColorService implements CommandProvider
{
    @Glue
    private HibernateSessionProvider provider;

    /*
     *The method color_list.
     * With this method we can print a list of all the colors in the database
     */
    @Transactional
    public void _color_list(CommandContext cc)
    {
        //We need to get all the colors that are in the database
        List<Color> colors = provider.getSession().getAll(Color.class);
        cc.tableHeader("Id", "Name", "RGB");

        for (Color color : colors)
        {
            cc.tableRow(Long.toString(color.getId()), color.getName(), Integer.toString(color.getRgb()));
        }

        cc.printTable();
    }

    /*
     * The method color_add.
     * With this method we can add a color.
     */
    @Transactional
    public void _color_add(final CommandContext cc)
    {
        String name = cc.getOption("-name");
        int rgb = Integer.parseInt(cc.getOption("-rgb"));

        Color newColor = new Color();
        newColor.setName(name);
        newColor.setRgb(rgb);

        provider.getSession().add(newColor);
        provider.commitCurrentSession();
    }

    /*
     * The method color_search.
     * With this method we can search for a color on the criteria 'name'.
     */
    @Transactional
    public void _color_search(CommandContext cc)
    {
        String text = cc.getOption("-text");

        Filter filter = new Filter();
        FilterLine filterline = new FilterLine();
        filterline.setFieldName("name");
        filterline.setOperator(FilterOperator.ICONTAINS);
        filterline.setValue(text);
        filter.setLimit(10);
        filter.addFilterLine(filterline);

        List <Color> l = provider.getSession().getFiltered(Color.class, filter);

        cc.tableHeader("Id", "Name", "RGB");

        for (Color color: l)
        {
            cc.tableRow(Long.toString(color.getId()), color.getName(), Integer.toString(color.getRgb()));
        }

        cc.printTable();
    }

    /*
     * The method color_delete.
     * With this method we can delete a color from the database.
     */
    @Transactional
    public void _color_delete(CommandContext cc)
    {
        long id = Long.parseLong(cc.getOption("-id"));
        Color color = (Color) provider.getSession().get(Color.class, id);

        if (color != null) {
            provider.getSession().delete(color);
            provider.commitCurrentSession();
        }
        else
            cc.println("There is no color with id " + id);
    }

    /*
     * The method color_change.
     * With this method we can update a color.
     * The user is obligated to insert all the values/new values
     */
    @Transactional
    public void _color_change(CommandContext cc) {
        long id = Long.parseLong(cc.getOption("-id"));
        String name = cc.getOption("-name");
        int rgb = Integer.parseInt(cc.getOption("-rgb"));


        Color color = (Color) provider.getSession().get(Color.class, id);

        if (color != null) {
            color.setName(name);
            color.setRgb(rgb);
            provider.getSession().update(color);
            provider.commitCurrentSession();
        }
        else
            cc.println("There is no color with id " + id);
    }

    /*
     * (non-Javadoc)
     * @see org.gluewine.console.CommandProvider#getCommands()
     */
    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<>();

        //adding a color
        CLICommand cmd_add_color = new CLICommand("color_add", "Adds a color");
        cmd_add_color.addOption("-name", "Name of the color", true, true);
        cmd_add_color.addOption("-rgb", "RGB-values of the color", true, true);
        l.add(cmd_add_color);

        //delete a color
        CLICommand cmd_color_delete = new CLICommand("color_delete", "Deletes a color");
        cmd_color_delete.addOption("-id", "The id of the color you want to delete", true, true);
        l.add(cmd_color_delete);

        //list all the colors in the db
        CLICommand cmd_color_list = new CLICommand("color_list", "Lists the colors");
        l.add(cmd_color_list);

        //search a color
        CLICommand cmd_color_search = new CLICommand("color_search", "Searches a color on criteria 'name'");
        cmd_color_search.addOption("-text", "%criteria%", true, true);
        l.add(cmd_color_search);

        //change a color
        CLICommand cmd_color_change = new CLICommand("color_change", "Changes/updates a color");
        cmd_color_change.addOption("-id", "The id of the color you want to change", true, true);
        cmd_color_change.addOption("-name", "The new name", true, true);
        cmd_color_change.addOption("-rgb", "The new rgb-value", true, true);
        l.add(cmd_color_change);

        return l;
    }
}
