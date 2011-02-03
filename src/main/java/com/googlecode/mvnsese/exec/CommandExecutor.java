package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.thoughtworks.selenium.Selenium;
import java.util.Map;

public interface CommandExecutor {

    public static final String TIMEOUT = "TIMEOUT";

    CommandResult execute(Selenium s, Map<String, Object> env, Command c);
}
