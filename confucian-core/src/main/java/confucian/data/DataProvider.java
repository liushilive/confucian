package confucian.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import confucian.common.Utils;
import confucian.data.driverConfig.IBrowserConfig;
import confucian.exception.FrameworkException;
import confucian.testng.support.MethodContextCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 为 @Test 方法提供数据
 */
public class DataProvider {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Excel数据源
     *
     * @param m 测试方法
     * @return 对象列表 object [ ] [ ]
     */
    /*@org.testng.annotations.DataProvider(name = "Excel", parallel = true)
    public static Object[][] excelDataProvider(Method m) {
        String methodName = Utils.getFullMethodName(m);
        return getData(methodName);
    }*/

    /**
     * 无数据源
     *
     * @param m 测试方法
     * @return 对象列表 object [ ] [ ]
     */
    @org.testng.annotations.DataProvider(name = "NoSource", parallel = true)
    public static Object[][] noDataProvider(Method m) {
        return getDataProvider(m);
    }

    /**
     * 获取数据源对象
     *
     * @param m 方法名称
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
     * 基于 {@link MapStrategy} 删除重复的浏览器和准备数据
     *
     * @param methodName 方法名称
     * @return Object[][]
     */
    private static Object[][] getData(String methodName) {
        Object[][] testMethodData = null;
        List<IBrowserConfig> browserConfFilteredList =
                filterSameBrowsers(MethodContextCollection.getMethodContext(methodName).
                        getBrowserConf());
        List<IProperty> testMData = MethodContextCollection.getMethodContext(methodName).getMethodTestData();
        MapStrategy strategy = MethodContextCollection.getMethodContext(methodName).getRunStrategy();
        int browserConfCount = browserConfFilteredList.size();
        verifyCount(browserConfCount, "IBrowserConfig");
        int testDataCount =
                MethodContextCollection.getMethodContext(methodName).getDataProvider().equals(DataSource.NoSource) ?
                        -1 :
                        testMData.size();
        if (testDataCount == -1) {
            testMethodData = new Object[browserConfCount][1];
            int k = 0;
            for (IBrowserConfig aBrowserConfFilteredList : browserConfFilteredList) {
                testMethodData[k][0] = aBrowserConfFilteredList;
                k++;
            }
            return testMethodData;
        }
        verifyCount(testDataCount, "IProperty");
        int loopCombination;
        int k = 0;
        switch (strategy) {
            case Full:
                loopCombination = browserConfCount * testDataCount;
                testMethodData = new Object[loopCombination][2];

                for (IBrowserConfig aBrowserConfFilteredList : browserConfFilteredList) {
                    for (IProperty aTestMData : testMData) {
                        testMethodData[k][0] = aBrowserConfFilteredList;
                        testMethodData[k][1] = aTestMData;
                        k++;
                    }
                }
                break;
            case Optimal:
            /*if(testDataCount <=0)
                testDataCount = 1;*/
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
        return testMethodData;
    }

    /**
     * 过滤相同的浏览器对象
     *
     * @param fullBrowserList 完整的浏览器列表
     * @return 浏览器对象列表
     */
    private static List<IBrowserConfig> filterSameBrowsers(List<IBrowserConfig> fullBrowserList) {
        Set<IBrowserConfig> browserConfSet = Sets.newHashSet(fullBrowserList);
        return Lists.newArrayList(browserConfSet);
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
     * XML数据源
     *
     * @param m 测试方法
     * @return 对象列表 object [ ] [ ]
     */
    @org.testng.annotations.DataProvider(name = "XmlData", parallel = true)
    public static Object[][] xmlDataProvider(Method m) {
        return getDataProvider(m);
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
