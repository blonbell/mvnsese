package com.googlecode.mvnsese;

import java.util.List;

public class SeleneseSuite extends Selenese{
    
    private List<SeleneseTest> tests;

    public List<SeleneseTest> getTests() {
        return tests;
    }

    public void setTests(List<SeleneseTest> tests) {
        this.tests = tests;
    }

}
