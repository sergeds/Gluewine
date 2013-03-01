package org.gluewine.test;

public class Tester
{
    public String getVersion()
    {
        return  getClass().getSuperclass().getProtectionDomain().getCodeSource().getLocation().getFile();
    }
}
