package com.googlecode.mvnsese.exec;

import com.thoughtworks.selenium.Selenium;
import java.lang.reflect.Method;
import java.util.Map;

public class DefaultCommandFactory implements CommandFactory {

    public void register(Map<String, CommandExecutor> registry) {
        for (Method m : Selenium.class.getMethods()) {
            String name = m.getName();
            if (name.startsWith("get")) {
                name = name.substring(3);
                registry.put("store" + name, new StoreCommandExecutor(m));
                registry.put("assert" + name, new AssertCommandExecutor(m));
                registry.put("assertNot" + name, new NotCommandExecutor(new AssertCommandExecutor(m), true));
                registry.put("verify" + name, new AssertCommandExecutor(m));
                registry.put("verifyNot" + name, new NotCommandExecutor(new AssertCommandExecutor(m), false));
                registry.put("waitFor" + name, new WaitForCommandExecutor(m));
                registry.put("waitForNot" + name, new NotCommandExecutor(new WaitForCommandExecutor(m), false));
            }
            if (name.startsWith("is")) {
                name = name.substring(2);
                registry.put("store" + name, new StoreCommandExecutor(m));
                registry.put("assert" + name, new AssertCommandExecutor(m));
                registry.put("assertNot" + name, new NotCommandExecutor(new AssertCommandExecutor(m), true));
                registry.put("verify" + name, new AssertCommandExecutor(m));
                registry.put("verifyNot" + name, new NotCommandExecutor(new AssertCommandExecutor(m), false));
                registry.put("waitFor" + name, new WaitForCommandExecutor(m));
                registry.put("waitForNot" + name, new NotCommandExecutor(new WaitForCommandExecutor(m), false));
            }
            if (name.startsWith("click") || name.startsWith("doubleClick")) {
                registry.put(name, new DirectCommandExecutor(m));
                registry.put(name + "AndWait", new AndWaitCommandExecutor(m));
            } else {
                registry.put(name, new DirectCommandExecutor(m));
            }
        }
        registry.put("pause", new PauseCommandExecutor());

    }
}
