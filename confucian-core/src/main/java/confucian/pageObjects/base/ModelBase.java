package confucian.pageObjects.base;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import confucian.driver.DriverUtility;
import confucian.driver.SearchContextElementLocatorFactory;

/**
 * 页面内部模块Base
 */
public class ModelBase {
    /**
     * 初始化
     *
     * @param parent the parent
     */
    protected ModelBase(WebElement parent) {
        DriverUtility.waitForLoad(parent, 60, "");
        SearchContextElementLocatorFactory elementLocatorFactory = new SearchContextElementLocatorFactory(parent);
        PageFactory.initElements(elementLocatorFactory, this);
    }

    /**
     * 等待页面加载完毕
     *
     * @param webElement the web element
     * @param modelName  the model name
     */
    protected void isLoaded(WebElement webElement, String modelName) {
        DriverUtility.waitForLoad(webElement, 60, modelName);
    }
}
