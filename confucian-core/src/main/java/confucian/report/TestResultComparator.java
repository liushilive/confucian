package confucian.report;

import org.testng.ITestResult;

import java.util.Comparator;

/**
 * 用于按测试执行顺序排序TestNG测试结果的比较器。
 */
class TestResultComparator implements Comparator<ITestResult> {
    public int compare(ITestResult result1, ITestResult result2) {
        // return result1.getName().compareTo(result2.getName());
        int result3;
        if (result1.getStartMillis() < result2.getStartMillis())
            result3 = -1;
        else
            result3 = 1;
        return result3;
    }
}
