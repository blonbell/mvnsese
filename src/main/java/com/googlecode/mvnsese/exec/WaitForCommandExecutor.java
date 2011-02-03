package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import java.lang.reflect.Method;
import java.util.Map;

public class WaitForCommandExecutor extends ReflectiveCommandExecutor {

    public WaitForCommandExecutor(Method m) {
        super(m);
    }

    public CommandResult execute(Selenium s, Map<String, Object> env, Command c) {
        CommandResult res = new CommandResult(c);
        if (c.getValue() == null) {
            return res.fail("no value specified");
        }
        long start = System.currentTimeMillis();
        long timeout = Long.parseLong((String)env.get(TIMEOUT));
        while (System.currentTimeMillis() - start <= timeout) {
            try {
                if (c.getValue().equals(execute(s, c))) {
                    return res;
                }
            } catch (SeleniumException se) {
                //if (!(se.getCause() instanceof ScriptException)) {
                return res.fail(se.getMessage());
                //}
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                return res.fail(e.getMessage());
            }
        }
        return res.fail("timed out");
    }
}
