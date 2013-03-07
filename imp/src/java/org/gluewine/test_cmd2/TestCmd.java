package org.gluewine.test_cmd2;

import java.util.ArrayList;
import java.util.List;

import org.gluewine.console.CLICommand;
import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.test.Tester;

public class TestCmd implements CommandProvider
{
    @Glue
    private Tester tester = null;

    @Override
    public List<CLICommand> getCommands()
    {
        List<CLICommand> l = new ArrayList<CLICommand>();
        l.add(new CLICommand("cmd2", "Returns the version of tester"));
        return l;
    }

    public void _cmd2(CommandContext cc) throws Throwable
    {
        cc.println("ClassLoader: " + getClass().getClassLoader().toString());
        cc.println("ClassLoader: " + getClass().getSuperclass().getClassLoader().toString());
        cc.println(tester.toString());
        cc.println(tester.getVersion());
    }
}
