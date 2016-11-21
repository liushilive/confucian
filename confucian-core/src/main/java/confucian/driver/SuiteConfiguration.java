package confucian.driver;

import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * 测试套件配置
 */
public class SuiteConfiguration implements ISuiteListener {
    /**
     * 测试套件名称
     */
    public static String suiteName;

    /**
     * 开始
     *
     * @param suite 测试套件
     */
    @Override
    public void onStart(ISuite suite) {
        suiteName = suite.getName();
        //ReportNG property
        System.setProperty("confucian.report.coverage-report", "true");
        final String ESCAPE_PROPERTY = "confucian.report.escape-output";
        System.setProperty(ESCAPE_PROPERTY, "false");
    }

    /**
     * 结束
     *
     * @param suite 测试套件
     */
    @Override
    public void onFinish(ISuite suite) {
        // TODO Auto-generated method stub
    }
}
