package confucian.data.driverConfig;

import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

import confucian.data.DriverConfigurations;
import confucian.driver.Driver;

/**
 * 浏览器配置
 */
public class BrowserConfig implements IBrowserConfig {

    private DesiredCapabilities desiredCapabilities;
    private Map<String, String> mappedValues;

    /**
     * 实例化一个新的浏览器配置
     *
     * @param completeBrowserMap 完整的浏览器映射
     */
    BrowserConfig(Map<String, String> completeBrowserMap) {
        this(completeBrowserMap, new DesiredCapabilities());
    }

    /**
     * 实例化一个新的浏览器配置
     *
     * @param completeBrowserMap  完整的浏览器映射
     * @param desiredCapabilities 浏览器期望功能
     */
    BrowserConfig(Map<String, String> completeBrowserMap, DesiredCapabilities desiredCapabilities) {
        mappedValues = completeBrowserMap;
        this.desiredCapabilities = desiredCapabilities;
    }

    /**
     * 如果 isRemoteFlag = false ,比较 BrowserName；
     * 如果为true，那么需要功能与浏览器名称相同。
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BrowserConfig secondObj = (BrowserConfig) obj;
        if (this.isRemoteFlag() == secondObj.isRemoteFlag()) {
            if (this.isRemoteFlag()) {
                return this.getBrowser().equals(secondObj.getBrowser()) &&
                        this.getCapabilities().equals(secondObj.getCapabilities());
            } else {
                return this.getBrowser().equals(secondObj.getBrowser());
            }
        }
        return false;
    }

    public String getBrowser() {
        return mappedValues.get(DriverConfigurations.LocalEnvironmentConfig.browserName.toString());
    }

    @Override
    public DesiredCapabilities getCapabilities() {
        return desiredCapabilities;
    }

    public String getDataSource() {
        return mappedValues.get(DriverConfigurations.FrameworkConfig.datasource.toString());
    }

    public Integer getDriverTimeOut() {
        return Integer.valueOf(mappedValues.get(DriverConfigurations.FrameworkConfig.driverTimeOut.toString()));
    }

    public String getLocalChromeDriverPath() {
        return mappedValues.get(DriverConfigurations.LocalEnvironmentConfig.chromeDriverPath.toString());
    }

    public String getLocalIEDriverPath() {
        return mappedValues.get(DriverConfigurations.LocalEnvironmentConfig.IEDriverPath.toString());
    }

    public String getRemoteURL() {
        return mappedValues.get(DriverConfigurations.HubConfig.remoteURL.toString());
    }

    public Integer getRetryFailedTestCaseCount() {
        return Integer.valueOf(mappedValues.get(DriverConfigurations.FrameworkConfig.retryFailedTestCase.toString()));
    }

    @Override
    public int hashCode() {
        int hash = 7;

        if (this.isRemoteFlag()) {

            hash = 31 * hash + this.getBrowser().hashCode();
            hash = 31 * hash + this.getCapabilities().hashCode();
        } else {
            hash = 31 * hash + this.getBrowser().hashCode();
        }
        return hash;
    }

    public boolean isHighLightElementFlag() {
        boolean isHtmlUnit = Driver.getBrowserConfig().getBrowser().equalsIgnoreCase(BrowserType.HTMLUNIT);
        return isHtmlUnit ? false : Boolean.valueOf(mappedValues.get(DriverConfigurations.FrameworkConfig.highlightElementFlag.toString()));
    }

    public boolean isRemoteFlag() {
        return Boolean.valueOf(mappedValues.get(DriverConfigurations.FrameworkConfig.remoteFlag.toString()));
    }

    public boolean isScreenShotFlag() {
        boolean isHtmlUnit = Driver.getBrowserConfig().getBrowser().equalsIgnoreCase(BrowserType.HTMLUNIT);
        return isHtmlUnit ? false : Boolean.valueOf(mappedValues.get(DriverConfigurations.FrameworkConfig.screenShotFlag.toString()));
    }

    /**
     * 滚动到元素标记
     *
     * @return boolean
     */
    @Override
    public boolean isScrollElementFlag() {
        boolean isHtmlUnit = Driver.getBrowserConfig().getBrowser().equalsIgnoreCase(BrowserType.HTMLUNIT);
        return isHtmlUnit ? false : Boolean.valueOf(mappedValues.
                get(DriverConfigurations.FrameworkConfig.scrollElementFlag.toString()));
    }

    @Override
    public String remoteURL() {
        return mappedValues.get(DriverConfigurations.HubConfig.remoteURL.toString());
    }

    /**
     * 执行字符串表示为Html报告
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("浏览器:" + "<span style='font-weight:normal'>").append(getBrowser()).append("</span>");
        sb.append(", 功能:" + "<span style='font-weight:normal'>").append(this.getCapabilities().toString())
                .append("</span>");
        if (isRemoteFlag()) {
            sb.append(",是否远程:" + "<span style='font-weight:normal'>").append(isRemoteFlag()).append("</span>");
            sb.append(",远程URL:" + "<span style='font-weight:normal'>").append(getRemoteURL()).append("</span>");
        }
        return sb.toString();
    }

    @Override
    public void updateCapabilities(DesiredCapabilities desiredCapabilities) {
        this.desiredCapabilities.merge(desiredCapabilities);
    }

}
