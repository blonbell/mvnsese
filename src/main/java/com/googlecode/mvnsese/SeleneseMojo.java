package com.googlecode.mvnsese;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import static com.googlecode.mvnsese.SuiteRunner.SuiteResult;

/**
 *
 * @requiresProject true
 * @requiresDependencyResolution test
 * @goal integration-test
 * @phase integration-test
 * @threadSafe
 * @noinspection JavaDoc
 */
public class SeleneseMojo extends AbstractMojo {

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
     * @required
     */
    SeleneseSuiteGroup[] groups;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing Selenese Suites");
        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            List<Future<SuiteResult>> results = new ArrayList<Future<SuiteResult>>();
            for (SeleneseSuiteGroup g : groups) {
                for (File f : g.getSuites()) {
                    File reportDirectory = g.getReportsDirectory() != null ? g.getReportsDirectory() : reportsDirectory;
                    results.add(exec.submit(new SuiteRunner(g.getBaseURL(), f, new File(reportDirectory, getReportFileName(f.getName())))));
                }
            }
            if (results.size() != 0) {
                getLog().info("-------------------------------------------------------");
                getLog().info("S E L E N E S E  T E S T S");
                getLog().info("-------------------------------------------------------");
                int totalTests = 0;
                int totalFailures = 0;
                for (Future<SuiteResult> f : results) {
                    try {
                        SuiteResult r = f.get();
                        getLog().info(String.format("Executed suite %s : %s", r.getSuite().getFileName(), r.getSuite().getTitle()));
                        getLog().info(String.format("Tests run: %d, Failures %d Time elapsed: %s", r.getTotalTests(), r.getTestFailures(), r.getTime()));
                        totalTests += r.getTotalTests();
                        totalFailures += r.getTestFailures();
                    } catch (Exception ex) {
                        throw new MojoExecutionException("Suite execution error", ex.getCause());
                    }
                }
                getLog().info("Results :\n");
                getLog().info(String.format("Tests run: %d, Failures %d ", totalTests, totalFailures));
                if (totalFailures > 0) {
                    throw new MojoFailureException("Failed Selenese Test Suites");
                }
            }
        } finally {
            exec.shutdown();
            try {
                exec.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected String getReportFileName(String filename) {
        int index = filename.lastIndexOf(".");
        if (index > -1) {
            return filename.substring(0, index) + "-REPORT" + filename.substring(index);
        } else {
            return filename + "-REPORT";
        }
    }
}
