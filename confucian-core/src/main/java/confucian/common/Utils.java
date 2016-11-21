package confucian.common;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * 通用辅助操作
 */
public class Utils {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 获取ID，用于自定义报表
     *
     * @param result result
     * @return id
     */
    public static int getId(ITestResult result) {
        int id = result.getTestClass().getName().hashCode();
        id = 31 * id + result.getMethod().getMethodName().hashCode();
        id = 31 * id + result.getMethod().getInstance().hashCode();
        id = 31 * id + (result.getParameters() != null ?
                Arrays.hashCode(result.getParameters()) :
                0);
        return id;
    }

    /**
     * 获取文件的路径字符串.
     *
     * @param fileName 文件名
     * @return 字符串 file path for upload
     */
    public static String getFilePathForUpload(String fileName) {
        LOGGER.debug("文件名:" + fileName);
        return System.getProperty("user.dir") + File.separator + fileName;
    }

    /**
     * 获取完整的方法名称.
     *
     * @param m 方法
     * @return 完整的方法名 full method name
     */
    public static String getFullMethodName(Method m) {
        return m.getDeclaringClass().getName() + "." + m.getName();
    }

    /**
     * 从字符串中获取整数的列表(带符号)
     *
     * @param text 字符串
     * @return 整数列表 integer list from string
     */
    public static List<Integer> getIntegerListFromString(String text) {
        List<Integer> integerList = Lists.newArrayList();
        Matcher matcher = Pattern.compile("(-)?\\d+").matcher(text);

        while (matcher.find()) {
            integerList.add(Integer.valueOf(matcher.group()));
        }
        return integerList;
    }

    /**
     * 从字符串中获取double的列表(带符号)
     *
     * @param text 字符串
     * @return 整数列表 integer list from string
     */
    public static List<Double> getDoubleListFromString(String text) {
        List<Double> integerList = Lists.newArrayList();
        Matcher matcher = Pattern.compile("(-)?[1-9]\\d*\\.\\d*|(-)?0\\.\\d*[1-9]\\d*").matcher(text);

        while (matcher.find()) {
            integerList.add(Double.valueOf(matcher.group()));
        }
        return integerList;
    }

    /**
     * 从字符串中获取字母的列表
     *
     * @param text 字符串
     * @return letter list from string
     */
    public static List<String> getLetterListFromString(String text) {
        List<String> list = Lists.newArrayList();
        Matcher matcher = Pattern.compile("[A-Za-z]+").matcher(text);

        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    /**
     * 从字符串中获取金额
     *
     * @param text the text
     * @return the double
     */
    public static double getMoneyFromString(String text) {
        return getMoneyListFromString(text).size() == 0 ?
                0.0 :
                getMoneyListFromString(text).get(0);
    }

    /**
     * 从字符串中获取金额列表
     *
     * @param text the text
     * @return the double List
     */
    public static List<Double> getMoneyListFromString(String text) {
//        text = text.replace(",", "");
        List<Double> list = Lists.newArrayList();
        // Matcher matcher = Pattern.compile("((-)?(([1-9]\\d*)|([0]))(\\.(\\d){1,10})?)").matcher(text);
        Matcher matcher = Pattern.compile("((-)?[1-9]\\d{0,10}){1}(,[0-9]{3})*(\\.?\\d+)?|((-)?[1-9]\\d*|0)(\\.\\d+)?")
                .matcher(text);

        while (matcher.find()) {
            list.add(Double.valueOf(matcher.group().trim().replace(",", "")));
        }

        return list;
    }

    /**
     * 返回资源文件的完整路径，
     * 如果找不到文件将返回null。
     *
     * @param fileName 文件名
     * @return 资源文件夹路径 resources
     */
    public static String getResources(String fileName) {
        LOGGER.debug("文件名:" + fileName);
        String returnFilePath = null;

        try {
            switch (OSName.get()) {
                case UNIX:
                    returnFilePath = Thread.currentThread().getContextClassLoader().getResource(fileName).getPath();
                    break;
                case WIN:
                    returnFilePath =
                            Thread.currentThread().getContextClassLoader().getResource(fileName).getPath().substring(1)
                                    .replace("%20", " ");
                    break;
                case MAC:
                    returnFilePath = Thread.currentThread().getContextClassLoader().getResource(fileName).getPath();
                    break;
                default:
                    break;
            }
            returnFilePath = java.net.URLDecoder.decode(returnFilePath, "utf-8");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("文件路径:" + returnFilePath);
            }
            return returnFilePath;
        } catch (NullPointerException | UnsupportedEncodingException e) {
            LOGGER.warn(Thread.currentThread().hashCode() + "---- 无法找到文件:" + fileName + "。 返回 null");
        }
        return null;
    }

    /**
     * 从文件读取数据，返回String
     *
     * @param filePath the file path
     * @return the resources of file
     */
    public static String getResourcesOfFile(String filePath) {
        try {
            return CharStreams.toString(
                    new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath),
                            Charsets.UTF_8));
        } catch (IOException e) {
            LOGGER.warn("读取文件失败", e);
            return null;
        }
    }

    /**
     * 分割字符串
     *
     * @param commaSeparatedList 需要分割的字符串
     * @param string             the string
     * @return 分割后的字符串列表 split list
     */
    public static List<String> getSplitList(String commaSeparatedList, String string) {
        Iterable<String> split = Splitter.on(string).omitEmptyStrings().trimResults().split(commaSeparatedList);
        return Lists.newArrayList(split);
    }

    /**
     * 返回子串附有唯一的字符串UUID
     *
     * @param text      字符串
     * @param charCount 从0开始计数的字符长度
     * @return 字符串 unique name
     */
    public static String getUniqueName(String text, int charCount) {
        return (text + UUID.randomUUID()).substring(0, charCount);
    }

    /**
     * 返回文本附有唯一的字符串UUID
     *
     * @param text 字符串
     * @return 字符串 unique name
     */
    public static String getUniqueName(String text) {
        return text + UUID.randomUUID();
    }

    /**
     * 写图片
     *
     * @param imageFile     the image file
     * @param extension     the extension
     * @param fileToWriteTo the file to write to
     */
    public static void writeImage(BufferedImage imageFile, String extension, File fileToWriteTo) {
        try {
            ImageIO.write(imageFile, extension, fileToWriteTo);
        } catch (IOException e) {
            LOGGER.warn("写入图片失败", e);
        }
    }
}
