package confucian.driver;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import confucian.common.ExpectedConditionExtended;
import confucian.common.ScrollStrategy;
import confucian.common.Utils;
import confucian.exception.FrameworkException;
import confucian.shutterbug.Shutterbug;
import confucian.shutterbug.Snapshot;

import static com.google.common.base.Preconditions.checkArgument;
import static confucian.driver.Driver.getDriver;

/**
 * WebDriver 相关功能
 */
public class DriverUtility {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 接受或驳回窗口提示
     *
     * @param acceptOrDismiss accept：接受<p>dismiss:驳回
     */
    public static void acceptOrDismissAlert(UnexpectedAlertBehaviour acceptOrDismiss) {
        Alert alert = getDriver().switchTo().alert();
        switch (acceptOrDismiss) {
            case ACCEPT:
                alert.accept();
                break;
            case DISMISS:
                alert.dismiss();
                break;
            case IGNORE:
                break;
        }
    }

    /**
     * 复选框状态改变，元素应该是可见的
     *
     * @param webElement   复选框元素
     * @param checkUnCheck {@link CHECK_UNCHECKED} 枚举
     */
    public static void checkUncheckedCheckBox(WebElement webElement, CHECK_UNCHECKED checkUnCheck) {
        boolean checked = webElement.isSelected();
        checkUnChecked(webElement, checkUnCheck, checked);
    }

    /**
     * 复选框状态改变，元素应该是可见的，非正规复选框，class中以cur标志判断。
     *
     * @param webElement   复选框元素
     * @param checkUnCheck {@link CHECK_UNCHECKED} 枚举
     */
    public static void checkUncheckedCheckBox_cur(WebElement webElement, CHECK_UNCHECKED checkUnCheck) {
        boolean checked = webElement.getAttribute("class").contains("cur");
        checkUnChecked(webElement, checkUnCheck, checked);
    }

    private static void checkUnChecked(WebElement webElement, CHECK_UNCHECKED checkUnCheck, boolean checked) {
        if (checked) {
            if (checkUnCheck.toString().equalsIgnoreCase(CHECK_UNCHECKED.UNCHECKED.toString())) {
                webElement.click();
            }
        } else {
            if (checkUnCheck.toString().equalsIgnoreCase(CHECK_UNCHECKED.CHECK.toString())) {
                webElement.click();
            }
        }
    }

    /**
     * Close others windows.
     */
    public static void closeOthersWindows() {
        String currentHandle = getDriver().getWindowHandle();
        waitForLoad();
        Set<String> handles = getDriver().getWindowHandles();
        handles.remove(currentHandle);
        if (!handles.isEmpty()) {
            for (String handle : handles) {
                getDriver().switchTo().window(handle);
                String title = getDriver().getTitle();
                getDriver().close();
                LOGGER.debug("关闭窗口::" + title);
            }
            getDriver().switchTo().window(currentHandle);
        } else {
            LOGGER.debug("无法关闭其他窗口，只有一个窗口句柄 :" + currentHandle);
        }
    }

    /**
     * 等待网页完全加载完毕
     */
    public static void waitForLoad() {
        waitForLoad("");
    }

    /**
     * 等待网页完全加载完毕
     *
     * @param pageName the page name
     */
    public static void waitForLoad(String pageName) {
        Boolean waitFor = null;
        try {
            waitFor = waitFor(ExpectedConditionExtended.pageLoad(), 300, "网页加载完毕");
        } catch (Exception e) {
            LOGGER.warn(pageName + "页面300S内未加载完成:", e);
        }
        if (waitFor == null || !waitFor) {
            LOGGER.warn(pageName + "页面300S内未加载完成:");
        }
    }

    /**
     * 等待条件的成功，否则返回null
     *
     * @param <T>               类型参数
     * @param expectedCondition :预期条件
     * @param timeOutInSeconds  时间以秒为单位
     * @param name              the name
     * @return T or null
     */
    public static <T> T waitFor(ExpectedCondition<T> expectedCondition, int timeOutInSeconds, String name) {
        try {
            getDriver().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            return new WebDriverWait(getDriver(), timeOutInSeconds).pollingEvery(500, TimeUnit.MILLISECONDS)
                    .until(expectedCondition);
        } catch (Exception e) {
            LOGGER.debug(e);
            return null;
        } finally {
            try {
                getDriver().manage().timeouts()
                        .implicitlyWait(Driver.getBrowserConfig().getDriverTimeOut(), TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // 忽略
            }
        }
    }

    /**
     * 双击 WebElement 使用 JavaScript 或自定义类
     *
     * @param element       需要双击的元素
     * @param clickStrategy {@link CLICK_STRATEGY}枚举
     */
    public static void doubleClick(WebElement element, CLICK_STRATEGY clickStrategy) {
        if (!DriverUtility.isElement(element))
            return;
        WebDriver driver = getDriver();
        switch (clickStrategy) {
            case USING_ACTION:
                Actions action = new Actions(driver);
                action.doubleClick(element).perform();
                break;
            case USING_JS:
                executeJsScript("var evt = document.createEvent('MouseEvents');" +
                        "evt.initMouseEvent('dblclick',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);" +
                        "arguments[0].dispatchEvent(evt);", element);
                break;
            default:
                String clickStrategyParameter;
                try {
                    clickStrategyParameter = clickStrategy.toString();
                } catch (Exception e) {
                    clickStrategyParameter = "null";
                }
                LOGGER.error("参数不正确: 未知点击策略. " + clickStrategyParameter);
        }
    }

    /**
     * 判断元素是否可点击,默认1S
     *
     * @param element the element
     * @return the boolean
     */
    public static boolean isElement(WebElement element) {
        return isElement(element, 1);
    }

    /**
     * 判断元素是否可点击
     *
     * @param element the element
     * @param time    等待时间
     * @return the boolean
     */
    public static boolean isElement(WebElement element, int time) {
        return isElement(element, time, element.toString());
    }

    /**
     * 判断元素是否可点击
     *
     * @param element the element
     * @param time    等待时间
     * @param s       the s
     * @return the boolean
     */
    public static boolean isElement(WebElement element, int time, String s) {
        try {
            return null != waitFor(ExpectedConditionExtended.elementToBeClickable(element), time, s);
        } catch (Exception e) {
            LOGGER.warn(e);
            return false;
        }
    }

    /**
     * 执行 JS
     *
     * @param JS   the js
     * @param args the args
     * @return the object
     */
    public static Object executeJsScript(String JS, Object... args) {
        if (JS.isEmpty())
            return null;
        try {
            return ((JavascriptExecutor) getDriver()).executeScript(JS, args);
        } catch (Exception e) {
            LOGGER.warn("执行JS: " + JS, e);
        }
        return null;
    }

    /**
     * 从文件执行JS
     *
     * @param filePath the file path
     * @param arg      the arg
     * @return the object
     */
    public static Object executeJsScriptOfFile(String filePath, Object... arg) {
        String script = Utils.getResourcesOfFile(filePath);
        return executeJsScript(script, arg);
    }

    /**
     * 拖拽元素
     *
     * @param sourceElement 需要拖拽的元素
     * @param targetElement 目标元素
     */
    public static void dragAndDrop(WebElement sourceElement, WebElement targetElement) {
        WebDriver driver = getDriver();
        Actions a = new Actions(driver);
        a.dragAndDrop(sourceElement, targetElement).perform();
    }

    /**
     * 检查弹窗是否存在，等待5S
     *
     * @return 存在返回message ，不存在返回null
     */
    public static String isAlert() {
        return isAlert(5);
    }

    /**
     * 检查弹窗是否存在
     *
     * @param time the time
     * @return 存在返回message ，不存在返回null
     */
    public static String isAlert(int time) {
        Alert alert = waitFor(ExpectedConditions.alertIsPresent(), time, "Alert");
        if (alert == null)
            return null;
        return alert.getText();
    }

    /**
     * 判断元素是否可点击,默认1S
     *
     * @param locator the locator
     * @return the boolean
     */
    public static boolean isElement(By locator) {
        return isElement(locator, null, 1, locator.toString());
    }

    /**
     * 判断元素是否出现
     *
     * @param locator       the locator
     * @param parentElement the parent element
     * @param time          等待时间
     * @param s             the s
     * @return the boolean
     */
    public static boolean isElement(By locator, WebElement parentElement, int time, String s) {
        try {
            return null !=
                    waitFor(ExpectedConditionExtended.invisibilityOfElementLocated(locator, parentElement), time, s);
        } catch (Exception e) {
            LOGGER.warn(e);
            return false;
        }
    }

    /**
     * 判断元素是否出现,默认1S
     *
     * @param locator       the locator
     * @param parentElement the parent element
     * @return the boolean
     */
    public static boolean isElement(By locator, WebElement parentElement) {
        return isElement(locator, parentElement, 1, locator.toString());
    }

    /**
     * 判断元素是否出现
     *
     * @param locator       the locator
     * @param parentElement the parent element
     * @param time          等待时间
     * @return the boolean
     */
    public static boolean isElement(By locator, WebElement parentElement, int time) {
        return isElement(locator, parentElement, time, locator.toString());
    }

    /**
     * 鼠标悬浮
     *
     * @param webElement the web element
     */
    public static void moveToElement(WebElement webElement) {
        WebDriver webDriver = getDriver();
        try {
            if (webDriver == null) {
                LOGGER.warn("鼠标无法悬浮，Driver为空");
                return;
            }
            if (!DriverUtility.isElement(webElement)) {
                LOGGER.warn("鼠标无法悬浮，传入元素不存在");
                return;
            }
            String mouseHoverJS = "var evt = document.createEvent('MouseEvents');" +
                    "evt.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);" +
                    "arguments[0].dispatchEvent(evt);";
            executeJsScript(mouseHoverJS, webElement);
        } catch (Exception e) {
            LOGGER.warn("鼠标悬浮", e);
        }
    }

    /**
     * 滚动到底部
     */
    public static void scrollFooter() {
        executeJsScript("var q=document.body.scrollTop=document.body.scrollHeight");
    }

    /**
     * 滚动到元素位置
     *
     * @param element element
     */
    public static void scrollToElement(WebElement element) {
        if (element == null)
            return;
        try {
            int height = getDriver().manage().window().getSize().getHeight();
            Point coo = element.getLocation();
            executeJsScript("scrollTo(0," + (coo.getY() - height / 3) + ")");
        } catch (Exception ignored) {
            LOGGER.warn(ignored);
        }
    }

    /**
     * 滚动到顶部
     */
    public static void scrollTop() {
        executeJsScript("var q=document.body.scrollTop=0");
    }

    /**
     * 查找“option”元素，通过使用部分文本比较选择下拉值
     *
     * @param element     元素
     * @param partialText 部分文本
     */
    public static void selectByPartialText(WebElement element, String partialText) {
        List<WebElement> optionList = element.findElements(By.tagName("option"));
        optionList.stream().filter(webElement -> webElement.getText().toLowerCase().contains(partialText.toLowerCase()))
                .forEachOrdered(WebElement::click);
    }

    /**
     * 下拉框选择，如果没有发现选择文本，则选择默认索引
     *
     * @param webElement   选择WebElement
     * @param visibleText  文本
     * @param defaultIndex 默认索引
     */
    public static void selectDropDown(WebElement webElement, String visibleText, Integer defaultIndex) {
        checkArgument(StringUtils.isNotBlank(visibleText), "选择文本不能为空");
        Select s = null;
        try {
            s = new Select(webElement);
            s.selectByVisibleText(visibleText);
        } catch (NoSuchElementException e) {
            LOGGER.warn(e);
            if (s != null)
                s.selectByIndex(defaultIndex);
        }
    }

    /**
     * 下拉框选择，选择索引
     *
     * @param webElement   选择WebElement
     * @param defaultIndex 索引
     */
    public static void selectDropDown(WebElement webElement, Integer defaultIndex) {
        Select s;
        try {
            s = new Select(webElement);
            s.selectByIndex(defaultIndex);
        } catch (NoSuchElementException e) {
            LOGGER.warn(e);
        }
    }

    /**
     * 下拉框可选数量
     *
     * @param webElement 选择WebElement
     * @return the int
     */
    public static int selectDropDownSize(WebElement webElement) {
        try {
            Select s = new Select(webElement);
            return s.getOptions().size();
        } catch (NoSuchElementException e) {
            LOGGER.warn(e);
            return 0;
        }
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public static String getURL() {
        try {
            return URLDecoder.decode(Driver.getDriver().getCurrentUrl(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new FrameworkException("当前URL转换失败：");
        }
    }

    public static String getTitle() {
        return Driver.getDriver().getTitle();
    }

    /**
     * Switch to default frame boolean.
     */
    public static void switchToDefaultFrame() {
        getDriver().switchTo().defaultContent();
    }

    /**
     * Switch to frame.
     *
     * @param <T> WebElement/Integer/String
     * @param t   the t
     */
    public static <T> void switchToFrame(T t) {
        try {
            if (t instanceof WebElement)
                getDriver().switchTo().frame((WebElement) t);
            else if (t instanceof Integer)
                getDriver().switchTo().frame((Integer) t);
            else if (t instanceof String) {
                getDriver().switchTo().frame(t.toString());
            } else {
                throw new FrameworkException("跳转到框架参数错误:" + t);
            }
        } catch (NoSuchFrameException e) {
            LOGGER.warn("无法找到框架" + t, e);
        } catch (StaleElementReferenceException e) {
            LOGGER.warn("WebElement过期" + t, e);
        }
    }

    /**
     * 打开并切换到新的窗口
     *
     * @return the boolean
     */
    public static boolean switchToWindow_New() {
        String selectLinkOpenInNewTab = Keys.chord(Keys.CONTROL, "t");
        waitForLoad();
        for (String s : getDriver().getWindowHandles()) {
            if (getDriver().switchTo().window(s).getTitle().equals("")) {
                return true;
            }
        }
        getDriver().findElement(By.cssSelector("body")).sendKeys(selectLinkOpenInNewTab);
        return switchToWindow_Title("", 1, false);
    }

    /**
     * 切换到下一个窗口
     *
     * @return the boolean
     */
    public static boolean switchToWindow_Next() {
        waitForLoad();
        String currentHandle = getDriver().getWindowHandle();
        waitForLoad();
        Set<String> handles = getDriver().getWindowHandles();
        handles.remove(currentHandle);
        if (!handles.isEmpty()) {
            for (String handle : handles) {
                try {
                    Driver.getDriver().switchTo().window(handle);
                    return true;
                } catch (NullPointerException ignored) {
                    LOGGER.warn(ignored);
                }
            }
        }
        return false;
    }

    /**
     * 根据标题，切换窗口
     *
     * @param sTitle    :目标窗口标题
     * @param second    等待时间
     * @param isContain 是否为包含关系，true：包含；false:相等
     * @return 如果窗口切换成功返回True. boolean
     */
    public static boolean switchToWindow_Title(String sTitle, int second, boolean isContain) {
        waitForLoad();
        String currentHandle = getDriver().getWindowHandle();
        waitForLoad();
        Set<String> handles;
        long runTime = Instant.now().getEpochSecond(), startTime, consuming;

        long endTime = runTime + second;

        do {
            waitForLoad();
            handles = getDriver().getWindowHandles();
            handles.remove(currentHandle);
            if (!handles.isEmpty()) {
                startTime = Instant.now().getEpochSecond();
                long tempTime = endTime - startTime;
                LOGGER.debug("正在切换到窗口：" + sTitle + " :: " + tempTime);
                for (String handle : handles) {
                    getDriver().switchTo().window(handle);
                    consuming = startTime - runTime;
                    if (!isContain && getDriver().getTitle().equals(sTitle)) {
                        LOGGER.debug("切换到窗口标题:" + sTitle + "  耗时：" + consuming + "S");
                        return true;
                    } else if (isContain && getDriver().getTitle().contains(sTitle)) {
                        LOGGER.debug("切换到窗口标题:" + sTitle + "  耗时：" + consuming + "S");
                        return true;
                    }
                }
            } else {
                LOGGER.debug("只有一个窗口句柄 :" + currentHandle);
                return false;
            }
        } while (startTime <= endTime);

        getDriver().switchTo().window(currentHandle);

        LOGGER.info("窗口标题:" + sTitle + " 不存在，不能够切换");
        return false;

    }

    /**
     * 根据URL，切换窗口
     *
     * @param sURL      :目标窗口URL
     * @param second    等待时间
     * @param isContain 是否为包含关系，true：包含；false:相等
     * @return 如果窗口切换成功返回True. boolean
     */
    public static boolean switchToWindow_URL(String sURL, int second, boolean isContain) {
        String currentHandle = getDriver().getWindowHandle();
        waitForLoad();
        Set<String> handles;
        long runTime = Instant.now().getEpochSecond(), startTime, consuming;

        long endTime = runTime + second;
        do {
            waitForLoad();
            handles = getDriver().getWindowHandles();
            handles.remove(currentHandle);
            if (!handles.isEmpty()) {

                startTime = Instant.now().getEpochSecond();
                consuming = startTime - runTime;
                long tempTime = endTime - startTime;
                LOGGER.debug("切换到其他窗口 URL:" + sURL + " :剩余等待时间: " + tempTime);

                for (String handle : handles) {
                    getDriver().switchTo().window(handle);
                    waitForLoad();
                    if (!isContain && sURL.equals(getURL())) {
                        LOGGER.debug("切换到窗口URL:" + sURL + "  耗时：" + consuming + "S");
                        return true;
                    } else if (isContain && getURL().contains(sURL)) {
                        LOGGER.debug("切换到窗口URL:" + sURL + "  耗时：" + consuming + "S");
                        return true;
                    }
                }
            } else {
                LOGGER.debug("只有一个窗口句柄 :" + currentHandle);
                return false;
            }
        } while (startTime <= endTime);

        getDriver().switchTo().window(currentHandle);

        LOGGER.debug("窗口URL:" + sURL + " 不存在，不能够切换");
        return false;
    }

    /**
     * 屏幕截图
     *
     * @param path      :文件路径，存储屏幕截图
     * @param imageName the image name
     * @param titles    the titles
     * @return 文件流 file
     */
    public static File takeScreenShot(String path, String imageName, String... titles) {
        File saved = new File(path);
        try {
            if (getDriver() != null) {
                List<Snapshot> snapshotList = Lists.newArrayList();
                String handle_now = getDriver().getWindowHandle();
                Set<String> windowHandles = getDriver().getWindowHandles();
                for (String handle : windowHandles) {
                    String alert = isAlert();
                    if (alert != null) acceptOrDismissAlert(UnexpectedAlertBehaviour.ACCEPT);
                    if (!getDriver().getWindowHandle().equals(handle)) {
                        getDriver().switchTo().window(handle);
                    }
                    Snapshot snapshot = Shutterbug.shootPage(ScrollStrategy.BOTH_DIRECTIONS)
                            .withHead("URL：" + getURL()).withHead("Title：" + getTitle());
                    if (alert != null) snapshot.withHead("Alert: " + alert);
                    snapshotList.add(snapshot);
                }
                if (!handle_now.equals(getDriver().getCurrentUrl()))
                    getDriver().switchTo().window(handle_now);
                Snapshot snapshot = Shutterbug.shootPage(null);
                snapshot.joinImagesVertical(snapshotList.stream().map(Snapshot::getImage)
                        .collect(Collectors.toList()));
                snapshot.withTitle(LocalDateTime.now().toString());
                for (String title : titles) snapshot = snapshot.withTitle(title);
                snapshot.withName(imageName).save(path);
                LOGGER.debug("截图保存：" + path + imageName + ".png");
            } else {
                LOGGER.warn("浏览器驱动为空，无法屏幕截图");
            }
        } catch (Exception e) {
            LOGGER.warn("无法屏幕截图", e);
        }
        return saved;
    }

    /**
     * 等待元素在指定时间出现
     *
     * @param <T>      the type parameter
     * @param o        By OR WebElement
     * @param t        秒
     * @param pageName the page name
     */
    public static <T> void waitForLoad(T o, int t, String pageName) {
        waitForLoad(pageName);
        boolean b;
        if (o instanceof By)
            b = isElement((By) o, t);
        else if (o instanceof WebElement)
            b = isElement((WebElement) o, t);
        else
            throw new FrameworkException("非法类型：" + o.toString());
        if (!b) {
            LOGGER.warn(pageName + "  " + t + " S 内，未找到" + o.toString());
        }
    }

    /**
     * 判断元素是否出现
     *
     * @param locator the locator
     * @param time    等待时间
     * @return the boolean
     */
    public static boolean isElement(By locator, int time) {
        return isElement(locator, null, time, locator.toString());
    }

    /**
     * 硬性等待
     *
     * @param millis 毫秒
     */
    public static void wait(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 高亮标记
     *
     * @param element element
     */
    public static void highlight(WebElement element) {
        try {
            DriverUtility.executeJsScript("arguments[0].setAttribute('style', arguments[1]);", element,
                    "color: red; border: 5px solid red;");
        } catch (Exception ignored) {
        }
    }

    /**
     * 禁用高亮标记
     *
     * @param element the element
     */
    public static void disableHighlight(WebElement element) {
        try {
            DriverUtility.executeJsScript("arguments[0].setAttribute('style', arguments[1]);", element, "");
        } catch (Exception e) {
            // 忽略异常，这通常会抛出
            // NoSuchElementException & StaleElementReferenceException
        }
    }

    /**
     * 枚举选择状态
     */
    public enum CHECK_UNCHECKED {
        /**
         * 选择
         */
        CHECK,
        /**
         * 不选择
         */
        UNCHECKED
    }

    /**
     * 枚举点击策略.
     */
    public enum CLICK_STRATEGY {
        /**
         * 使用JS
         */
        USING_JS,
        /**
         * 使用自定义方法
         */
        USING_ACTION
    }
}
