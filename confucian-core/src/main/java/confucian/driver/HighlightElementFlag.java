package confucian.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

import static confucian.driver.DriverUtility.highlight;

/**
 * 突出 WebElement 在单击之前和在任何值更改，并添加延迟
 */
public class HighlightElementFlag extends AbstractWebDriverEventListener {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 值变化前
     */
    @Override
    public void beforeChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
        highlight(element, true);
        DriverUtility.wait(300);
        highlight(element, false);
    }

    /**
     * 单击前执行
     */
    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        highlight(element, true);
        DriverUtility.wait(300);
        highlight(element, false);
    }
}
