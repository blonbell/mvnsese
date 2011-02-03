package com.googlecode.mvnsese;

import java.io.File;

public class SeleneseSuiteGroup {

    /**
     * @parameter
     */
    private String baseURL;
    /**
     * @parameter default-value="default"
     */
    private String webDriver;
    /**
     *
     * @parameter default-value="${project.build.directory}/selenese-reports"
     */
    private File reportsDirectory;
    /**
     * @parameter default-value="30000"
     */
    private String timeout;
    /**
     * @parameter default-value="0"
     */
    private long maxTestTime;
    /**
     * @parameter default-value="none"
     */
    private String traceHTML;
    /**
     * @parameter
     * @required
     */
    private File[] suites;

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

    public File getReportsDirectory() {
        return reportsDirectory;
    }

    public void setReportsDirectory(File reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }

    public File[] getSuites() {
        return suites;
    }

    public void setSuites(File[] suites) {
        this.suites = suites;
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

    public String getTraceHTML() {
        return traceHTML;
    }

    public void setTraceHTML(String traceHTML) {
        this.traceHTML = traceHTML;
    }
}
