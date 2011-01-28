package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.thoughtworks.selenium.Selenium;
import java.util.Map;

public class NotCommandExecutor implements CommandExecutor {

    CommandExecutor exec;
    boolean isAssert;

    public NotCommandExecutor(CommandExecutor exec, boolean isAssert) {
        this.exec = exec;
        this.isAssert =isAssert;
    }

    public CommandResult execute(Selenium s, Map<String, Object> env, Command c) {
        CommandResult res = exec.execute(s, env, c);
        if (res.getResult() == Result.FAILED || res.getResult() == Result.ASSERT_FAILED){
            res.setResult(Result.PASSED);
            res.setMsg(null);
        }else  if (res.getResult() == Result.PASSED){
            if (isAssert){
                res.setResult(Result.ASSERT_FAILED);
            }else{
                res.setResult(Result.FAILED);
            }
        }
       
        return res;
    }
}
