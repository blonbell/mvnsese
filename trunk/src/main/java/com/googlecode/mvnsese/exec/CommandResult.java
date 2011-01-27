package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;

public class CommandResult {

    private Result result = Result.PASSED;
    private String msg;
    private Command command;

    CommandResult(Command command) {
        this.command = command;
    }

    public CommandResult fail(String msg) {
        this.result = Result.FAILED;
        this.msg = msg;
        return this;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Command getCommand() {
        return command;
    }
}
