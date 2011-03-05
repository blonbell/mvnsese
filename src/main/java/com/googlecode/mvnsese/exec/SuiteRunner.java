package com.googlecode.mvnsese.exec;

import com.googlecode.mvnsese.model.Command;
import com.googlecode.mvnsese.model.SeleneseParser;
import com.googlecode.mvnsese.model.SeleneseSuite;
import com.googlecode.mvnsese.model.SeleneseTest;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.server.htmlrunner.HTMLTestResults;
import static com.googlecode.mvnsese.exec.ExecContext.TraceLevel;

public class SuiteRunner implements Callable<SuiteResult> {

    private static Logger clog = Logger.getLogger("com.safeway.maven.selenese.exec.console");
    private static String VERSION = "";
    private static String REVISION = "";
    private static AtomicBoolean consoleInit = new AtomicBoolean(false);

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
    static Map<String, WebDriverProfileFactory> profileMap = buildProfileMap();
    ExecContext execCtx;
    File suiteFile;
    File reportFile;

    public SuiteRunner(File suiteFile, File reportFile, ExecContext execCtx) {
        this.execCtx = execCtx;
        this.suiteFile = suiteFile;
        this.reportFile = reportFile;
    }

    public SuiteResult call() throws Exception {
        SeleneseSuite suite = (SeleneseSuite) SeleneseParser.parse(suiteFile);

        WebDriverProfileFactory factory = profileMap.get(execCtx.getWebDriver());
        if (factory == null) {
            throw new SeleniumException(String.format("Unknown WebDriver profile %s", execCtx.getWebDriver()));
        }
        WebDriver driver = factory.buildWebDriver();


        StringBuilder log = new StringBuilder();
        List<TestResult> testResults = new ArrayList<TestResult>();
        long maxTestTime = execCtx.getMaxTestTime() > 0 ? execCtx.getMaxTestTime() * 1000 : Long.MAX_VALUE;
        long startTime = System.currentTimeMillis();
        if (clog.isLoggable(Level.INFO) && consoleInit.compareAndSet(false, true) && System.console() != null) {
            System.console().printf("Executing test %65s", "");
        }
        for (SeleneseTest test : suite.getTests()) {
            List<CommandResult> cmdResults = new ArrayList<CommandResult>();
            TestResult testResult = new TestResult(test, cmdResults);
            Selenium selenium = new WebDriverBackedSelenium(driver, execCtx.getBaseURL() != null ? execCtx.getBaseURL() : test.getBaseURL());
            log.append(String.format("info: Starting test %s\n", new File(suiteFile.getParentFile(), test.getFileName())));
            log.append(String.format("info: Base URL %s\n", execCtx.getBaseURL()));
            if (clog.isLoggable(Level.INFO) && System.console() != null) {
                printTest(suite, test);
            }
            Map<String, Object> testCtx = new HashMap<String, Object>();
            testCtx.put(CommandExecutor.TIMEOUT, execCtx.getTimeout());
            selenium.setTimeout(execCtx.getTimeout());
            for (Command c : test.getCommands()) {
                CommandExecutor executor = executorMap.get(c.getName());
                if (executor != null) {
                    log.append(String.format("info: Executing: |%s | %s | %s | \n", c.getName(), c.getTarget(), c.getValue()));
                    CommandResult result = executor.execute(selenium, testCtx, c);
                    cmdResults.add(result);
                    if (execCtx.getTraceHTML() == TraceLevel.ALL) {
                        log.append(String.format("*****************\n%s\n%s\n*****************\n", selenium.getLocation(), selenium.getHtmlSource()));
                    }

                    if (result.getResult() != Result.PASSED) {
                        log.append(String.format("error %s\n", result.getMsg()));
                        if (execCtx.getTraceHTML() == TraceLevel.ERROR) {
                            log.append(String.format("*****************\n%s\n%s\n*****************\n", selenium.getLocation(), selenium.getHtmlSource()));
                        }

                        if (testResult.getResult() != Result.PASSED) {
                            log.append(String.format("warn: currentTest.recordFailure: false\n"));
                        }
                        testResult.setResult(Result.FAILED);
                        if (result.getResult() == Result.ASSERT_FAILED) {
                            break;
                        }
                    }
                } else {
                    log.append(String.format("error: Unknown command: '%s'\n", c.getName()));
                    if (testResult.getResult() != Result.PASSED) {
                        log.append(String.format("warn: currentTest.recordFailure: Unknown command: '%s'\n", c.getName()));
                    }
                    CommandResult result = new CommandResult(c);
                    result.setResult(Result.ERROR);
                    result.setMsg(String.format("Unknown command: '%s'\n", c.getName()));
                    cmdResults.add(result);
                    testResult.setResult(Result.FAILED);
                }
            }
            testResults.add(testResult);
            if (System.currentTimeMillis() - startTime > maxTestTime) {
                log.append(String.format("error: max test time %d exceeded\n", execCtx.getMaxTestTime()));
                testResult.setResult(Result.FAILED);
                break;
            }
        }
        if (clog.isLoggable(Level.INFO) && System.console() != null) {
            System.console().printf("\n\n");
        }

        long duration = System.currentTimeMillis() - startTime;


        return generateReport(suite, testResults, log.toString(), duration, reportFile);


    }

    void printTest(SeleneseSuite suite, SeleneseTest test) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 65; i++) {
            sb.append("\b");
        }
        //sb.append("Executing test ");
        int index = 14;
        String title = suite.getTitle();
        for (int i = 0; i < title.length() && i < 30; i++, index++) {
            sb.append(title.charAt(i));
        }
        sb.append(" => ");
        index+=4;
        title = test.getTitle();
        for (int i = 0; i < title.length() && index < 80; i++, index++) {
            sb.append(title.charAt(i));
        }
        for (int i = 1; i < 80 - index; i++) {
            sb.append(" ");
        }
        System.console().printf(sb.toString());
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
                } else {
                    commandsError++;
                    cmdStatus = "status_failed";
                }
                cmdHtml.append(MessageFormat.format(COMMAND_RESULT, cmdStatus, cmd.getName(), cmd.getTarget(), cmdResult.getMsg() != null ? cmdResult.getMsg() : cmd.getValue()));
            }
            if (testResult.getResult() == Result.PASSED) {
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
        DefaultCommandFactory defFactory = new DefaultCommandFactory();
        defFactory.register(executors);
        ServiceLoader<CommandFactory> cmdImpls = ServiceLoader.load(CommandFactory.class);
        for (CommandFactory factory : cmdImpls) {
            factory.register(executors);
        }
        return executors;

    }

    static Map<String, WebDriverProfileFactory> buildProfileMap() {
        Map<String, WebDriverProfileFactory> profiles = new HashMap<String, WebDriverProfileFactory>();
        ServiceLoader<WebDriverProfileFactory> profileImpls = ServiceLoader.load(WebDriverProfileFactory.class);
        for (WebDriverProfileFactory factory : profileImpls) {
            profiles.put(factory.profileName(), factory);
        }
        return profiles;
    }
}
