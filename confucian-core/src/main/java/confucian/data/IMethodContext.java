package confucian.data;

import org.testng.IRetryAnalyzer;

import java.util.List;

import confucian.data.driverConfig.IBrowserConfig;

/**
 * 接口方法上下文
 */
public interface IMethodContext {

    /**
     * 获取浏览器配置
     *
     * @return 浏览器配置 browser conf
     */
    List<IBrowserConfig> getBrowserConf();

    /**
     * 获取数据源
     *
     * @return 数据源 data provider
     */
    DataSource getDataProvider();

    /**
     * 获取数据源文件
     *
     * @return the data provider path
     */
    String getDataProviderPath();

    /**
     * 获取方法测试数据
     *
     * @return 测试数据 method test data
     */
    List<IProperty> getMethodTestData();

    /**
     * 获取重试失败的测试
     *
     * @return 重试失败的测试 retry analyzer
     */
    IRetryAnalyzer getRetryAnalyzer();

    /**
     * 获取运行策略.
     *
     * @return 运行策略 run strategy
     */
    DataProvider.MapStrategy getRunStrategy();

    /**
     * 是否测试前运行
     *
     * @return boolean boolean
     */
    boolean isAfterMethod();

    /**
     * 是否测试后运行
     *
     * @return the boolean
     */
    boolean isBeforeMethod();

    /**
     * 为测试方法准备browserConf和IProperty
     */
    void prepareData();
}
