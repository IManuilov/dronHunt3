package ilovke.compil;

import ilovke.compil.MainRun.My;

public class MyClass implements My {
    public String sayHello(String str) {
        System.out.println("Hello from dynamically compiled class! " + str);
        return "" + Math.random();
    }
}