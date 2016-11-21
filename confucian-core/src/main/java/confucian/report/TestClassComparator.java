package confucian.report;

import org.testng.IClass;

import java.util.Comparator;

/**
 * 按完全限定名称按字母顺序排序的比较器。
 */
class TestClassComparator implements Comparator<IClass> {
    public int compare(IClass class1, IClass class2) {
        return class1.getName().compareTo(class2.getName());
    }
}
