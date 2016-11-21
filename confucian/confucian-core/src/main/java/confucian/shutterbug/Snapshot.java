package confucian.shutterbug;

import com.google.common.collect.Lists;
import confucian.common.Utils;
import confucian.shutterbug.utils.ImageProcessor;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * The type Snapshot.
 *
 * @param <T> the type parameter
 */
public abstract class Snapshot<T extends Snapshot> {

    /**
     * The constant ELEMENT_OUT_OF_VIEWPORT_EX_MESSAGE.
     */
    protected static final String ELEMENT_OUT_OF_VIEWPORT_EX_MESSAGE = "请求元素在窗口之外";
    private static final String EXTENSION = "PNG";
    /**
     * The Image.
     */
    protected BufferedImage image;
    /**
     * The Thumbnail image.
     */
    protected BufferedImage thumbnailImage;
    /**
     * The Driver.
     */
    protected WebDriver driver;
    private String fileName =
            new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date()) + "." + EXTENSION.toLowerCase();
    private Path location = Paths.get("./screenshots/");
    private List<String> titleList = Lists.newArrayList();

    /**
     * 字符串切割,实现字符串自动换行
     *
     * @param text     the text
     * @param maxWidth the max width
     * @param ft       the ft
     * @return the string [ ]
     */
    public static String[] format(String text, int maxWidth, Font ft) {
        String[] result;
        Vector tempR = new Vector();
        int lines = 0;
        int len = text.length();
        int index0;
        int index1 = 0;
        boolean wrap;
        while (true) {
            int widthes = 0;
            wrap = false;
            for (index0 = index1; index1 < len; index1++) {
                if (text.charAt(index1) == '\n') {
                    index1++;
                    wrap = true;
                    break;
                }
                widthes = ft.getSize() + widthes;

                if (widthes > (maxWidth * 0.9)) {
                    break;
                }
            }
            lines++;

            if (wrap) {
                tempR.addElement(text.substring(index0, index1 - 1));
            } else {
                tempR.addElement(text.substring(index0, index1));
            }
            if (index1 >= len) {
                break;
            }
        }
        result = new String[lines];
        tempR.copyInto(result);
        return result;
    }

    /**
     * 文件名
     *
     * @param name file name of the resulted image             by default will be timestamp in format: 'yyyy_MM_dd_HH_mm_ss_SSS'.
     * @return instance of type Snapshot
     */
    public T withName(String name) {
        if (name != null) {
            fileName = name + "." + EXTENSION.toLowerCase();
        }
        return self();
    }

    /**
     * Self t.
     *
     * @return the t
     */
    protected abstract T self();

    /**
     * 写入标题
     *
     * @param title title of the resulted image.              Won't be assigned by default.
     * @return instance of type Snapshot
     */
    public T withTitle(String title) {
        this.titleList.add(title);
        return self();
    }

    /**
     * Generate a thumbnail of the original screenshot.
     * Will save different thumbnails depends on when it was called in the chain.
     *
     * @param path  to save thumbnail image to
     * @param name  of the resulting image
     * @param scale to apply
     * @return instance of type Snapshot
     */
    public T withThumbnail(Path path, String name, double scale) {
        return withThumbnail(path.toString(), name, scale);
    }

    /**
     * Generate a thumbnail of the original screenshot.
     * Will save different thumbnails depends on when it was called in the chain.
     *
     * @param path  to save thumbnail image to
     * @param name  of the resulting image
     * @param scale to apply
     * @return instance of type Snapshot
     */
    public T withThumbnail(String path, String name, double scale) {
        File thumbnailFile = new File(path.toString(), name);
        if (!Files.exists(Paths.get(path))) {
            thumbnailFile.mkdirs();
        }
        thumbnailImage = ImageProcessor.scale(image, scale);
        Utils.writeImage(thumbnailImage, EXTENSION, thumbnailFile);
        return self();
    }

    /**
     * Generate a thumbnail of the original screenshot.
     * Will save different thumbnails depends on when it was called in the chain.
     *
     * @param scale to apply
     * @return instance of type Snapshot
     */
    public T withThumbnail(double scale) {
        return withThumbnail(Paths.get(location.toString(), "./thumbnails").toString(), "thumb_" + fileName, scale);
    }

    /**
     * Apply gray-and-white filter to the image.
     *
     * @return instance of type Snapshot
     */
    public T monochrome() {
        this.image = ImageProcessor.convertToGrayAndWhite(this.image);
        return self();
    }

    /**
     * 保存到指定路径
     *
     * @param path to save image to
     */
    public void save(String path) {
        this.location = Paths.get(path);
        save();
    }

    /**
     * Final method to be called in the chain.
     * Actually saves processed image to the default location: ./screenshots
     */
    public void save() {
        File screenshotFile = new File(location.toString(), fileName);
        if (!Files.exists(location)) {
            screenshotFile.mkdirs();
        }
        for (int i = titleList.size() - 1; i >= 0; i--) {
            if (StringUtils.isNotEmpty(titleList.get(i))) {
                String[] strings = format(titleList.get(i), image.getWidth(), new Font("Serif", Font.BOLD, 20));

                for (int i1 = strings.length - 1; i1 >= 0; i1--) {
                    String string = strings[i1];
                    image = ImageProcessor.addTitle(image, string, Color.red, new Font("Serif", Font.BOLD, 20));
                }
            }
        }
        Utils.writeImage(image, EXTENSION, screenshotFile);
    }

    /**
     * Equals boolean.
     *
     * @param o         Object to compare with
     * @param deviation allowed deviation while comparing.
     * @return true if the the percentage of differences between current image and provided one is less than or equal to <b>deviation</b>
     */
    public boolean equals(Object o, double deviation) {
        if (this == o)
            return true;
        if (!(o instanceof Snapshot))
            return false;

        Snapshot that = (Snapshot) o;

        return getImage() != null ?
                ImageProcessor.imagesAreEquals(getImage(), that.getImage(), deviation) :
                that.getImage() == null;
    }

    /**
     * Gets image.
     *
     * @return BufferedImage - current image being processed.
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Sets image.
     *
     * @param image the image
     */
    protected void setImage(BufferedImage image) {
        self().image = image;
    }

    /**
     * Equals boolean.
     *
     * @param image BufferedImage to compare with.
     * @return true if the the provided image and current image are strictly equal.
     */
    public boolean equals(BufferedImage image) {
        if (this.getImage() == image)
            return true;
        return getImage() != null ?
                ImageProcessor.imagesAreEquals(getImage(), image, 0) :
                image == null;
    }

    /**
     * Equals boolean.
     *
     * @param image     BufferedImage to compare with.
     * @param deviation allowed deviation while comparing.
     * @return true if the the percentage of differences between current image and provided one is less than or equal to <b>deviation</b>
     */
    public boolean equals(BufferedImage image, double deviation) {
        if (this.getImage() == image)
            return true;
        return getImage() != null ?
                ImageProcessor.imagesAreEquals(getImage(), image, deviation) :
                image == null;
    }

    /**
     * @return image hash code
     */
    @Override
    public int hashCode() {
        return getImage() != null ?
                getImage().hashCode() :
                0;
    }

    /**
     * @param o Object to compare with
     * @return true if the the provided object is of type Snapshot
     * and images are strictly equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Snapshot))
            return false;

        Snapshot that = (Snapshot) o;

        return getImage() != null ?
                ImageProcessor.imagesAreEquals(getImage(), that.getImage(), 0) :
                that.getImage() == null;
    }
}
