package confucian.shutterbug;

import org.openqa.selenium.WebElement;

import confucian.common.ScrollStrategy;
import confucian.driver.DriverUtility;
import confucian.shutterbug.utils.BrowserScreenshot;

/**
 * 截图
 */
public class Shutterbug {
    /**
     * 截取浏览器页面中某一个元素
     *
     * @param element WebElement instance to be screenshotted
     *
     * @return ElementSnapshot instance
     */
    public static ElementSnapshot shootElement(WebElement element) {
        BrowserScreenshot browserScreenshot = new BrowserScreenshot();
        ElementSnapshot elementSnapshot = new ElementSnapshot();
        DriverUtility.scrollToElement(element);
        elementSnapshot.setImage(browserScreenshot.takeScreenshot(), browserScreenshot.getBoundingClientRect(element));
        return elementSnapshot;
    }

    /**
     * 截取浏览器屏幕
     *
     * @param scroll ScrollStrategy How you need to scroll
     *
     * @return PageSnapshot instance
     */
    public static PageSnapshot shootPage(ScrollStrategy scroll) {
        BrowserScreenshot browserScreenshot = new BrowserScreenshot();
        PageSnapshot pageScreenshot = new PageSnapshot();
        if (scroll != null)
            switch (scroll) {
                case HORIZONTALLY:
                    pageScreenshot.setImage(browserScreenshot.takeScreenshotScrollHorizontally());
                    break;
                case VERTICALLY:
                    pageScreenshot.setImage(browserScreenshot.takeScreenshotScrollVertically());
                    break;
                case BOTH_DIRECTIONS:
                    pageScreenshot.setImage(browserScreenshot.takeScreenshotEntirePage());
                    break;
                case DISABLE:
                    pageScreenshot.setImage(browserScreenshot.takeScreenshot());
                    break;
            }
        return pageScreenshot;
    }


}
