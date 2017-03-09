package confucian.driver;

import org.testng.ISuite;
import org.testng.ISuiteListener;

import confucian.data.DB.HikariData;
import confucian.data.DriverConfigurations;
import confucian.data.xml.MappingParserRevisit;

/**
 * 测试套件配置
 */
public class SuiteConfiguration implements ISuiteListener {
    /**
     * 测试套件名称
     */
    private static String suiteName;

    public static String getSuiteName() {
        return suiteName;
    }

    /**
     * 结束
     *
     * @param suite 测试套件
     */
    @Override
    public void onFinish(ISuite suite) {
        HikariData.close();
        // if (Boolean.valueOf(MappingParserRevisit.getCalcValue(DriverConfigurations.DBConfig.DBFlag.name())))
        //     HikariData.getDataSoure().close();
    }

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
        if (Boolean.valueOf(MappingParserRevisit.getCalcValue(DriverConfigurations.DBConfig.DBFlag.name()))) {
            HikariData.init(MappingParserRevisit.getCalcValue(DriverConfigurations.DBConfig.jdbcURL.name()),
                    MappingParserRevisit.getCalcValue(DriverConfigurations.DBConfig.username.name()),
                    MappingParserRevisit.getCalcValue(DriverConfigurations.DBConfig.password.name()));
        }
    }
}
