package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoreCommandExecutor extends ReflectiveCommandExecutor {

    public static Pattern var = Pattern.compile("\\$\\{(\\w*)\\}");

    public StoreCommandExecutor(Method m) {
        super(m);
    }

    public CommandResult execute(Selenium s, Map<String, Object> env, Command c) {
        CommandResult res = new CommandResult(c);
        try {
            Object value = evaluate(s, null, c);
            env.put(c.getTarget(), value);
        } catch (SeleniumException se) {
            res.fail(se.getMessage());
        }
        return res;
    }

    public static String substituteVariables(String value, Map<String, Object> env) {
        Matcher m = var.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String varName = m.group(1);
            Object varValue = env.get(varName);
            if (varValue instanceof String) {
                m.appendReplacement(sb, (String) varValue);
            } else {
                m.appendReplacement(sb, "");
            }
        }
        m.appendTail(sb);
        return sb.toString();

    }
}
