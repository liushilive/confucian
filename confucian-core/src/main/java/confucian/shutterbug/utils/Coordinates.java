package confucian.shutterbug.utils;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

/**
 * 坐标类
 */
public class Coordinates {

    private int width;
    private int height;
    private int x;
    private int y;

    /**
     * Instantiates a new Coordinates.
     *
     * @param element the element
     */
    public Coordinates(WebElement element) {
        Point point = element.getLocation();
        Dimension size = element.getSize();
        this.width = size.getWidth();
        this.height = size.getHeight();
        this.x = point.getX();
        this.y = point.getY();
    }

    /**
     * Instantiates a new Coordinates.
     *
     * @param point the point
     * @param size  the size
     */
    Coordinates(Point point, Dimension size) {
        this.width = size.getWidth();
        this.height = size.getHeight();
        this.x = point.getX();
        this.y = point.getY();
    }

    /**
     * Gets width.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets x.
     *
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * Gets y.
     *
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * Gets height.
     *
     * @return the height
     */
    int getHeight() {
        return height;
    }
}
