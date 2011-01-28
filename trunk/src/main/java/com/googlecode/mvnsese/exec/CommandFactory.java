package com.googlecode.mvnsese.exec;

import java.util.Map;


public interface CommandFactory {
    void register (Map<String,CommandExecutor> registry);
}
