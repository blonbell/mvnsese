package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.SeleneseSuite;

public class SuiteResult {

    private SeleneseSuite suite;
    private int totalTests;
    private int testFailures;
    private String time;

    public SuiteResult(SeleneseSuite suite, int totalTests, int testFailures, String time) {
        this.suite = suite;
        this.totalTests = totalTests;
        this.testFailures = testFailures;
        this.time = time;
    }

    public SeleneseSuite getSuite() {
        return suite;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public int getTestFailures() {
        return testFailures;
    }

    public String getTime() {
        return time;
    }
}
