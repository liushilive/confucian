package confucian.driver;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
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
public interface DriverUtility {
    /**
     * The constant LOGGER.
     */
    Logger LOGGER = LogManager.getLogger();

    /**
     * 接受或驳回窗口提示
     *
     * @param acceptOrDismiss accept：接受<p>dismiss:驳回
     */
    static void acceptOrDismissAlert(UnexpectedAlertBehaviour acceptOrDismiss) {
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
     * Check un checked.
     *
     * @param webElement   the web element
     * @param checkUnCheck the check un check
     * @param checked      the checked
     */
    static void checkUnChecked(WebElement webElement, CHECK_UNCHECKED checkUnCheck, boolean checked) {
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
     * 复选框状态改变，元素应该是可见的
     *
     * @param webElement   复选框元素
     * @param checkUnCheck {@link CHECK_UNCHECKED} 枚举
     */
    static void checkUncheckedCheckBox(WebElement webElement, CHECK_UNCHECKED checkUnCheck) {
        boolean checked = webElement.isSelected();
        checkUnChecked(webElement, checkUnCheck, checked);
    }

    /**
     * 复选框状态改变，元素应该是可见的，非正规复选框，class中以cur标志判断。
     *
     * @param webElement   复选框元素
     * @param checkUnCheck {@link CHECK_UNCHECKED} 枚举
     */
    static void checkUncheckedCheckBox_cur(WebElement webElement, CHECK_UNCHECKED checkUnCheck) {
        boolean checked = webElement.getAttribute("class").contains("cur");
        checkUnChecked(webElement, checkUnCheck, checked);
    }

    /**
     * Click.
     *
     * @param element the element
     */
    static void click(WebElement element) {
        int iTimeout = 20;
        while (iTimeout > 0)
            try {
                element.click();
                return;
            } catch (org.openqa.selenium.WebDriverException err) {
                iTimeout--;
                if (err.getMessage().contains("not clickable at point")) {
                    if (iTimeout == 0) {
                        throw err;
                    }
                } else {
                    throw err;
                }
                DriverUtility.wait(1000);
            }
    }

    /**
     * Close others windows.
     */
    static void closeOthersWindows() {
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
     * Close window.
     */
    static void closeWindow() {
        Driver.getDriver().close();
    }

    /**
     * Double click.
     *
     * @param element the element
     */
    static void doubleClick(WebElement element) {
        Actions action = new Actions(Driver.getDriver());
        action.doubleClick(element);
        action.perform();
        // executeJsScript("var evt = document.createEvent('MouseEvents');" +
        //         "evt.initMouseEvent('dblclick',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);" +
        //         "arguments[0].dispatchEvent(evt);", element);
    }

    /**
     * 拖拽元素
     *
     * @param sourceElement 需要拖拽的元素
     * @param targetElement 目标元素
     */
    static void dragAndDrop(WebElement sourceElement, WebElement targetElement) {
        Actions a = new Actions(getDriver());
        a.dragAndDrop(sourceElement, targetElement).build().perform();
    }

    /**
     * 执行 JS
     *
     * @param JS   the js
     * @param args the args
     *
     * @return the object
     */
    static Object executeJsScript(String JS, Object... args) {
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
     *
     * @return the object
     */
    static Object executeJsScriptOfFile(String filePath, Object... arg) {
        String script = Utils.getResourcesOfFile(filePath);
        return executeJsScript(script, arg);
    }

    /**
     * Find web element.
     *
     * @param by the by
     *
     * @return the web element
     */
    static WebElement find(By by) {
        return Driver.getDriver().findElement(by);
    }

    /**
     * Find web element.
     *
     * @param element the element
     * @param by      the by
     *
     * @return the web element
     */
    static WebElement find(WebElement element, By by) {
        return element.findElement(by);
    }

    /**
     * Find all list.
     *
     * @param element the element
     * @param by      the by
     *
     * @return the list
     */
    static List<WebElement> findAll(WebElement element, By by) {
        return element.findElements(by);
    }

    /**
     * Find all list.
     *
     * @param by the by
     *
     * @return the list
     */
    static List<WebElement> findAll(By by) {
        return Driver.getDriver().findElements(by);
    }

    /**
     * 获取属性
     *
     * @param element the element
     * @param href    the href
     *
     * @return the attribute
     */
    static String getAttribute(WebElement element, String href) {
        int iTimeout = 20;
        while (iTimeout > 0) {
            try {
                return element.getAttribute(href);
            } catch (StaleElementReferenceException err) {
                iTimeout--;
                if (err.getMessage().contains("stale element reference")) {
                    if (iTimeout == 0) {
                        throw new FrameworkException(element.toString(), err);
                    }
                } else {
                    throw new FrameworkException(element.toString(), err);
                }
                DriverUtility.wait(1000);
            }
        }
        return null;
    }

    /**
     * 当前滚动X位置
     *
     * @return 当前滚动X位置 current scroll x
     */
    static int getCurrentScrollX() {
        Object value = executeJsScript("return Math.max(document.documentElement.scrollLeft, document.body.scrollLeft);");
        return value != null ? ((Long) value).intValue() : 0;
    }

    /**
     * 当前滚动y位置
     *
     * @return 当前滚动y位置 current scroll y
     */
    static int getCurrentScrollY() {
        Object value = executeJsScript("return Math.max(document.documentElement.scrollTop, document.body.scrollTop);");
        return value != null ? ((Long) value).intValue() : 0;
    }

    /**
     * 页面高度
     *
     * @return 页面高度 doc height
     */
    static int getDocHeight() {
        Object value = executeJsScript("return Math.max(document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);");
        int docHeight = Driver.getDriver() == null ? -1 : Driver.getDriver().manage().window().getSize().getHeight();
        return value != null ? Math.max(docHeight, ((Long) value).intValue()) : docHeight;
    }

    /**
     * 页面宽度
     *
     * @return 页面宽度 doc width
     */
    static int getDocWidth() {
        Object value = executeJsScript("return Math.max(document.body.scrollWidth, document.body.offsetWidth, document.documentElement.clientWidth, document.documentElement.scrollWidth, document.documentElement.offsetWidth);");
        int docWidth = Driver.getDriver() == null ? -1 : Driver.getDriver().manage().window().getSize().getWidth();
        return value == null ? 0 : Math.max(((Long) value).intValue(), docWidth);
    }

    /**
     * Get text string.
     *
     * @param element the element
     *
     * @return the string
     */
    static String getText(WebElement element) {
        return element.getText();
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    static String getTitle() {
        return Driver.getDriver().getTitle();
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    static String getURL() {
        try {
            return URLDecoder.decode(Driver.getDriver().getCurrentUrl(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new FrameworkException("当前URL转换失败：");
        }
    }

    /**
     * 窗口高度
     *
     * @return 窗口高度 viewport height
     */
    static int getViewportHeight() {
        Object script = executeJsScript("return Math.max(document.documentElement.clientHeight, window.innerHeight || 0);");
        return script != null ? ((Long) script).intValue() : 0;
    }

    /**
     * 窗口宽度.
     *
     * @return 窗口的宽度 viewport width
     */
    static int getViewportWidth() {
        Object script = executeJsScript("return Math.max(document.documentElement.clientWidth, window.innerWidth || 0);");
        return script != null ? ((Long) script).intValue() : 0;
    }

    /**
     * 高亮标记
     *
     * @param element element
     * @param b       True 高亮，FALSE取消高亮
     */
    static void highlight(WebElement element, boolean b) {
        try {
            String script = "";
            if (b) script = "color: red; border: 5px solid red;";
            DriverUtility.executeJsScript("arguments[0].setAttribute('style', arguments[1]);", element, script);
        } catch (Exception ignored) {
        }
    }

    /**
     * 检查弹窗是否存在，等待5S
     *
     * @return 存在返回message ，不存在返回null
     */
    static String isAlert() {
        return isAlert(5);
    }

    /**
     * 检查弹窗是否存在
     *
     * @param time the time
     *
     * @return 存在返回message ，不存在返回null
     */
    static String isAlert(int time) {
        Alert alert = waitFor(ExpectedConditions.alertIsPresent(), time);
        return alert == null ? null : alert.getText();
    }

    /**
     * 判断元素是否可点击,默认1S
     *
     * @param element the element
     *
     * @return the boolean
     */
    static boolean isElement(WebElement element) {
        return isElement(element, 1);
    }

    /**
     * 判断元素是否可点击
     *
     * @param element the element
     * @param time    等待时间
     *
     * @return the boolean
     */
    static boolean isElement(WebElement element, int time) {
        return null != waitFor(ExpectedConditionExtended.elementToBeClickable(element), time);
    }

    /**
     * 判断元素是否可点击,默认1S
     *
     * @param locator the locator
     *
     * @return the boolean
     */
    static boolean isElement(By locator) {
        return isElement(locator, null, 1);
    }

    /**
     * 判断元素是否出现
     *
     * @param locator       the locator
     * @param parentElement the parent element
     * @param time          等待时间
     *
     * @return the boolean
     */
    static boolean isElement(By locator, WebElement parentElement, int time) {
        return null != waitFor(ExpectedConditionExtended.invisibilityOfElementLocated(locator, parentElement), time);
    }

    /**
     * 判断元素是否出现,默认1S
     *
     * @param locator       the locator
     * @param parentElement the parent element
     *
     * @return the boolean
     */
    static boolean isElement(By locator, WebElement parentElement) {
        return isElement(locator, parentElement, 1);
    }

    /**
     * 判断元素是否出现
     *
     * @param locator the locator
     * @param time    等待时间
     *
     * @return the boolean
     */
    static boolean isElement(By locator, int time) {
        return isElement(locator, null, time);
    }

    /**
     * 鼠标悬浮
     *
     * @param webElement the web element
     */
    static void mouseOverToElement(WebElement webElement) {
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
                    "evt.initEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);" +
                    "arguments[0].dispatchEvent(evt);";

            executeJsScript(mouseHoverJS, webElement);
        } catch (Exception e) {
            LOGGER.warn("鼠标悬浮", e);
        }
    }

    /**
     * 右击元素
     *
     * @param element the element
     */
    static void rightClick(WebElement element) {
        if (!DriverUtility.isElement(element))
            return;
        Actions action = new Actions(getDriver());
        action.contextClick(element).perform();
    }

    /**
     * 滚动到指定位置.
     *
     * @param x the x
     * @param y the y
     */
    static void scrollTo(int x, int y) {
        executeJsScript("window.scrollTo(arguments[0], arguments[1]);", x, y);
    }

    /**
     * 滚动到元素位置
     *
     * @param element element
     */
    static void scrollToElement(WebElement element) {
        try {
            if (getDriver() != null && element != null) {
                int height = getDriver().manage().window().getSize().getHeight();
                Point coo = element.getLocation();
                executeJsScript("scrollTo(0," + (coo.getY() - height / 3) + ")");
            }
        } catch (Exception ignored) {
            LOGGER.warn(ignored);
        }
    }

    /**
     * 滚动到底部
     */
    static void scrollToFooter() {
        executeJsScript("var q=document.body.scrollTop=document.body.scrollHeight");
    }

    /**
     * 滚动到顶部
     */
    static void scrollTop() {
        executeJsScript("var q=document.body.scrollTop=0");
    }

    /**
     * 查找“option”元素，通过使用部分文本比较选择下拉值
     *
     * @param element     元素
     * @param partialText 部分文本
     */
    static void selectByPartialText(WebElement element, String partialText) {
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
    static void selectDropDown(WebElement webElement, String visibleText, Integer defaultIndex) {
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
    static void selectDropDown(WebElement webElement, Integer defaultIndex) {
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
     *
     * @return the int
     */
    static int selectDropDownSize(WebElement webElement) {
        try {
            Select s = new Select(webElement);
            return s.getOptions().size();
        } catch (NoSuchElementException e) {
            LOGGER.warn(e);
            return 0;
        }
    }

    /**
     * Send keys.
     *
     * @param element the element
     * @param s       the s
     */
    static void sendKeys(WebElement element, String s) {
        if (s != null) {
            element.sendKeys(s);
        }
    }

    /**
     * 设置焦点
     *
     * @param webElement the web element
     */
    static void setFocus(WebElement webElement) {
        try {
            if (webElement == null)
                return;
            if (webElement.getTagName().contains("input"))
                webElement.sendKeys("");
            else
                new Actions(getDriver()).moveToElement(webElement).perform();
        } catch (Exception e) {
            LOGGER.warn(e);
        }
    }

    /**
     * Set text.
     *
     * @param element the element
     * @param s       the s
     */
    static void setText(WebElement element, String s) {
        if (s != null) {
            element.sendKeys(Keys.HOME, Keys.chord(Keys.SHIFT, Keys.END), s);
        }
    }

    /**
     * Submit.
     *
     * @param element the element
     */
    static void submit(WebElement element) {
        element.submit();
    }


    /**
     * Switch to default content.
     */
    static void switchToDefaultContent() {
        getDriver().switchTo().defaultContent();
    }

    /**
     * Switch to frame.
     *
     * @param <T> WebElement/Integer/String
     * @param t   the t
     */
    static <T> void switchToFrame(T t) {
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
    static boolean switchToWindow_New() {
        String selectLinkOpenInNewTab = Keys.chord(Keys.CONTROL, "t");
        waitForLoad();
        for (String s : getDriver().getWindowHandles()) {
            if (getDriver().switchTo().window(s).getTitle().equals("")) {
                return true;
            }
        }
        getDriver().findElement(By.cssSelector("body")).sendKeys(selectLinkOpenInNewTab);
        return switchToWindow_Title("", 2, false);
    }

    /**
     * 切换到下一个窗口
     *
     * @return the boolean
     */
    static boolean switchToWindow_Next() {
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
     *
     * @return 如果窗口切换成功返回True. boolean
     */
    static boolean switchToWindow_Title(String sTitle, int second, boolean isContain) {
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
     *
     * @return 如果窗口切换成功返回True. boolean
     */
    static boolean switchToWindow_URL(String sURL, int second, boolean isContain) {
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
     *
     * @return 文件流 file
     */
    static File takeScreenShot(String path, String imageName, String... titles) {
        File saved = new File(path);
        try {
            if (getDriver() != null) {
                List<Snapshot> snapshotList = Lists.newArrayList();
                String handle_now = getDriver().getWindowHandle();
                Set<String> windowHandles = getDriver().getWindowHandles();
                for (String handle : windowHandles) {
                    String alert = isAlert(1);
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
     * 硬性等待
     *
     * @param millis 毫秒
     */
    static void wait(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * 等待条件的成功，否则返回null
     *
     * @param <T>               类型参数
     * @param expectedCondition :预期条件
     * @param timeOutInSeconds  时间以秒为单位
     *
     * @return T or null
     */
    static <T> T waitFor(ExpectedCondition<T> expectedCondition, int timeOutInSeconds) {
        try {
            getDriver().manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
            return new WebDriverWait(getDriver(), timeOutInSeconds).pollingEvery(500, TimeUnit.MILLISECONDS)
                    .until(expectedCondition);
        } catch (Exception e) {
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
     * 等待网页完全加载完毕
     */
    static void waitForLoad() {
        waitForLoad("");
    }

    /**
     * 等待网页完全加载完毕
     *
     * @param pageName the page name
     *
     * @return the boolean
     */
    static boolean waitForLoad(String pageName) {
        Boolean waitFor = waitFor(ExpectedConditionExtended.pageLoad(), 300);
        if (waitFor == null || !waitFor) {
            LOGGER.warn(pageName + "页面300S内未加载完成:");
            return false;
        }
        return true;
    }

    /**
     * 等待元素在指定时间出现
     *
     * @param <T>      the type parameter
     * @param o        By OR WebElement
     * @param t        秒
     * @param pageName the page name
     */
    static <T> void waitForLoad(T o, int t, String pageName) {
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
     * 枚举选择状态
     */
    enum CHECK_UNCHECKED {
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
     * The interface Cookies.
     */
    interface Cookies {
        /**
         * Add cookies boolean.
         *
         * @param name  the name
         * @param value the value
         *
         * @return the boolean
         */
        static void addCookies(String name, String value) {
            getDriver().manage().addCookie(new Cookie(name, value));
        }

        /**
         * Delete all cookies.
         */
        static void deleteAllCookies() {
            getDriver().manage().deleteAllCookies();
        }

        /**
         * Delete cookies.
         *
         * @param name  the name
         * @param value the value
         */
        static void deleteCookies(String name, String value) {
            getDriver().manage().deleteCookie(new Cookie(name, value));
        }

        /**
         * Delete cookies.
         *
         * @param name the name
         */
        static void deleteCookies(String name) {
            getDriver().manage().deleteCookieNamed(name);
        }

        /**
         * Get cookies set.
         *
         * @return the set
         */
        static Set<Cookie> getCookies() {
            return getDriver().manage().getCookies();
        }

        /**
         * Get cookies string.
         *
         * @param name the name
         *
         * @return the string
         */
        static String getCookies(String name) {
            Cookie cookie = getDriver().manage().getCookieNamed(name);
            return cookie == null ? null : cookie.getValue();
        }
    }

    /**
     * The interface Session storage.
     */
    interface SessionStorage {
        /**
         * Clear session storage.
         */
        static void clearSessionStorage() {
            executeJsScript("window.sessionStorage.clear();");
        }

        /**
         * Gets item from session storage.
         *
         * @param key the key
         *
         * @return the item from session storage
         */
        static String getItemFromSessionStorage(String key) {
            return (String) executeJsScript(
                    "return window.sessionStorage.getItem(arguments[0]);", key);
        }

        /**
         * Gets key from session storage.
         *
         * @param key the key
         *
         * @return the key from session storage
         */
        static String getKeyFromSessionStorage(int key) {
            return (String) executeJsScript(
                    "return window.sessionStorage.key(arguments[0]);", key);
        }

        /**
         * Gets session storage length.
         *
         * @return the session storage length
         */
        static Long getSessionStorageLength() {
            return (Long) executeJsScript("return window.sessionStorage.length;");
        }

        /**
         * Is item present in session storage boolean.
         *
         * @param item the item
         *
         * @return the boolean
         */
        static boolean isItemPresentInSessionStorage(String item) {
            return executeJsScript(
                    "return window.sessionStorage.getItem(arguments[0]);", item) != null;
        }

        /**
         * Remove item from session storage.
         *
         * @param item the item
         */
        static void removeItemFromSessionStorage(String item) {
            executeJsScript(
                    "window.sessionStorage.removeItem(arguments[0]);", item);
        }

        /**
         * Sets item in session storage.
         *
         * @param item  the item
         * @param value the value
         */
        static void setItemInSessionStorage(String item, String value) {
            executeJsScript(
                    "window.sessionStorage.setItem(arguments[0],arguments[1]);", item, value);
        }
    }

    /**
     * The interface Local storage.
     */
    interface LocalStorage {
        /**
         * Clear local storage.
         */
        static void clearLocalStorage() {
            executeJsScript("window.localStorage.clear();");
        }

        /**
         * Gets item from local storage.
         *
         * @param key the key
         *
         * @return the item from local storage
         */
        static String getItemFromLocalStorage(String key) {
            return (String) executeJsScript(
                    "return window.localStorage.getItem(arguments[0]);", key);
        }

        /**
         * Gets key from local storage.
         *
         * @param key the key
         *
         * @return the key from local storage
         */
        static String getKeyFromLocalStorage(int key) {
            return (String) executeJsScript(
                    "return window.localStorage.key(arguments[0]);", key);
        }

        /**
         * Gets local storage length.
         *
         * @return the local storage length
         */
        static Long getLocalStorageLength() {
            return (Long) executeJsScript("return window.localStorage.length;");
        }

        /**
         * Is item present in local storage boolean.
         *
         * @param item the item
         *
         * @return the boolean
         */
        static boolean isItemPresentInLocalStorage(String item) {
            return !(executeJsScript(
                    "return window.localStorage.getItem(arguments[0]);", item) == null);
        }

        /**
         * Remove item from local storage.
         *
         * @param item the item
         */
        static void removeItemFromLocalStorage(String item) {
            executeJsScript(
                    "window.localStorage.removeItem(arguments[0]);", item);
        }

        /**
         * Sets item in local storage.
         *
         * @param item  the item
         * @param value the value
         */
        static void setItemInLocalStorage(String item, String value) {
            executeJsScript(
                    "window.localStorage.setItem(arguments[0],arguments[1]);", item, value);
        }
    }
}
