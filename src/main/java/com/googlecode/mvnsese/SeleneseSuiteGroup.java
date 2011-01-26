package com.googlecode.mvnsese;

import java.io.File;

public class SeleneseSuiteGroup {

    /**
     * @parameter
     */
    private String baseURL;

    /**
     * @parameter default-value="htmlunit"
     */
    private String webDriver;

    /**
     *
     * @parameter default-value="${project.build.directory}/selenese-reports"
     */
    private File reportsDirectory;
    /**
     * @parameter
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
}
