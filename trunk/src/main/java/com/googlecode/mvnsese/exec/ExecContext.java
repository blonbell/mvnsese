package com.googlecode.mvnsese.exec;

public class ExecContext {

    private String baseURL;
    private String webDriver;
    private String timeout ="30000";
    private long maxTestTime;
    private TraceLevel traceHTML;

    public static enum TraceLevel {

        NONE, ERROR, ALL;

        public static TraceLevel getLevel(String level) {
            if ("ALL".equalsIgnoreCase(level)) {
                return ALL;
            }
            if ("ERROR".equalsIgnoreCase(level)) {
                return ERROR;
            }
            return NONE;
        }
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getWebDriver() {
        return webDriver;
    }

    public void setWebDriver(String webDriver) {
        this.webDriver = webDriver;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public long getMaxTestTime() {
        return maxTestTime;
    }

    public void setMaxTestTime(long maxTestTime) {
        this.maxTestTime = maxTestTime;
    }

    public TraceLevel getTraceHTML() {
        return traceHTML;
    }

    public void setTraceHTML(TraceLevel traceHTML) {
        this.traceHTML = traceHTML;
    }
}
