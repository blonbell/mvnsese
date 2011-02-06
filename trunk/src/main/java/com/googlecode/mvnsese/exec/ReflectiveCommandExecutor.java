package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import static com.googlecode.mvnsese.exec.StoreCommandExecutor.substituteVariables;

public abstract class ReflectiveCommandExecutor implements CommandExecutor {

    Method m;

    public ReflectiveCommandExecutor(Method m) {
        this.m = m;
    }

    Object evaluate(Selenium s, Map<String, Object> env, Command c) throws SeleniumException {
        Object returnVal = null;
        try {
            if (m.getParameterTypes().length == 1) {
                returnVal = m.invoke(s, substituteVariables(c.getTarget(),env));
            } else if (m.getParameterTypes().length == 2) {
                returnVal = m.invoke(s,  substituteVariables(c.getTarget(),env),  substituteVariables(c.getValue(),env));
            } else if (m.getParameterTypes().length == 0) {
                returnVal = m.invoke(s);
            }
        } catch (InvocationTargetException te) {
            if (te.getTargetException() instanceof SeleniumException) {
                throw (SeleniumException) te.getTargetException();
            } else if (te.getTargetException() instanceof Exception) {
                throw new SeleniumException((Exception) te.getTargetException());
            } else {
                throw new SeleniumException(te);
            }
        } catch (IllegalAccessException ie) {
            throw new SeleniumException(ie);
        }
        return returnVal;
    }
}
