package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import java.lang.reflect.Method;
import java.util.Map;

public class VerifyCommandExecutor extends ReflectiveCommandExecutor {

    public VerifyCommandExecutor(Method m) {
        super(m);
    }

    public CommandResult execute(Selenium s, Map<String, Object> env, Command c) {
        CommandResult res = new CommandResult(c);
        try {
            Boolean b = (Boolean) execute(s, c);
            if (!b) {
                res.setResult(Result.FAILED);
            }
        } catch (SeleniumException se) {
            res.fail(se.getMessage());
        }
        return res;
    }
}
