package confucian.testng.support;

import com.google.common.collect.Lists;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import confucian.common.Utils;

import static com.google.common.collect.Sets.newHashSet;
import static confucian.common.Utils.getId;

/**
 * 拦截器
 */
public class TestInterceptor extends TestListenerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();

    public void onTestStart(ITestResult arg0) {
        LOGGER.info(
                "****************************************************************************************************");
        LOGGER.info("启动测试用例::" + arg0.getMethod().getDescription() + " (" + arg0.getMethod().getId() + ") " + " (" +
                Utils.getId(arg0) + ") ");
        LOGGER.info("启动时间::" + getTimeReport());
    }

    public void onTestSuccess(ITestResult arg0) {
        LOGGER.info("结束时间::" + getTimeReport());
        long ms = arg0.getEndMillis() - arg0.getStartMillis();
        LOGGER.info("执行时间:: " + ms / 1000 + "." + ms % 1000 + " 秒");
        LOGGER.info("完成的测试用例:: " + arg0.getMethod().getDescription() + " (" + arg0.getMethod().getId() + ") " +
                " => 状态：通过" + " (" + Utils.getId(arg0) + ") ");
        LOGGER.info(
                "****************************************************************************************************");
    }

    public void onTestFailure(ITestResult arg0) {
        LOGGER.info("结束时间::" + getTimeReport());
        long ms = arg0.getEndMillis() - arg0.getStartMillis();
        LOGGER.info("执行时间:: " + ms / 1000 + "." + ms % 1000 + " 秒");
        LOGGER.info("完成的测试用例:: " + arg0.getMethod().getDescription() + " (" + arg0.getMethod().getId() + ") " +
                " => 状态：失败" + " (" + Utils.getId(arg0) + ") ");
        LOGGER.info(
                "****************************************************************************************************");
    }

    public void onTestSkipped(ITestResult result) {
        LOGGER.info("跳过测试用例：" + result.getMethod().getDescription() + " (" + result.getMethod().getId() + ") " + " (" +
                Utils.getId(result) + ") ");
        LOGGER.info(
                "****************************************************************************************************");
    }

    public void onStart(ITestContext context) {
        LOGGER.info("启动套件::" + context.getSuite().getName());
    }

    public void onFinish(ITestContext context) {
        List<ITestResult> testsToBeRemoved = Lists.newArrayList();
        Set<Integer> passedTest = newHashSet();
        // 创建passTest列表
        passedTest.addAll(context.getPassedTests().getAllResults().stream().map(Utils::getId)
                .collect(Collectors.toList()));

        Set<Integer> failedTestID = newHashSet();
        // 创建失败的测试用例列表和列表，重复的测试用例将被删除
        for (ITestResult failTest : context.getFailedTests().getAllResults()) {
            int failTestID = getId(failTest);
            if (failedTestID.contains(failTestID) || passedTest.contains(failTestID)) {
                testsToBeRemoved.add(failTest);
            } else {
                failedTestID.add(failTestID);
            }
        }

        // 更新上下文
        context.getFailedTests().getAllResults().removeIf(testsToBeRemoved::contains);
    }

    /**
     * 获取本地时间
     *
     * @return 本地时间
     */
    private String getTimeReport() {
        Calendar calendar = new GregorianCalendar();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        return " " + hour + ":" + minutes + ":" + seconds;
    }
}
