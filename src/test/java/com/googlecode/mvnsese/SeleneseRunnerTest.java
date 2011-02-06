package com.googlecode.mvnsese;

import com.googlecode.mvnsese.exec.DefaultWebDriverProfileFactory;
import com.googlecode.mvnsese.exec.ExecContext;
import com.googlecode.mvnsese.exec.StoreCommandExecutor;
import com.googlecode.mvnsese.exec.SuiteResult;
import com.googlecode.mvnsese.exec.SuiteRunner;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class SeleneseRunnerTest {

    public SeleneseRunnerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void substituteVars() throws Exception {
        Map<String, Object> env = new HashMap<String, Object>();
        env.put("foo", "bar");
        env.put("bar", "foo");
        String eval = "Test ${foo} test ${bar} Test";
        assertEquals("Test bar test foo Test", StoreCommandExecutor.substituteVariables(eval, env));
    }

    @Test
    public void runSuite() throws Exception {
        File suite = new File("target/test-classes/googleSuite.html");
        File report = new File("target/selenese-report/googleSuite.html");
        ExecContext ctx = new ExecContext();
        ctx.setWebDriver(DefaultWebDriverProfileFactory.PROFILE);
        SuiteRunner runner = new SuiteRunner(suite, report, ctx);
        SuiteResult result = runner.call();
        assertEquals(1, result.getTotalTests());
        assertEquals(0, result.getTestFailures());

    }
}
