package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import java.lang.reflect.Method;
import java.util.Map;

public class StoreCommandExecutor extends ReflectiveCommandExecutor {

    public StoreCommandExecutor(Method m) {
        super(m);
    }

    public CommandResult execute(Selenium s, Map<String, Object> env, Command c) {
        CommandResult res = new CommandResult(c);
        try {
            Object value = execute(s, c);
            env.put(c.getTarget(), value);
        } catch (SeleniumException se) {
            res.fail(se.getMessage());
        }
        return res;
    }
}
