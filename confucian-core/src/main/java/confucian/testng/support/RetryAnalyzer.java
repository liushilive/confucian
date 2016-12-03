package confucian.testng.support;

import com.google.common.collect.Maps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.Map;

import confucian.common.Utils;
import confucian.configuration.DefaultBrowserConfig;
import confucian.data.driverConfig.IBrowserConfig;
import confucian.driver.Driver;

/**
 * 用于重新运行失败的测试用例
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<Integer, Integer> countMap = Maps.newHashMap();

    public boolean retry(ITestResult result) {
        IBrowserConfig browserConf = Driver.getBrowserConfig();
        int id = Utils.getId(result);
        if (!countMap.keySet().contains(id))
            countMap.put(id, 0);
        if (null == browserConf)
            browserConf = DefaultBrowserConfig.get();
        int maxCount = browserConf.getRetryFailedTestCaseCount();
        int count = countMap.get(id);
        LOGGER.info("测试用例最大重试次数为: " + maxCount);
        LOGGER.info("当前重试次数: " + count);
        if (count < maxCount) {
            count++;
            countMap.put(id, count);
            return true;
        }
        return false;
    }
}
