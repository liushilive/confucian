package confucian.shutterbug.utils;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.List;

import confucian.exception.FrameworkException;

/**
 * 图像处理类
 */
public class ImageProcessor {

    private static final int ARCH_SIZE = 10;
    private static float[] matrix = new float[49];

    static {
        for (int i = 0; i < 49; i++)
            matrix[i] = 1.0f / 49.0f;
    }

    /**
     * Add text buffered image.
     *
     * @param sourceImage the source image
     * @param x           the x
     * @param y           the y
     * @param text        the text
     * @param color       the color
     * @param font        the font
     *
     * @return the buffered image
     */
    public static BufferedImage addText(BufferedImage sourceImage, int x, int y, String text, Color color, Font font) {
        Graphics2D g = sourceImage.createGraphics();
        g.setPaint(color);
        g.setFont(font);
        g.getFontMetrics();
        g.drawString(text, x, y);
        g.dispose();
        return sourceImage;
    }

    /**
     * Add title buffered image.
     *
     * @param sourceImage the source image
     * @param title       the title
     * @param color       the color
     * @param textFont    the text font
     *
     * @return the buffered image
     */
    public static BufferedImage addTitle(BufferedImage sourceImage, String title, Color color, Font textFont) {
        BufferedImage combined = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight() + textFont.getSize(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combined.createGraphics();
        g.drawImage(sourceImage, 0, textFont.getSize() + 5, null);
        addText(combined, textFont.getSize(), textFont.getSize(), title, color, textFont);
        g.dispose();
        return combined;
    }

    /**
     * Blur buffered image.
     *
     * @param sourceImage the source image
     *
     * @return the buffered image
     */
    public static BufferedImage blur(BufferedImage sourceImage) {
        BufferedImageOp options = new ConvolveOp(new Kernel(7, 7, matrix), ConvolveOp.EDGE_NO_OP, null);
        return options.filter(sourceImage, null);
    }

    /**
     * Blur area buffered image.
     *
     * @param sourceImage the source image
     * @param coordinates the coordinates
     *
     * @return the buffered image
     */
    public static BufferedImage blurArea(BufferedImage sourceImage, Coordinates coordinates) {
        BufferedImage blurredImage =
                blur(sourceImage.getSubimage(coordinates.getX(), coordinates.getY(), coordinates.getWidth(), coordinates.getHeight()));
        BufferedImage combined =
                new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combined.createGraphics();
        g.drawImage(sourceImage, 0, 0, null);
        g.drawImage(blurredImage, coordinates.getX(), coordinates.getY(), null);
        g.dispose();
        return combined;
    }

    /**
     * Blur except area buffered image.
     *
     * @param sourceImage the source image
     * @param coordinates the coordinates
     *
     * @return the buffered image
     */
    public static BufferedImage blurExceptArea(BufferedImage sourceImage, Coordinates coordinates) {
        BufferedImage subImage =
                sourceImage.getSubimage(coordinates.getX(), coordinates.getY(), coordinates.getWidth(), coordinates.getHeight());
        BufferedImage blurredImage = blur(sourceImage);
        BufferedImage combined =
                new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combined.createGraphics();
        g.drawImage(blurredImage, 0, 0, null);
        g.drawImage(subImage, coordinates.getX(), coordinates.getY(), null);
        g.dispose();
        return combined;
    }

    /**
     * Convert to gray and white buffered image.
     *
     * @param sourceImage the source image
     *
     * @return the buffered image
     */
    public static BufferedImage convertToGrayAndWhite(BufferedImage sourceImage) {
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(sourceImage, sourceImage);
        return sourceImage;
    }

    /**
     * Gets element.
     *
     * @param sourceImage the source image
     * @param coordinates the coordinates
     *
     * @return the element
     */
    public static BufferedImage getElement(BufferedImage sourceImage, Coordinates coordinates) {
        return sourceImage.getSubimage(coordinates.getX(), coordinates.getY(), coordinates.getWidth(), coordinates.getHeight());
    }

    /**
     * 高亮图像缓存
     *
     * @param sourceImage the source image
     * @param coordinates the coordinates
     * @param color       the color
     * @param lineWidth   the line width
     *
     * @return the buffered image
     */
    public static BufferedImage highlight(BufferedImage sourceImage, Coordinates coordinates, Color color, int lineWidth) {
        byte defaultLineWidth = 3;
        Graphics2D g = sourceImage.createGraphics();
        g.setPaint(color);
        g.setStroke(new BasicStroke(lineWidth == 0 ?
                defaultLineWidth :
                lineWidth));
        g.drawRoundRect(coordinates.getX(), coordinates.getY(), coordinates.getWidth(), coordinates.getHeight(), ARCH_SIZE, ARCH_SIZE);
        g.dispose();
        return sourceImage;
    }

    /**
     * Images are equals boolean.
     *
     * @param image1    the image 1
     * @param image2    the image 2
     * @param deviation the deviation
     *
     * @return the boolean
     */
    public static boolean imagesAreEquals(BufferedImage image1, BufferedImage image2, double deviation) {
        int width1 = image1.getWidth(null);
        int width2 = image2.getWidth(null);
        int height1 = image1.getHeight(null);
        int height2 = image2.getHeight(null);
        if ((width1 != width2) || (height1 != height2)) {
            throw new FrameworkException(
                    "图片的尺寸不匹配: image1 - " + width1 + "x" + height1 + "; image2 - " + width2 + "x" + height2);
        }
        long diff = 0;
        for (int y = 0; y < height1; y++) {
            for (int x = 0; x < width1; x++) {
                int rgb1 = image1.getRGB(x, y);
                int rgb2 = image2.getRGB(x, y);
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = (rgb1) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = (rgb2) & 0xff;
                diff += Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);
            }
        }
        double n = width1 * height1 * 3;
        double p = diff / n / 255.0;
        return p == 0 || p <= deviation;
    }

    /**
     * 纵向拼接图片
     *
     * @param image 图片组
     *
     * @return the buffered image
     */
    public static BufferedImage joinImagesVertical(List<BufferedImage> image) {
        int width = 0, height = 0;
        for (BufferedImage bufferedImage : image) {
            int width1 = bufferedImage.getWidth();
            int height1 = bufferedImage.getHeight();
            width = width > width1 ? width : width1;
            height += height1 + 5;
        }
        BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //生成新图片
        int dst_height = 0;
        Graphics2D g = combinedImage.createGraphics();
        for (BufferedImage anImage : image) {
            g.drawImage(anImage, 0, dst_height, null);
            dst_height += anImage.getHeight() + 5;
        }
        return combinedImage;
    }

    /**
     * Monochrome area buffered image.
     *
     * @param sourceImage the source image
     * @param coordinates the coordinates
     *
     * @return the buffered image
     */
    public static BufferedImage monochromeArea(BufferedImage sourceImage, Coordinates coordinates) {
        BufferedImage monochromeImage = convertToGrayAndWhite(
                sourceImage.getSubimage(coordinates.getX(), coordinates.getY(), coordinates.getWidth(), coordinates.getHeight()));
        BufferedImage combined =
                new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combined.createGraphics();
        g.drawImage(sourceImage, 0, 0, null);
        g.drawImage(monochromeImage, coordinates.getX(), coordinates.getY(), null);
        g.dispose();
        return combined;
    }

    /**
     * Scale buffered image.
     *
     * @param source the source
     * @param ratio  the ratio
     *
     * @return the buffered image
     */
    public static BufferedImage scale(BufferedImage source, double ratio) {
        int w = (int) (source.getWidth() * ratio);
        int h = (int) (source.getHeight() * ratio);
        BufferedImage scaledImage = getCompatibleImage(w, h);
        Graphics2D resultGraphics = scaledImage.createGraphics();
        resultGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        resultGraphics.drawImage(source, 0, 0, w, h, null);
        resultGraphics.dispose();
        return scaledImage;
    }

    private static BufferedImage getCompatibleImage(int w, int h) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        return gc.createCompatibleImage(w, h);
    }
}
