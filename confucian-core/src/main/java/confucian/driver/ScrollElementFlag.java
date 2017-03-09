package confucian.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

/**
 * 滚动到WebElement元素。
 */
class ScrollElementFlag extends AbstractWebDriverEventListener {

    /**
     * 查找元素之后
     *
     * @param by      the by
     * @param element the element
     * @param driver  the driver
     */
    @Override
    public void afterFindBy(By by, WebElement element, WebDriver driver) {
        DriverUtility.scrollToElement(element);
    }

    /**
     * 单击前执行
     *
     * @param element the element
     * @param driver  the driver
     */
    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        DriverUtility.scrollToElement(element);
    }
}
