package confucian.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.Set;

import confucian.common.Utils;
import confucian.data.driverConfig.IBrowserConfig;
import confucian.exception.FrameworkException;
import confucian.testng.support.MethodContextCollection;

/**
 * 为 @Test 方法提供数据
 */
public class DataProvider {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * CSV数据源,无浏览器配置
     *
     * @param m 测试方法
     *
     * @return 对象列表 object [ ] [ ]
     */
    @org.testng.annotations.DataProvider(name = "CSVData", parallel = true)
    public static Object[][] csvData(Method m) {
        return getDataProvider(m);
    }

    /**
     * CSV数据源,带浏览器配置
     *
     * @param m 测试方法
     *
     * @return 对象列表 object [ ] [ ]
     */
    @org.testng.annotations.DataProvider(name = "CSVDataBrowser", parallel = true)
    public static Object[][] csvDataBrowser(Method m) {
        return getDataProvider(m);
    }

    /**
     * 无数据源带浏览器配置
     *
     * @param m 测试方法
     *
     * @return 对象列表 object [ ] [ ]
     */
    @org.testng.annotations.DataProvider(name = "NoSourceBrowser", parallel = true)
    public static Object[][] noSourceBrowser(Method m) {
        return getDataProvider(m);
    }

    /**
     * XML数据源,无浏览器配置
     *
     * @param m 测试方法
     *
     * @return 对象列表 object [ ] [ ]
     */
    @org.testng.annotations.DataProvider(name = "XmlData", parallel = true)
    public static Object[][] xmlData(Method m) {
        return getDataProvider(m);
    }

    /**
     * XML数据源,带浏览器配置
     *
     * @param m 测试方法
     *
     * @return 对象列表 object [ ] [ ]
     */
    @org.testng.annotations.DataProvider(name = "XmlDataBrowser", parallel = true)
    public static Object[][] xmlDataBrowser(Method m) {
        return getDataProvider(m);
    }

    /**
     * 过滤相同的浏览器对象
     *
     * @param fullBrowserList 完整的浏览器列表
     *
     * @return 浏览器对象列表
     */
    private static List<IBrowserConfig> filterSameBrowsers(List<IBrowserConfig> fullBrowserList) {
        Set<IBrowserConfig> browserConfSet = Sets.newHashSet(fullBrowserList);
        return Lists.newArrayList(browserConfSet);
    }

    /**
     * 基于 {@link MapStrategy} 删除重复的浏览器和准备数据
     *
     * @param methodName 方法名称
     *
     * @return Object[][]
     */
    private static Object[][] getData(String methodName) {
        Object[][] testMethodData = null;
        IMethodContext methodContext = MethodContextCollection.getMethodContext(methodName);
        List<IBrowserConfig> fullBrowserList = methodContext.getBrowserConf();
        List<IProperty> testMData = methodContext.getMethodTestData();
        MapStrategy strategy = methodContext.getRunStrategy();

        switch (methodContext.getDataProvider()) {
            case CSVData:
            case XmlData: {
                int testDataCount = testMData.size();
                verifyCount(testDataCount, "IProperty");
                testMethodData = new Object[testMData.size()][1];
                for (int i = 0; i < testMData.size(); i++) {
                    testMethodData[i][0] = testMData.get(i);
                }
            }
            break;
            case CSVDataBrowser:
            case XmlDataBrowser: {
                List<IBrowserConfig> browserConfFilteredList = filterSameBrowsers(fullBrowserList);
                int browserConfCount = browserConfFilteredList.size();
                int testDataCount = testMData.size();
                verifyCount(browserConfCount, "IBrowserConfig");
                verifyCount(testDataCount, "IProperty");

                int loopCombination;
                switch (strategy) {
                    case Full:
                        loopCombination = browserConfCount * testDataCount;
                        testMethodData = new Object[loopCombination][2];
                        int k = 0;
                        for (IBrowserConfig aBrowserConfFilteredList : browserConfFilteredList) {
                            for (IProperty aTestMData : testMData) {
                                testMethodData[k][0] = aBrowserConfFilteredList;
                                testMethodData[k][1] = aTestMData;
                                k++;
                            }
                        }
                        break;
                    case Optimal:
                        if (browserConfCount >= testDataCount) {
                            loopCombination = browserConfCount;
                        } else {
                            loopCombination = testDataCount;
                        }
                        testMethodData = new Object[loopCombination][2];
                        for (int i = 0; i < loopCombination; i++) {
                            Random r = new Random();
                            if (i >= browserConfCount) {
                                testMethodData[i][0] = browserConfFilteredList.get(r.nextInt(browserConfCount));
                            } else {
                                testMethodData[i][0] = browserConfFilteredList.get(i);
                            }
                            if (i >= testDataCount) {
                                testMethodData[i][1] = testMData.get(r.nextInt(testDataCount));
                            } else {
                                testMethodData[i][1] = testMData.get(i);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            break;
            case NoSourceBrowser: {
                List<IBrowserConfig> browserConfFilteredList = filterSameBrowsers(fullBrowserList);
                int browserConfCount = browserConfFilteredList.size();
                verifyCount(browserConfCount, "IBrowserConfig");
                testMethodData = new Object[browserConfCount][1];
                for (int i = 0; i < browserConfFilteredList.size(); i++)
                    testMethodData[i][0] = browserConfFilteredList.get(i);
            }
            break;
            case Invalid:
                break;
            default:
                break;
        }
        return testMethodData;
    }

    /**
     * 获取数据源对象
     *
     * @param m 方法名称
     *
     * @return 对象列表
     */
    private static Object[][] getDataProvider(Method m) {
        String methodName = Utils.getFullMethodName(m);
        IMethodContext methodContext = MethodContextCollection.getMethodContext(methodName);
        if (methodContext.getMethodTestData() == null &&
                Utils.getResources(methodContext.getDataProviderPath()) != null) {
            methodContext.prepareData();
        }
        return getData(methodName);
    }

    /**
     * 异常
     *
     * @param count    计数
     * @param dataName 数据类型名称
     */
    private static void verifyCount(int count, String dataName) {
        if (count <= 0) {
            throw new FrameworkException("数据提供类型:" + dataName + "不存在!");
        }
    }

    /**
     * 枚举策略
     */
    enum MapStrategy {
        /**
         * 全
         */
        Full, /**
         * 最佳
         */
        Optimal
    }

}
