package confucian.pageObjects.base;

import confucian.driver.Driver;
import confucian.driver.DriverUtility;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * 页面基类
 */
public class PageBase {
    /**
     * 初始化
     */
    protected PageBase() {
        PageFactory.initElements(Driver.getDriver(), this);
    }

    /**
     * 从当前框架等待加载
     *
     * @param webElement the web element
     * @param pageName   the page name
     */
    protected void isFrameLoaded(WebElement webElement, String pageName) {
        DriverUtility.waitForLoad(webElement, 60, pageName);
    }

    /**
     * 从根框架等待加载
     *
     * @param webElement the web element
     * @param pageName   the page name
     */
    protected void isLoaded(WebElement webElement, String pageName) {
        DriverUtility.switchToDefaultFrame();
        DriverUtility.waitForLoad(webElement, 60, pageName);
    }

    /**
     * 等待页面加载完毕
     * 4
     *
     * @param pageName the page name
     */
    protected void isLoaded(String pageName) {
        DriverUtility.waitForLoad(pageName);
    }
}
