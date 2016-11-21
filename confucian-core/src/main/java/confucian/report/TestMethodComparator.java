package confucian.report;

import org.testng.ITestNGMethod;

import java.util.Comparator;

/**
 * 用于排序TestNG测试方法的比较器。 按字母顺序排序方法
 * （首先是完全限定类名，然后是方法名）。
 */
class TestMethodComparator implements Comparator<ITestNGMethod> {
    public int compare(ITestNGMethod method1, ITestNGMethod method2) {
        int compare = method1.getRealClass().getName().compareTo(method2.getRealClass().getName());
        if (compare == 0) {
            compare = method1.getMethodName().compareTo(method2.getMethodName());
        }
        return compare;
    }
}
