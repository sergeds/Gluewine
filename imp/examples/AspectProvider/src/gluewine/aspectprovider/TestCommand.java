package gluewine.aspectprovider;

import java.util.ArrayList;
import java.util.List;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;

public class TestCommand implements CommandProvider
{

    public void _test(CommandContext cc)
    {    	
    	
        cc.println("This is a test command");        
    }

    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<>();

        CLICommand cmd = new CLICommand("test", "Test command for Gluewine!");
        l.add(cmd);

        return l;
    }
}
