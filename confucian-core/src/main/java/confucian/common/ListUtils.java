package confucian.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * List公共类
 */
public class ListUtils {
    /**
     * 以HashMap中的一个值为索引，对ArrayList进行降序排列
     *
     * @param list the list
     * @param name the name
     */
    public static void descSort(List<HashMap<String, String>> list, String name) {
        Collections.sort(list, (arg0, arg1) -> arg1.get(name).compareTo(arg0.get(name)));
    }

    /**
     * 以HashMap中的多个值为索引，对ArrayList进行降序排列
     *
     * @param list     the list
     * @param nameList the name list
     */
    public static void descSort(List<HashMap<String, String>> list, String[] nameList) {
        Collections.sort(list, (arg0, arg1) -> {
            int result = 0;
            for (String aNameList : nameList) {
                result = arg1.get(aNameList).compareTo(arg0.get(aNameList));
                if (result != 0) {
                    break;
                }
            }
            return result;
        });
    }

    /**
     * 以HashMap中的多个值为索引，对ArrayList进行升序排列
     *
     * @param list     the list
     * @param nameList the name list
     */
    public static void sort(List<HashMap<String, String>> list, String[] nameList) {
        Collections.sort(list, (arg0, arg1) -> {
            int result = 0;
            for (String aNameList : nameList) {
                result = arg0.get(aNameList).compareTo(arg1.get(aNameList));
                if (result != 0) {
                    break;
                }
            }
            return result;
        });
    }

    /**
     * 以HashMap中的一个值为索引，对ArrayList进行升序排列：
     *
     * @param list the list
     * @param name the name
     */
    public static void sort(List<HashMap<String, String>> list, String name) {
        Collections.sort(list, (arg0, arg1) -> arg0.get(name).compareTo(arg1.get(name)));
    }
}
