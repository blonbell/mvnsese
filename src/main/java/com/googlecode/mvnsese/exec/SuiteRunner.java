package com.googlecode.mvnsese.exec;

import com.gargoylesoftware.htmlunit.WebClient;
import com.googlecode.mvnsese.model.Command;
import com.googlecode.mvnsese.model.SeleneseParser;
import com.googlecode.mvnsese.model.SeleneseSuite;
import com.googlecode.mvnsese.model.SeleneseTest;
import com.thoughtworks.selenium.Selenium;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.server.htmlrunner.HTMLTestResults;

public class SuiteRunner implements Callable<SuiteResult> {

    private static String VERSION = "";
    private static String REVISION = "";

    static {
        try {
            InputStream version = SuiteRunner.class.getResourceAsStream("/VERSION.txt");
            if (version != null) {
                Properties p = new Properties();
                p.load(version);
                VERSION = p.getProperty("selenium.core.version");
                REVISION = p.getProperty("selenium.core.revision");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static final String SUITE_SUMMARY =
            "<table id=\"suiteTable\" class=\"selenium\" border=\"1\" cellpadding=\"1\" cellspacing=\"1\"><tbody>\n"
            + "<tr class=\"title {0}\"><td><b>{1}</b></td></tr>{2}\n"
            + "</tbody></table>";
    private static final String SUITE_TEST_SUMMARY =
            "<tr class=\"{0}\"><td><a href=\"{1}\">{2}</a></td></tr>\n";
    private static final String TEST_RESULT =
            "<div>\n"
            + "<table border=\"1\" cellpadding=\"1\" cellspacing=\"1\">\n"
            + "<thead>\n"
            + "<tr class=\"title {0}\"><td rowspan=\"1\" colspan=\"3\">{1}</td></tr>\n"
            + "</thead><tbody>\n{2}"
            + "</tbody></table>\n"
            + "</div>\n";
    private static final String COMMAND_RESULT = "<tr class=\"{0}\" style=\"cursor: pointer;\">\n"
            + "<td>{1}</td>\n"
            + "<td>{2}</td>\n"
            + "<td>{3}</td>\n"
            + "</tr>\n";
    static Map<String, CommandExecutor> executorMap = buildExecutorMap();
    String baseURL;
    File suiteFile;
    File reportFile;

    public SuiteRunner(String baseURL, File suiteFile, File reportFile) {
        this.baseURL = baseURL;
        this.suiteFile = suiteFile;
        this.reportFile = reportFile;
    }

    public SuiteResult call() throws Exception {
        SeleneseSuite suite = (SeleneseSuite) SeleneseParser.parse(suiteFile);
        HtmlUnitDriver driver = new HtmlUnitDriver() {

            @Override
            protected WebClient modifyWebClient(WebClient client) {
                try {
                    client.setUseInsecureSSL(true);
                } catch (GeneralSecurityException ex) {
                    ex.printStackTrace();
                }
                return client;
            }
        };

        Logger.getLogger("com.gargoylesoftware.htmlunit.DefaultCssErrorHandler").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl").setLevel(Level.OFF);
        //Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware.htmlunit.javascript").setLevel(Level.ALL);
        Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);

        driver.setJavascriptEnabled(true);


        StringBuilder log = new StringBuilder();
        List<TestResult> testResults = new ArrayList<TestResult>();
        long startTime = System.currentTimeMillis();

        for (SeleneseTest test : suite.getTests()) {
            List<CommandResult> cmdResults = new ArrayList<CommandResult>();
            TestResult testResult = new TestResult(test, cmdResults);
            Selenium selenium = new WebDriverBackedSelenium(driver, baseURL != null ? baseURL : test.getBaseURL());
            log.append(String.format("info: Starting test %s\n", new File(suiteFile.getParentFile(), test.getFileName())));
            boolean testFailed = false;
            Map<String, Object> testCtx = new HashMap<String, Object>();
            for (Command c : test.getCommands()) {
                CommandExecutor executor = executorMap.get(c.getName());
                if (executor != null) {
                    log.append(String.format("info: Executing: |%s | %s | %s | \n", c.getName(), c.getTarget(), c.getValue()));
                    CommandResult result = executor.execute(selenium, testCtx, c);
                    cmdResults.add(result);
                    if (result.getResult() != Result.PASSED) {
                        log.append(String.format("error %s\n", result.getMsg()));
                        log.append(String.format("*****************\n%s\n*****************\n", selenium.getHtmlSource()));
                        if (!testFailed) {
                            log.append(String.format("warn: currentTest.recordFailure: false\n"));
                        }
                        testFailed = true;
                        if (result.getResult() == Result.ASSERT_FAILED) {
                            break;
                        }
                    }
                } else {
                    log.append(String.format("error: Unknown command: '%s'\n", c.getName()));
                    if (!testFailed) {
                        log.append(String.format("warn: currentTest.recordFailure: Unknown command: '%s'\n", c.getName()));
                    }
                    CommandResult result = new CommandResult(c);
                    result.setResult(Result.ERROR);
                    result.setMsg(String.format("Unknown command: '%s'\n", c.getName()));
                    cmdResults.add(result);
                    testFailed = true;
                }
            }
            testResults.add(testResult);
        }
        long duration = System.currentTimeMillis() - startTime;


        return generateReport(suite, testResults, log.toString(), duration, reportFile);


    }

    static SuiteResult generateReport(SeleneseSuite suite, List<TestResult> results, String log, long duration, File reportFile) throws Exception {
        int totalTests = 0;
        int testsPassed = 0;
        int testsFailed = 0;
        int commandsPassed = 0;
        int commandsFailed = 0;
        int commandsError = 0;
        String suiteResult = "status_passed";

        StringBuilder suiteIndex = new StringBuilder();
        List<String> testTables = new ArrayList<String>();
        for (int i = 0; i < results.size(); i++) {
            TestResult testResult = results.get(i);
            SeleneseTest test = testResult.getTest();

            totalTests++;
            Result testOutcome = Result.PASSED;
            String testStatus = "status_passed";

            StringBuilder cmdHtml = new StringBuilder();
            for (CommandResult cmdResult : testResult.getResults()) {
                String cmdStatus;
                Command cmd = cmdResult.getCommand();

                if (cmdResult.getResult() == Result.PASSED) {
                    commandsPassed++;
                    if (cmd.getName().startsWith("assert")) {
                        cmdStatus = "status_passed";
                    } else {
                        cmdStatus = "status_done";
                    }
                } else if (cmdResult.getResult() == Result.FAILED || cmdResult.getResult() == Result.ASSERT_FAILED) {
                    commandsFailed++;
                    cmdStatus = "status_failed";
                    testOutcome = Result.FAILED;
                } else {
                    commandsError++;
                    cmdStatus = "status_failed";
                    testOutcome = Result.FAILED;
                }
                cmdHtml.append(MessageFormat.format(COMMAND_RESULT, cmdStatus, cmd.getName(), cmd.getTarget(), cmdResult.getMsg() != null ? cmdResult.getMsg() : cmd.getValue()));
            }
            if (testOutcome == Result.PASSED) {
                testsPassed++;
            } else {
                testsFailed++;
                testStatus = "status_failed";
                suiteResult = "status_failed";
            }
            testTables.add(MessageFormat.format(TEST_RESULT, testStatus, test.getTitle(), cmdHtml));
            suiteIndex.append(MessageFormat.format(SUITE_TEST_SUMMARY, testStatus, test.getFileName(), test.getTitle()));
        }

        String suiteHtml = MessageFormat.format(SUITE_SUMMARY, suiteResult, suite.getTitle(), suiteIndex);
        String time = String.format("%.2f sec", duration / 1000.0d);

        HTMLTestResults htmlResult = new HTMLTestResults(VERSION, REVISION, suiteResult.substring(7), time, String.valueOf(totalTests), String.valueOf(testsPassed), String.valueOf(testsFailed), String.valueOf(commandsPassed), String.valueOf(commandsFailed), String.valueOf(commandsError), suiteHtml, testTables, log);
        reportFile.getParentFile().mkdirs();
        PrintWriter writer = new PrintWriter(reportFile);
        htmlResult.write(writer);
        writer.close();

        return new SuiteResult(suite, totalTests, testsFailed, time);


    }

    static Map<String, CommandExecutor> buildExecutorMap() {
        Map<String, CommandExecutor> executors = new HashMap<String, CommandExecutor>();
        for (Method m : Selenium.class.getMethods()) {
            String name = m.getName();
            if (name.startsWith("get")) {
                name = name.substring(3);
                executors.put("store" + name, new StoreCommandExecutor(m));
                executors.put("assert" + name, new AssertCommandExecutor(m));
                executors.put("verify" + name, new AssertCommandExecutor(m));
                executors.put("waitFor" + name, new WaitForCommandExecutor(m));
            }
            if (name.startsWith("is")) {
                name = name.substring(2);
                executors.put("store" + name, new StoreCommandExecutor(m));
                executors.put("verify" + name, new VerifyCommandExecutor(m));
                executors.put("assert" + name, new AssertCommandExecutor(m));
                executors.put("waitFor" + name, new WaitForCommandExecutor(m));
            }
            if (name.startsWith("click") || name.startsWith("doubleClick")) {
                executors.put(name, new DirectCommandExecutor(m));
                executors.put(name + "AndWait", new AndWaitCommandExecutor(m));
            } else {
                executors.put(name, new DirectCommandExecutor(m));
            }
        }
        executors.put("pause", new PauseCommandExecutor());

        return executors;

    }
}
