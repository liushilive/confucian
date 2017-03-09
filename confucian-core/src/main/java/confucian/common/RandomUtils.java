package confucian.common;

import com.google.common.collect.Lists;

import org.apache.http.util.TextUtils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 随机工具类
 */
public interface RandomUtils {
    /**
     * 制定字符串范围的因子：
     * 大写字母
     */
    String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 制定字符串范围的因子：
     * 字母
     */
    String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 制定字符串范围的因子：
     * 小写字母
     */
    String LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    /**
     * 制定字符串范围的因子：
     * 数字
     */
    String NUMBERS = "0123456789";
    /**
     * 制定字符串范围的因子：
     * 数字与字母
     */
    String NUMBERS_AND_LETTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 获取0~max范围的随机int
     *
     * @param max the max
     *
     * @return the random
     */
    static int getRandom(int max) {
        return getRandom(0, max);
    }

    /**
     * 获取min~max范围的随机int
     *
     * @param min the min
     * @param max the max
     *
     * @return the random
     */
    static int getRandom(int min, int max) {
        if (min > max) {
            return 0;
        }
        if (min == max) {
            return min;
        }
        return min + new Random().nextInt(max - min + 1);
    }

    /**
     * 获取制定字符串和长度的随机字符串
     *
     * @param source the source
     * @param length the length
     *
     * @return the random
     */
    static String getRandom(String source, int length) {
        return TextUtils.isEmpty(source) ?
                null :
                getRandom(source.toCharArray(), length);
    }

    /**
     * 获取制定字符数组和长度的随机字符串
     *
     * @param sourceChar the source char
     * @param length     the length
     *
     * @return the random
     */
    static String getRandom(char[] sourceChar, int length) {
        if (sourceChar == null || sourceChar.length == 0 || length < 0) {
            return null;
        }
        StringBuilder str = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            str.append(sourceChar[random.nextInt(sourceChar.length)]);
        }
        return str.toString();
    }

    /**
     * 获取随机布尔值.
     *
     * @return the random bool
     */
    static boolean getRandomBool() {
        return getRandom(1) == 0;
    }

    /**
     * 获取大写字母组成的随机字符串
     *
     * @param length the length
     *
     * @return the random capital letters
     */
    static String getRandomCapitalLetters(int length) {
        return getRandom(CAPITAL_LETTERS, length);
    }

    /**
     * 获取英文字母组成的随机字符串
     *
     * @param length the length
     *
     * @return the random letters
     */
    static String getRandomLetters(int length) {
        return getRandom(LETTERS, length);
    }

    /**
     * 获取min~max范围的指定个数的intList,不重复
     *
     * @param min the min
     * @param max the max
     * @param num the num
     *
     * @return the random
     */
    static List<Integer> getRandomList(int min, int max, int num) {
        List<Integer> list = Lists.newArrayList();

        if (min == max || max - min + 1 < num) {
            list.add(min);
            return list;
        }

        for (int i = 0; i < num; i++) {
            int random;
            do {
                random = getRandom(min, max);
            } while (list.contains(random));
            list.add(random);
        }
        Collections.sort(list);

        return list;
    }

    /**
     * 获取小写字母组成的随机字符串
     *
     * @param length the length
     *
     * @return the random lower case letters
     */
    static String getRandomLowerCaseLetters(int length) {
        return getRandom(LOWER_CASE_LETTERS, length);
    }

    /**
     * 获取随机数字字符串
     *
     * @param length the length
     *
     * @return the random numbers
     */
    static String getRandomNumbers(int length) {
        return getRandom(NUMBERS, length);
    }

    /**
     * 获取随机数字、字母字符串.
     *
     * @param length the length
     *
     * @return the random numbers and letters
     */
    static String getRandomNumbersAndLetters(int length) {
        return getRandom(NUMBERS_AND_LETTERS, length);
    }
}