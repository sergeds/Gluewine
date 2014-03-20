package gluewine.commands;

import java.util.ArrayList;
import java.util.List;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;

public class Commands2 implements CommandProvider
{
    @Glue
    private Commands test;

    public void _test2(CommandContext cc)
    {
        test._test(cc);
    }

    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<>();

        CLICommand cmd = new CLICommand("test2", "Test command2 for Gluewine!");
        l.add(cmd);
        cmd.addOption("-t", "Prints the table name", false, false);

        return l;

    }
    
   
}
