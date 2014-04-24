package gluewine.directoryAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;


public class DirectoryAnnotations implements CommandProvider
{
    @Glue(properties = "test.properties")
    private Properties props;

    public void _testDA(CommandContext cc)
    {
        cc.println("This is a test-command");
    }


    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<>();

        CLICommand cmd = new CLICommand("testDA", "Test command for gluewine with using directory annotations!");
        l.add(cmd);

        return l;
    }
}
