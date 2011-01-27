package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.googlecode.mvnsese.exec.SuiteRunner;
import com.thoughtworks.selenium.Selenium;
import java.util.Map;

public interface CommandExecutor {

    CommandResult execute(Selenium s, Map<String, Object> env, Command c);
}
