package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.thoughtworks.selenium.Selenium;
import java.lang.reflect.Method;
import java.util.Map;

public class AssertCommandExecutor extends VerifyCommandExecutor {

    public AssertCommandExecutor(Method m) {
        super(m);
    }

    @Override
    public CommandResult execute(Selenium s, Map<String, Object> env, Command c) {
        CommandResult res = super.execute(s, env, c);
        if (res.getResult() == Result.FAILED) {
            res.setResult(Result.ASSERT_FAILED);
        }
        return res;
    }
}
