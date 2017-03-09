package confucian.shutterbug;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;

import confucian.common.Utils;
import confucian.shutterbug.utils.ImageProcessor;

/**
 * The type Snapshot.
 *
 * @param <T> the type parameter
 */
public abstract class Snapshot<T extends Snapshot> {

    /**
     * The constant ELEMENT_OUT_OF_VIEWPORT_EX_MESSAGE.
     */
    static final String ELEMENT_OUT_OF_VIEWPORT_EX_MESSAGE = "请求元素在窗口之外";
    private static final String EXTENSION = "PNG";
    /**
     * The Driver.
     */
    protected WebDriver driver;
    /**
     * The Image.
     */
    BufferedImage image;
    private String fileName =
            DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS").format(LocalDateTime.now())
                    + "." + EXTENSION.toLowerCase();
    private Path location = Paths.get("./screenshots/");
    private List<String> titleList = Lists.newArrayList();

    /**
     * Equals boolean.
     *
     * @param o         Object to compare with
     * @param deviation allowed deviation while comparing.
     *
     * @return true if the the percentage of differences between current image and provided one is
     * less than or equal to <b>deviation</b>
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
     * Equals boolean.
     *
     * @param image BufferedImage to compare with.
     *
     * @return 真的, 如果提供的图像和当前图像严格相等 。
     */
    public boolean equals(BufferedImage image) {
        return this.getImage() == image || (getImage() != null ?
                ImageProcessor.imagesAreEquals(getImage(), image, 0) : image == null);
    }

    /**
     * Equals boolean.
     *
     * @param image     BufferedImage to compare with.
     * @param deviation allowed deviation while comparing.
     *
     * @return true if the the percentage of differences between current image and provided one is
     * less than or equal to <b>deviation</b>
     */
    public boolean equals(BufferedImage image, double deviation) {
        return this.getImage() == image || (getImage() != null ?
                ImageProcessor.imagesAreEquals(getImage(), image, deviation) : image == null);
    }

    /**
     * @param o Object to compare with
     *
     * @return true if the the provided object is of type Snapshot and images are strictly equal.
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

    /**
     * Gets image.
     *
     * @return BufferedImage - 当前图像.
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Sets image.
     *
     * @param image the image
     */
    void setImage(BufferedImage image) {
        self().image = image;
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
     * 纵向拼接图片
     *
     * @param image 图片组
     *
     * @return the buffered image
     */
    public T joinImagesVertical(List<BufferedImage> image) {
        self().image = ImageProcessor.joinImagesVertical(image);
        return self();
    }

    /**
     * 对图像应用灰度和白色滤镜。
     *
     * @return the t
     */
    public T monochrome() {
        this.image = ImageProcessor.convertToGrayAndWhite(this.image);
        return self();
    }

    /**
     * 保存到指定路径
     *
     * @param path the path
     */
    public void save(String path) {
        this.location = Paths.get(path);
        save();
    }

    /**
     * 在链中调用的最终方法。
     * 实际上将处理后的图像保存到默认位置：./screenshots
     */
    public void save() {
        File screenshotFile = new File(location.toString(), fileName);
        if (!Files.exists(location)) {
            screenshotFile.mkdirs();
        }
        for (int i = titleList.size() - 1; i >= 0; i--) {
            image = ImageProcessor.addTitle(image, "", Color.red,
                    new Font("Serif", Font.BOLD, 20));
            if (StringUtils.isNotEmpty(titleList.get(i))) {
                List<String> strings = Utils.format(titleList.get(i), image.getWidth(),
                        new Font("Serif", Font.BOLD, 20));
                for (int i1 = strings.size() - 1; i1 >= 0; i1--) {
                    String string = strings.get(i1);
                    image = ImageProcessor.addTitle(image, string, Color.red,
                            new Font("Serif", Font.BOLD, 20));
                }
            }
        }
        writeImage(image, EXTENSION, screenshotFile);
    }

    /**
     * 写入头信息，及时写入。后来居上
     *
     * @param head the head
     *
     * @return the t
     */
    public T withHead(String head) {
        List<String> strings = Utils.format(head, image.getWidth(),
                new Font("Serif", Font.BOLD, 20));
        for (int i = strings.size() - 1; i >= 0; i--) {
            image = ImageProcessor.addTitle(image, strings.get(i), Color.MAGENTA,
                    new Font("Serif", Font.BOLD, 20));
        }
        return self();
    }

    /**
     * 文件名
     *
     * @param name 默认文件名的结果图像时间戳格式: 'yyyy_MM_dd_HH_mm_ss_SSS'.
     *
     * @return t t
     */
    public T withName(String name) {
        if (name != null) {
            fileName = name + "." + EXTENSION.toLowerCase();
        }
        return self();
    }

    /**
     * 生成原始屏幕截图的缩略图。
     * 将保存不同的缩略图取决于它在链中被调用的时间。
     *
     * @param path  the path
     * @param name  the name
     * @param scale the scale
     *
     * @return the t
     */
    public T withThumbnail(Path path, String name, double scale) {
        return withThumbnail(path.toString(), name, scale);
    }

    /**
     * 生成原始屏幕截图的缩略图。
     * 将保存不同的缩略图取决于它在链中被调用的时间。
     *
     * @param path  the path
     * @param name  the name
     * @param scale the scale
     *
     * @return the t
     */
    public T withThumbnail(String path, String name, double scale) {
        File thumbnailFile = new File(path, name);
        if (!Files.exists(Paths.get(path))) {
            thumbnailFile.mkdirs();
        }
        writeImage(ImageProcessor.scale(image, scale), EXTENSION, thumbnailFile);
        return self();
    }

    /**
     * 生成原始屏幕截图的缩略图。
     * 将保存不同的缩略图取决于它在链中被调用的时间。
     *
     * @param scale the scale
     *
     * @return the t
     */
    public T withThumbnail(double scale) {
        return withThumbnail(Paths.get(location.toString(), "./thumbnails").toString(), "thumb_" + fileName, scale);
    }

    /**
     * 写入标题
     *
     * @param title 图像标题
     *
     * @return the t
     */
    public T withTitle(String title) {
        this.titleList.add(title);
        return self();
    }

    /**
     * Self t.
     *
     * @return the t
     */
    protected abstract T self();

    /**
     * 写图片
     *
     * @param imageFile     the image file
     * @param extension     the extension
     * @param fileToWriteTo the file to write to
     */
    private void writeImage(BufferedImage imageFile, String extension, File fileToWriteTo) {
        try {
            ImageIO.write(imageFile, extension, fileToWriteTo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
