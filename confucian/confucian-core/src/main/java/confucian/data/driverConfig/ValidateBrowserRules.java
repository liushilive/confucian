package confucian.data.driverConfig;

import confucian.exception.FrameworkException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * 验证浏览器规则
 */
public class ValidateBrowserRules {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * 浏览器配置
     */
    private IBrowserConfig browserConf;
    /**
     * 异常消息
     */
    private StringBuilder exceptionMessage = new StringBuilder();

    /**
     * 实例化一个新的浏览器验证规则
     *
     * @param refinedBrowserConf 浏览器配置
     */
    ValidateBrowserRules(Map<String, String> refinedBrowserConf) {
        browserConf = new BrowserConfig(refinedBrowserConf);
    }

    /**
     * 验证
     */
    void validate() {
        checkAndThrowExceptionForLocalBrowser();
        checkAndThrowExceptionForRemote();
    }

    /**
     * 检查并抛出异常：本地浏览器
     */
    private void checkAndThrowExceptionForLocalBrowser() {
        if (!browserConf.isRemoteFlag()) {
            if (browserConf.getBrowser().toLowerCase().startsWith("ie") &&
                    browserConf.getLocalIEDriverPath().isEmpty()) {
                exceptionMessage.append("找不到IE驱动地址!");
            }
            if (browserConf.getBrowser().toLowerCase().startsWith("c") &&
                    browserConf.getLocalChromeDriverPath().isEmpty()) {
                exceptionMessage.append("找不到Chrome驱动地址");
            }
        }
        throwExceptionIfAny();
    }

    /**
     * 抛出所有异常
     */
    private void throwExceptionIfAny() {
        if (exceptionMessage.length() != 0) {
            LOGGER.info("!!!!!!!! " + exceptionMessage.toString() + " !!!!!!!!");
            throw new FrameworkException(exceptionMessage.toString());
        }
    }

    /**
     * 检查并抛出异常：Selenium Grid
     */
    private void checkAndThrowExceptionForRemote() {
        if (browserConf.isRemoteFlag() && browserConf.getRemoteURL().isEmpty())
            exceptionMessage.append("远程标志启用 , 请设置 remoteURL");
        throwExceptionIfAny();
    }

}
