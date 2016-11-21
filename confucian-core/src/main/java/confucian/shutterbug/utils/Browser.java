package confucian.shutterbug.utils;

import confucian.driver.Driver;
import confucian.driver.DriverUtility;
import confucian.exception.FrameworkException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static confucian.driver.DriverUtility.executeJsScriptOfFile;

/**
 * 浏览器
 */
public class Browser {

    /**
     * The constant RELATIVE_COORDS_JS.
     */
    public static final String RELATIVE_COORDS_JS = "js/relative-element-coords.js";
    /**
     * The constant MAX_DOC_WIDTH_JS.
     */
    public static final String MAX_DOC_WIDTH_JS = "js/max-document-width.js";
    /**
     * The constant MAX_DOC_HEIGHT_JS.
     */
    public static final String MAX_DOC_HEIGHT_JS = "js/max-document-height.js";
    /**
     * The constant VIEWPORT_HEIGHT_JS.
     */
    public static final String VIEWPORT_HEIGHT_JS = "js/viewport-height.js";
    /**
     * The constant VIEWPORT_WIDTH_JS.
     */
    public static final String VIEWPORT_WIDTH_JS = "js/viewport-width.js";
    /**
     * The constant SCROLL_TO_JS.
     */
    public static final String SCROLL_TO_JS = "js/scroll-to.js";
    /**
     * The constant CURRENT_SCROLL_Y_JS.
     */
    public static final String CURRENT_SCROLL_Y_JS = "js/get-current-scrollY.js";
    /**
     * The constant CURRENT_SCROLL_X_JS.
     */
    public static final String CURRENT_SCROLL_X_JS = "js/get-current-scrollX.js";

    private int docHeight = Driver.getDriver() == null ?
            -1 :
            Driver.getDriver().manage().window().getSize().getHeight();
    private int docWidth = Driver.getDriver() == null ?
            -1 :
            Driver.getDriver().manage().window().getSize().getWidth();
    private int viewportWidth = -1;
    private int viewportHeight = -1;

    /**
     * 实例化一个新的浏览器
     */
    public Browser() {
    }

    /**
     * 截图缓冲图像.
     *
     * @return 缓冲图像
     */
    public BufferedImage takeScreenshot() {
        File srcFile = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.FILE);
        try {
            return ImageIO.read(srcFile);
        } catch (IOException e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * 截图整个页面缓冲图像.
     *
     * @return 缓冲图像
     */
    public BufferedImage takeScreenshotEntirePage() {
        BufferedImage combinedImage =
                new BufferedImage(this.getDocWidth(), this.getDocHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combinedImage.createGraphics();
        int horizontalIterations = (int) Math.ceil(((double) this.getDocWidth()) / this.getViewportWidth());
        int verticalIterations = (int) Math.ceil(((double) this.getDocHeight()) / this.getViewportHeight());
        outer_loop:
        for (int j = 0; j < verticalIterations; j++) {
            this.scrollTo(0, j * this.getViewportHeight());
            for (int i = 0; i < horizontalIterations; i++) {
                this.scrollTo(i * this.getViewportWidth(), this.getViewportHeight() * j);
                Image image = takeScreenshot();
                g.drawImage(image, this.getCurrentScrollX(), this.getCurrentScrollY(), null);
                if (this.getDocWidth() == image.getWidth(null) && this.getDocHeight() == image.getHeight(null)) {
                    break outer_loop;
                }
            }
        }
        g.dispose();
        return combinedImage;
    }

    /**
     * 截图水平滚动缓冲图像.
     *
     * @return 缓冲图像
     */
    public BufferedImage takeScreenshotScrollHorizontally() {
        BufferedImage combinedImage =
                new BufferedImage(this.getDocWidth(), this.getViewportHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combinedImage.createGraphics();
        int horizontalIterations = (int) Math.ceil(((double) this.getDocWidth()) / this.getViewportWidth());
        for (int i = 0; i < horizontalIterations; i++) {
            this.scrollTo(i * this.getViewportWidth(), 0);
            Image image = takeScreenshot();
            g.drawImage(image, this.getCurrentScrollX(), 0, null);
            if (this.getDocWidth() == image.getWidth(null)) {
                break;
            }
        }
        g.dispose();
        return combinedImage;
    }

    /**
     * 截图垂直滚动缓冲图像
     *
     * @return 缓冲图像
     */
    public BufferedImage takeScreenshotScrollVertically() {
        BufferedImage combinedImage =
                new BufferedImage(this.getViewportWidth(), this.getDocHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combinedImage.createGraphics();
        int verticalIterations = (int) Math.ceil(((double) this.getDocHeight()) / this.getViewportHeight());
        for (int j = 0; j < verticalIterations; j++) {
            this.scrollTo(0, j * this.getViewportHeight());
            Image image = takeScreenshot();
            g.drawImage(image, 0, this.getCurrentScrollY(), null);
            if (this.getDocHeight() == image.getHeight(null)) {
                break;
            }
        }
        g.dispose();
        return combinedImage;
    }

    /**
     * 当前滚动X位置
     *
     * @return 当前滚动X位置
     */
    public int getCurrentScrollX() {
        return ((Long) DriverUtility.executeJsScriptOfFile(Browser.CURRENT_SCROLL_X_JS)).intValue();
    }

    /**
     * 当前滚动y位置
     *
     * @return 当前滚动y位置
     */
    public int getCurrentScrollY() {
        return ((Long) executeJsScriptOfFile(Browser.CURRENT_SCROLL_Y_JS)).intValue();
    }

    /**
     * 页面宽度
     *
     * @return 页面宽度
     */
    public int getDocWidth() {
        Object o = executeJsScriptOfFile(MAX_DOC_WIDTH_JS);
        if (o != null) {
            return Math.max(((Long) o).intValue(), docWidth);
        }
        return docWidth;
    }

    /**
     * 页面高度
     *
     * @return 页面高度
     */
    public int getDocHeight() {
        Object value = executeJsScriptOfFile(MAX_DOC_HEIGHT_JS);
        if (value != null) {
            return Math.max(docHeight, ((Long) value).intValue());
        }
        return docHeight;
    }

    /**
     * 窗口宽度.
     *
     * @return 窗口的宽度
     */
    public int getViewportWidth() {
        return viewportWidth != -1 ?
                viewportWidth :
                ((Long) executeJsScriptOfFile(VIEWPORT_WIDTH_JS)).intValue();
    }

    /**
     * 窗口高度
     *
     * @return 窗口高度
     */
    public int getViewportHeight() {
        return viewportHeight != -1 ?
                viewportHeight :
                ((Long) executeJsScriptOfFile(VIEWPORT_HEIGHT_JS)).intValue();
    }

    /**
     * 获取客户端矩形边界
     *
     * @param element the element
     * @return 客户端矩形边界
     */
    public Coordinates getBoundingClientRect(WebElement element) {
        ArrayList<String> list = (ArrayList<String>) executeJsScriptOfFile(RELATIVE_COORDS_JS, element);
        Point start = new Point(Integer.parseInt(list.get(0)), Integer.parseInt(list.get(1)));
        Dimension size = new Dimension(Integer.parseInt(list.get(2)), Integer.parseInt(list.get(3)));
        return new Coordinates(start, size);
    }

    /**
     * 滚动到指定位置.
     *
     * @param x the x
     * @param y the y
     */
    public void scrollTo(int x, int y) {
        executeJsScriptOfFile(SCROLL_TO_JS, x, y);
    }
}
