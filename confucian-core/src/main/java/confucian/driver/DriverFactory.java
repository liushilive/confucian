package confucian.driver;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import confucian.data.driverConfig.IBrowserConfig;

/**
 * 返回的驱动实例的工厂类
 */
class DriverFactory {

    private static final Logger LOGGER = LogManager.getLogger();
    private String browser;
    private String chromeServerPath;
    private DesiredCapabilities dc;
    private int driverTimeOut;
    private String ieServerPath;
    private boolean isHighlightElementFlag;
    private boolean remoteFlag;
    private String remoteURL;
    private boolean scrollElementFlag;
    /**
     * web驱动.
     */
    private WebDriver webDriver = null;

    /**
     * 实例化一个新的驱动工厂
     *
     * @param browserConfig 浏览器配置
     */
    DriverFactory(IBrowserConfig browserConfig) {
        this.dc = browserConfig.getCapabilities();
        this.browser = browserConfig.getBrowser();
        this.remoteFlag = browserConfig.isRemoteFlag();
        this.remoteURL = browserConfig.remoteURL();
        this.driverTimeOut = browserConfig.getDriverTimeOut();
        this.ieServerPath = browserConfig.getLocalIEDriverPath();
        this.chromeServerPath = browserConfig.getLocalChromeDriverPath();
        this.isHighlightElementFlag = browserConfig.isHighLightElementFlag();
        this.scrollElementFlag = browserConfig.isScrollElementFlag();
    }

    /**
     * 基于  remoteFlag 返回WebDriver为Selenium Grid本地浏览器
     *
     * @return web driver
     */
    WebDriver initializeDriver() {
        if (remoteFlag) {
            RemoteBrowser rb = this.new RemoteBrowser();
            webDriver = rb.returnRemoteDriver();
        } else if (browser.toLowerCase().startsWith("f")) {
            LOGGER.debug("本地 firefox 驱动.");
            webDriver = new FirefoxDriver(dc);
        } else if (browser.toLowerCase().startsWith("i")) {
            System.setProperty("webdriver.ie.driver", ieServerPath);
            LOGGER.debug("本地 ie 驱动.");
            webDriver = new InternetExplorerDriver(dc);
        } else if (browser.toLowerCase().startsWith("c")) {
            System.setProperty("webdriver.chrome.driver", chromeServerPath);
            LOGGER.debug("本地 chrome 驱动.");
            webDriver = new ChromeDriver(dc);
        } else if (browser.toLowerCase().startsWith("h")) {
            LOGGER.info("浏览器为 HTML—UNIT");
            webDriver = new HtmlUnitDriver(dc);
        }

        // 用于设置驱动超时
        if (webDriver != null) {
            webDriver.manage().timeouts().implicitlyWait(driverTimeOut, TimeUnit.SECONDS);
        }

        if (webDriver != null) {
            EventFiringWebDriver efw = new EventFiringWebDriver(webDriver);
            efw.register(new WebDriverEventListener());
            if (scrollElementFlag)
                efw.register(new ScrollElementFlag());
            if (isHighlightElementFlag)
                efw.register(new HighlightElementFlag());
            webDriver = efw;
        }
        return webDriver;
    }

    /**
     * 返回远程驱动为 Hub
     */
    private class RemoteBrowser {

        /**
         * 实例化一个新的远程浏览器
         */
        public RemoteBrowser() {
            if (StringUtils.isBlank(dc.getBrowserName())) {
                String tmpBrowser = browser.toLowerCase();
                if (tmpBrowser.contains("chrome")) {
                    dc.setBrowserName(BrowserType.CHROME);
                } else if (tmpBrowser.contains("firefox")) {
                    dc.setBrowserName(BrowserType.FIREFOX);
                    dc.setCapability("browser.startup.homepage", "about:blank");
                    dc.setCapability("startup.homepage_welcome_url", "about:blank");
                    dc.setCapability("startup.homepage_welcome_url.additional", "about:blank");
                } else if (tmpBrowser.contains("ie") || tmpBrowser.contains("internet") ||
                        tmpBrowser.contains("explorer")) {
                    dc.setBrowserName(BrowserType.IE);
                    dc.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                } else if (tmpBrowser.contains("opera")) {
                    dc.setBrowserName(BrowserType.OPERA_BLINK);
                } else if (tmpBrowser.contains("android")) {
                    dc.setBrowserName(BrowserType.ANDROID);
                } else if (tmpBrowser.contains("iphone")) {
                    dc.setBrowserName(BrowserType.IPHONE);
                } else if (tmpBrowser.contains("htmlunit")) {
                    dc.setBrowserName(BrowserType.HTMLUNIT);
                    dc.setJavascriptEnabled(true);
                } else if (tmpBrowser.contains("edge")) {
                    dc.setBrowserName(BrowserType.EDGE);
                } else if (tmpBrowser.contains("safari")) {
                    dc.setBrowserName(BrowserType.SAFARI);
                } else if (tmpBrowser.contains("phantomjs")) {
                    dc.setBrowserName(BrowserType.PHANTOMJS);
                } else
                    dc.setBrowserName(browser);
            }
        }

        /**
         * 返回远程驱动
         *
         * @return web driver
         */
        public WebDriver returnRemoteDriver() {
            try {
                RemoteWebDriver driver = new RemoteWebDriver(new URL(remoteURL), dc);

                // 设置本地文件磁盘文件
                driver.setFileDetector(new LocalFileDetector());
                return driver;
            } catch (MalformedURLException e) {
                LOGGER.debug(e);
                return null;
            }
        }
    }
}