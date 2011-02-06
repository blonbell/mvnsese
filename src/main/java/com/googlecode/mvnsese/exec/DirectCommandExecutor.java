package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import java.lang.reflect.Method;
import java.util.Map;

public class DirectCommandExecutor extends ReflectiveCommandExecutor {

    public DirectCommandExecutor(Method m) {
        super(m);
    }

    public CommandResult execute(Selenium s, Map<String, Object> env, Command c) {
        CommandResult res = new CommandResult(c);
        try {
            evaluate(s, env, c);
        } catch (SeleniumException se) {
            res.fail(se.getMessage());
        }
        return res;
    }
}
