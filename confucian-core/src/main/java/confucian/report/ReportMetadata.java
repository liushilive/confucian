package confucian.report;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 提供对生成报告时有用的静态信息的访问
 */
public final class ReportMetadata {
    private static final String PROPERTY_KEY_PREFIX = "confucian.report.";
    private static final String TITLE_KEY = PROPERTY_KEY_PREFIX + "title";
    private static final String DEFAULT_TITLE = "测试报告";
    private static final String COVERAGE_KEY = PROPERTY_KEY_PREFIX + "coverage-report";
    private static final String EXCEPTIONS_KEY = PROPERTY_KEY_PREFIX + "show-expected-exceptions";
    private static final String OUTPUT_KEY = PROPERTY_KEY_PREFIX + "escape-output";
    private static final String XML_DIALECT_KEY = PROPERTY_KEY_PREFIX + "xml-dialect";
    private static final String STYLESHEET_KEY = PROPERTY_KEY_PREFIX + "stylesheet";
    private static final String LOCALE_KEY = PROPERTY_KEY_PREFIX + "locale";
    private static final String VELOCITY_LOG_KEY = PROPERTY_KEY_PREFIX + "velocity-log";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm E");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    /**
     * The date/time at which this report is being generated.
     */
    private final LocalDateTime reportTime = LocalDateTime.now();

    /**
     * If the XML dialect has been set to "junit", we will render all skipped tests
     * as failed tests in the XML.  Otherwise we use TestNG's extended version of
     * the XML format that allows for "<skipped>" elements.
     */
    public boolean allowSkippedTestsInXML() {
        return !System.getProperty(XML_DIALECT_KEY, "testng").equalsIgnoreCase("junit");
    }

    /**
     * @return The URL (absolute or relative) of an HTML coverage report associated with the test
     * run.  Null if there is no coverage report.
     */
    public String getCoverageLink() {
        return System.getProperty(COVERAGE_KEY);
    }

    public String getJavaInfo() {
        return String.format("Java %s (%s)", System.getProperty("java.version"), System.getProperty("java.vendor"));
    }

    /**
     * @return The locale specified by the System properties, or the platform default locale if none
     * is specified.
     */
    public Locale getLocale() {
        if (System.getProperties().containsKey(LOCALE_KEY)) {
            String locale = System.getProperty(LOCALE_KEY);
            String[] components = locale.split("_", 3);
            switch (components.length) {
                case 1:
                    return new Locale(locale);
                case 2:
                    return new Locale(components[0], components[1]);
                case 3:
                    return new Locale(components[0], components[1], components[2]);
                default:
                    System.err.println("Invalid locale specified: " + locale);
            }
        }
        return Locale.getDefault();
    }

    public String getPlatform() {
        return String.format("%s %s (%s)", System.getProperty("os.name"), System.getProperty("os.version"),
                System.getProperty("os.arch"));
    }

    /**
     * @return A String representation of the report date.
     *
     * @see #getReportTime()
     */
    public String getReportDate() {
        return reportTime.format(DATE_FORMAT);
    }

    /**
     * @return A String representation of the report time.
     *
     * @see #getReportDate()
     */
    public String getReportTime() {
        return reportTime.format(TIME_FORMAT);
    }

    public String getReportTitle() {
        return System.getProperty(TITLE_KEY, DEFAULT_TITLE);
    }

    /**
     * If a custom CSS file has been specified, returns the path.  Otherwise
     * returns null.
     *
     * @return A {@link File} pointing to the stylesheet, or null if no stylesheet is specified.
     */
    public File getStylesheetPath() {
        String path = System.getProperty(STYLESHEET_KEY);
        return path == null ?
                null :
                new File(path);
    }

    /**
     * @return The user account used to run the tests and the host name of the test machine.
     *
     * @throws UnknownHostException If there is a problem accessing the machine's host name.
     */
    public String getUser() throws UnknownHostException {
        String user = System.getProperty("user.name");
        String host = InetAddress.getLocalHost().getHostName();
        return user + '@' + host;
    }

    /**
     * Returns true (the default) if log text should be escaped when displayed in a
     * report.  Turning off escaping allows you to do something link inserting
     * link tags into HTML reports, but it also means that other output could
     * accidentally corrupt the mark-up.
     *
     * @return True if reporter log output should be escaped when displayed in a report, false
     * otherwise.
     */
    public boolean shouldEscapeOutput() {
        return System.getProperty(OUTPUT_KEY, "true").equalsIgnoreCase("true");
    }

    /**
     * @return True if Velocity should generate a log file, false otherwise.
     */
    public boolean shouldGenerateVelocityLog() {
        return System.getProperty(VELOCITY_LOG_KEY, "false").equalsIgnoreCase("true");
    }

    /**
     * Returns false (the default) if stack traces should not be shown for
     * expected exceptions.
     *
     * @return True if stack traces should be shown even for expected exceptions, false otherwise.
     */
    public boolean shouldShowExpectedExceptions() {
        return System.getProperty(EXCEPTIONS_KEY, "false").equalsIgnoreCase("true");
    }
}
