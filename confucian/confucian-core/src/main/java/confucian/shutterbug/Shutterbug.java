package confucian.shutterbug;

import confucian.common.ScrollStrategy;
import confucian.driver.DriverUtility;
import confucian.shutterbug.utils.Browser;
import org.openqa.selenium.WebElement;

/**
 * 截图
 */
public class Shutterbug {
    /**
     * 截取浏览器屏幕
     *
     * @param scroll ScrollStrategy How you need to scroll
     * @return PageSnapshot instance
     */
    public static PageSnapshot shootPage(ScrollStrategy scroll) {
        Browser browser = new Browser();
        PageSnapshot pageScreenshot = new PageSnapshot();
        switch (scroll) {
            case HORIZONTALLY:
                pageScreenshot.setImage(browser.takeScreenshotScrollHorizontally());
                break;
            case VERTICALLY:
                pageScreenshot.setImage(browser.takeScreenshotScrollVertically());
                break;
            case BOTH_DIRECTIONS:
                pageScreenshot.setImage(browser.takeScreenshotEntirePage());
                break;
            case DISABLE:
                pageScreenshot.setImage(browser.takeScreenshot());
                break;
        }
        return pageScreenshot;
    }

    /**
     * 截取浏览器页面中某一个元素
     *
     * @param element WebElement instance to be screenshotted
     * @return ElementSnapshot instance
     */
    public static ElementSnapshot shootElement(WebElement element) {
        Browser browser = new Browser();
        ElementSnapshot elementSnapshot = new ElementSnapshot();
        DriverUtility.scrollToElement(element);
        elementSnapshot.setImage(browser.takeScreenshot(), browser.getBoundingClientRect(element));
        return elementSnapshot;
    }
}
