package org.gluewine.test_cmd1;

import java.util.HashMap;
import java.util.Map;

import org.gluewine.console.CommandContext;
import org.gluewine.console.CommandProvider;
import org.gluewine.core.Glue;
import org.gluewine.test.Tester;

public class TestCmd implements CommandProvider
{
    @Glue
    private Tester tester = null;

    @Override
    public Map<String, String> getCommandsSyntax()
    {
        Map<String, String> m = new HashMap<String, String>();
        m.put("cmd1", "Returns the version of tester");
        return m;
    }

    public void _cmd1(CommandContext cc) throws Throwable
    {
        cc.println("ClassLoader: " + getClass().getClassLoader().toString());
        cc.println("ClassLoader: " + getClass().getSuperclass().getClassLoader().toString());
        cc.println(tester.toString());
        cc.println(tester.getVersion());
    }
}
