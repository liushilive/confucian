package confucian.shutterbug.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import confucian.driver.Driver;
import confucian.exception.FrameworkException;

import static confucian.driver.DriverUtility.executeJsScript;
import static confucian.driver.DriverUtility.getCurrentScrollX;
import static confucian.driver.DriverUtility.getCurrentScrollY;
import static confucian.driver.DriverUtility.getDocHeight;
import static confucian.driver.DriverUtility.getDocWidth;
import static confucian.driver.DriverUtility.getViewportHeight;
import static confucian.driver.DriverUtility.getViewportWidth;
import static confucian.driver.DriverUtility.scrollTo;

/**
 * 浏览器截图
 */
public class BrowserScreenshot {
    /**
     * 获取客户端矩形边界
     *
     * @param element the element
     *
     * @return 客户端矩形边界 bounding client rect
     */
    public Coordinates getBoundingClientRect(WebElement element) {
        ArrayList<String> list = (ArrayList<String>) executeJsScript("var rect = arguments[0].getBoundingClientRect();return ['' + parseInt(rect.left), '' + parseInt(rect.top), '' + parseInt(rect.width), '' + parseInt(rect.height)]", element);
        org.openqa.selenium.Point start = new org.openqa.selenium.Point(Integer.parseInt(list.get(0)), Integer.parseInt(list.get(1)));
        org.openqa.selenium.Dimension size = new org.openqa.selenium.Dimension(Integer.parseInt(list.get(2)), Integer.parseInt(list.get(3)));
        return new Coordinates(start, size);
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
                new BufferedImage(getDocWidth(), getDocHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combinedImage.createGraphics();
        int horizontalIterations = (int) Math.ceil(((double) getDocWidth()) / getViewportWidth());
        int verticalIterations = (int) Math.ceil(((double) getDocHeight()) / getViewportHeight());
        outer_loop:
        for (int j = 0; j < verticalIterations; j++) {
            scrollTo(0, j * getViewportHeight());
            for (int i = 0; i < horizontalIterations; i++) {
                scrollTo(i * getViewportWidth(), getViewportHeight() * j);
                Image image = takeScreenshot();
                g.drawImage(image, getCurrentScrollX(), getCurrentScrollY(), null);
                if (getDocWidth() == image.getWidth(null) && getDocHeight() == image.getHeight(null)) {
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
                new BufferedImage(getDocWidth(), getViewportHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combinedImage.createGraphics();
        int horizontalIterations = (int) Math.ceil(((double) getDocWidth()) / getViewportWidth());
        for (int i = 0; i < horizontalIterations; i++) {
            scrollTo(i * getViewportWidth(), 0);
            Image image = takeScreenshot();
            g.drawImage(image, getCurrentScrollX(), 0, null);
            if (getDocWidth() == image.getWidth(null)) {
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
                new BufferedImage(getViewportWidth(), getDocHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combinedImage.createGraphics();
        int verticalIterations = (int) Math.ceil(((double) getDocHeight()) / getViewportHeight());
        for (int j = 0; j < verticalIterations; j++) {
            scrollTo(0, j * getViewportHeight());
            Image image = takeScreenshot();
            g.drawImage(image, 0, getCurrentScrollY(), null);
            if (getDocHeight() == image.getHeight(null)) {
                break;
            }
        }
        g.dispose();
        return combinedImage;
    }
}
