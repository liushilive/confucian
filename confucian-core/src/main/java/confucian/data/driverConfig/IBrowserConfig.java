package confucian.data.driverConfig;

import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * 接口浏览器配置
 */
public interface IBrowserConfig {

    /**
     * 获取浏览器
     *
     * @return 浏览器
     */
    String getBrowser();

    /**
     * 获取浏览器功能
     *
     * @return 浏览器功能
     */
    DesiredCapabilities getCapabilities();

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    String getDataSource();

    /**
     * 获取驱动超时
     *
     * @return 驱动超时
     */
    Integer getDriverTimeOut();

    /**
     * 获取本地chrome驱动路径
     *
     * @return 本地chrome驱动路径
     */
    String getLocalChromeDriverPath();

    /**
     * 获取本地IE驱动路径
     *
     * @return 本地IE驱动路径
     */
    String getLocalIEDriverPath();

    /**
     * 获取远程URL
     *
     * @return 远程URL
     */
    String getRemoteURL();

    /**
     * 获取重试失败的测试用例次数
     *
     * @return 重试失败的测试用例次数
     */
    Integer getRetryFailedTestCaseCount();

    /**
     * 操作元素高亮标记
     *
     * @return boolean
     */
    boolean isHighLightElementFlag();

    /**
     * 标记是否远程
     *
     * @return boolean
     */
    boolean isRemoteFlag();

    /**
     * 屏幕截图标记
     *
     * @return boolean
     */
    boolean isScreenShotFlag();

    /**
     * 滚动到元素标记
     *
     * @return boolean
     */
    boolean isScrollElementFlag();

    /**
     * Selenium Grid地址
     *
     * @return Selenium Grid地址
     */
    String remoteURL();

    /**
     * 更新浏览器功能
     *
     * @param desiredCapabilities 浏览器功能
     */
    void updateCapabilities(DesiredCapabilities desiredCapabilities);
}
