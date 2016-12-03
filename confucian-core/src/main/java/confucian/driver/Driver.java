package confucian.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import confucian.configuration.DefaultBrowserConfig;
import confucian.data.driverConfig.IBrowserConfig;

/**
 * 驱动程序类，返回Web驱动程序特定的配置
 */
public class Driver {

    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * 浏览器配置
     */
    static InheritableThreadLocal<IBrowserConfig> browserConfig = new InheritableThreadLocal<>();
    /**
     * 浏览器驱动
     */
    static InheritableThreadLocal<WebDriver> driver = new InheritableThreadLocal<>();

    /**
     * 获取浏览器驱动配置
     *
     * @return {@link IBrowserConfig}
     */
    public static IBrowserConfig getBrowserConfig() {
        return browserConfig.get();
    }

    /**
     * 获取浏览器驱动实例
     *
     * @return {@link WebDriver}
     */
    public static WebDriver getDriver() {
        if (driverRemovedStatus()) {
            setDriverValue();
        }
        return driver.get();
    }

    /**
     * 设置浏览器驱动值
     */
    private static void setDriverValue() {
        browserConfig.set(DefaultBrowserConfig.get());
        DriverFactory df = new DriverFactory(DefaultBrowserConfig.get());
        driver.set(df.initializeDriver());
    }

    /**
     * 驱动是否已经删除
     *
     * @return 删除 true
     */
    static boolean driverRemovedStatus() {
        try {
            if (driver == null) {
                return true;
            }
            if (driver.get() == null) {
                return true;
            }

            try {
                if (driver.get() instanceof RemoteWebDriver) {
                    if (((RemoteWebDriver) driver.get()).getSessionId() == null) {
                        return true;
                    }
                } else if (driver.get() instanceof EventFiringWebDriver) {
                    if (((RemoteWebDriver) ((EventFiringWebDriver) driver.get()).getWrappedDriver()).getSessionId() ==
                            null) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
                LOGGER.warn("驱动状态：：getSessionId:" + ignored);
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("驱动是否已经删除", e);
        }
        return true;
    }

    /**
     * 获取浏览器驱动程序实例为特定的配置
     *
     * @param iBrowserConfig 浏览器驱动配置
     * @return {@link WebDriver}
     */
    public static WebDriver getDriver(IBrowserConfig iBrowserConfig) {
        if (driverRemovedStatus()) {
            setDriverValue(iBrowserConfig);
        }
        return driver.get();
    }

    /**
     * 设置浏览器驱动值
     *
     * @param b_conf 驱动配置
     */
    private static void setDriverValue(IBrowserConfig b_conf) {
        browserConfig.set(b_conf);
        DriverFactory df = new DriverFactory(b_conf);
        driver.set(df.initializeDriver());
    }

    /**
     * 卸载当前驱动
     */
    static void tearDown() {
        try {
            if (!driverRemovedStatus()) {
                try {
                    driver.get().quit();
                } catch (Exception ex) {
                    LOGGER.error("卸载当前驱动异常:" + ex);
                }
            }
            if (browserConfig != null && null != browserConfig.get()) {
                browserConfig.set(null);
            }
        } catch (Exception e) {
            LOGGER.error("卸载驱动异常::", e);
        }
    }
}