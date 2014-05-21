package gluewine.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;

public class Commands implements CommandProvider {
	
    @Glue(properties = "test.properties")
    private Properties props;

    public void _test(CommandContext cc)
    {
        /*
         * If we add the option -t on the test-command, then the command will execute the content of this if-structure.
         * When there are multiple options for one command, it's best to use a switch-structure.
         */
        if (cc.hasOption("-t"))
        {
            cc.tableHeader("Name", "Firstname");

            int i = 1;
            String name = props.getProperty("name." + i);
            while (name != null)
            {
                String firstname = props.getProperty("firstname." + i);
                cc.tableRow(name, firstname);

                i++;
                name = props.getProperty("name." + i);
            }

            cc.printTable();
        }
        if (cc.hasOption("-a")) {
            cc.println("This is a required test-command");
        }
    }

    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<>();

        CLICommand cmd = new CLICommand("test", "Test command for gluewine!");
        l.add(cmd);

        //This option isn't required and doesn't need a value.
        cmd.addOption("-t", "Prints the table: name", false, false);

        //This option is required, so the user will have to add this option to the command.
        //however it doesn't need a value, just adding -a is enough for this command
        cmd.addOption("-a", "Nothing yet", true, false);

        return l;
    }
}