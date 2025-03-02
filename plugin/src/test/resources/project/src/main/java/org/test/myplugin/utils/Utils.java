package org.test.myplugin.utils;

public abstract class Utils {

    public static final Utils INSTANCE = new UtilsImpl();

    public abstract void doSomething();

}

class UtilsImpl extends Utils {

    @Override
    public void doSomething() {
        System.out.println("something");
    }

}
