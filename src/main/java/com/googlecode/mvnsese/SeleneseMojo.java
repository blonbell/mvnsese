package com.googlecode.mvnsese;

import com.googlecode.mvnsese.exec.ExecContext;
import com.googlecode.mvnsese.exec.SuiteResult;
import com.googlecode.mvnsese.exec.SuiteRunner;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import static com.googlecode.mvnsese.exec.ExecContext.TraceLevel;

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
     * @parameter default-value="default"
     */
    private String webDriver;
    /**
     * @parameter default-value="${project.build.directory}/selenese-reports"
     */
    private File reportsDirectory;
    /**
     * Maximum time in miliseconds each command may run
     * @parameter default-value="30000"
     */
    private String timeout;
    /**
     * Maximum time in seconds each Test may run. 0 means no limit
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
    SeleneseSuiteGroup[] groups;
    /**
     * @parameter default-value="${project}"
     */
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing Selenese Suites");
        if (getLog().isDebugEnabled()) {
            Logger.getLogger("com.safeway.maven.selenese").setLevel(Level.FINEST);
        } else if (getLog().isInfoEnabled()) {
            Logger.getLogger("com.safeway.maven.selenese").setLevel(Level.INFO);
        }

        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            Thread.currentThread().setContextClassLoader(getTestClassPath(project));
            List<Future<SuiteResult>> results = new ArrayList<Future<SuiteResult>>();
            for (SeleneseSuiteGroup g : groups) {
                ExecContext ctx = new ExecContext();
                ctx.setWebDriver(g.getWebDriver() != null ? g.getWebDriver() : webDriver);
                ctx.setBaseURL(g.getBaseURL());
                if (g.getTimeout() != null && timeout.compareTo(g.getTimeout()) > 0) {
                    ctx.setTimeout(g.getTimeout());
                } else {
                    ctx.setTimeout(timeout);
                }
                ctx.setMaxTestTime(g.getMaxTestTime() > 0 ? g.getMaxTestTime() : maxTestTime);
                TraceLevel pLevel = TraceLevel.getLevel(g.getTraceHTML());
                TraceLevel gLevel = TraceLevel.getLevel(traceHTML);
                ctx.setTraceHTML(gLevel.ordinal() > pLevel.ordinal() ? gLevel : pLevel);
                for (File f : g.getSuites()) {
                    File reportDirectory = g.getReportsDirectory() != null ? g.getReportsDirectory() : reportsDirectory;

                    results.add(exec.submit(new SuiteRunner(f, new File(reportDirectory, getReportFileName(f.getName())), ctx)));
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
                        getLog().info(String.format("Tests run: %d, Failures %d Time elapsed: %s%s", r.getTotalTests(), r.getTestFailures(), r.getTime(), r.getTestFailures() == 0 ? "" : " <<< FAILURE!"));
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
            Thread.currentThread().setContextClassLoader(orig);
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

    protected ClassLoader getTestClassPath(MavenProject project) throws MojoExecutionException {
        try {
            ClassWorld cw = new ClassWorld();
            ClassRealm realm = cw.newRealm("maven.plugin." + getClass().getSimpleName(), Thread.currentThread().getContextClassLoader());
            List<String> elements = (List<String>) project.getTestClasspathElements();
            for (String element : elements) {
                File elementFile = new File(element);
                realm.addConstituent(elementFile.toURI().toURL());
            }
            return realm.getClassLoader();
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to set test classloader", e);
        }
    }
}
