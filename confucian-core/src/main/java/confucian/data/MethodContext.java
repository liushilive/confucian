package confucian.data;

import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.IDataProvidable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import confucian.common.Utils;
import confucian.data.DataProvider.MapStrategy;
import confucian.data.csv.CSVApplicationData;
import confucian.data.driverConfig.IBrowserConfig;
import confucian.data.driverConfig.PrepareDriverConfig;
import confucian.data.xml.BrowserXmlParser;
import confucian.data.xml.MappingParserRevisit;
import confucian.data.xml.XmlApplicationData;
import confucian.exception.FrameworkException;
import confucian.testng.support.RetryAnalyzer;


/**
 * 实例化方法上下文
 */
public class MethodContext implements IMethodContext {
    private static MappingParserRevisit mpr = new MappingParserRevisit();
    private static RefineMappedData refinedMappedData = new RefineMappedData(mpr);
    private Logger LOGGER = LogManager.getLogger();
    private boolean afterMethod;
    private boolean beforeMethod;
    private List<IBrowserConfig> browserConfig;
    private String dataProviderPath;
    private DataSource dataSource;
    private String[] groups;
    private boolean isDataSourceCalculated = false;
    private boolean isEnabled;
    private Method method;
    private String methodName;
    private IRetryAnalyzer retryAnalyzer;
    private DataProvider.MapStrategy runStrategy;
    private List<IProperty> testData;

    /**
     * 实例化一个新的方法上下文
     *
     * @param method 方法
     */
    public MethodContext(Method method) {
        this.method = method;
        this.methodName = Utils.getFullMethodName(method);
        setBeforeAfterMethod();
        setIsEnable();
        setGroups();
        setDataProviderPath(refinedMappedData.getMethodData(method).getDataProviderPath());
    }

    @Override
    public List<IBrowserConfig> getBrowserConf() {
        return browserConfig;
    }

    @Override
    public DataSource getDataProvider() {
        return dataSource;
    }

    /**
     * 获取数据源文件
     *
     * @return the data provider path
     */
    @Override
    public String getDataProviderPath() {
        return dataProviderPath;
    }

    /**
     * 设置数据源文件
     *
     * @param dataProviderPath the data provider path
     */
    public void setDataProviderPath(String dataProviderPath) {
        this.dataProviderPath = dataProviderPath;
    }

    /**
     * 获取类/方法所属的组
     *
     * @return string [ ]
     */
    public String[] getGroup() {
        return groups;
    }

    @Override
    public List<IProperty> getMethodTestData() {
        return this.testData;
    }

    public IRetryAnalyzer getRetryAnalyzer() {
        return retryAnalyzer;
    }

    @Override
    public MapStrategy getRunStrategy() {
        return runStrategy;
    }

    /**
     * 设置运行策略
     *
     * @param runStrategy 运行策略
     */
    public void setRunStrategy(MapStrategy runStrategy) {
        this.runStrategy = runStrategy;
    }

    @Override
    public boolean isAfterMethod() {
        return afterMethod;
    }

    @Override
    public boolean isBeforeMethod() {
        return beforeMethod;
    }

    /**
     * 获取类/方法是否启用状态
     *
     * @return boolean boolean
     */
    public boolean isEnable() {
        return isEnabled;
    }

    /**
     * 为测试方法准备browserConf和IProperty
     */
    @Override
    public void prepareData() {
        if (isEnabled) switch (dataSource) {
            case XmlDataBrowser:
                updateXml(System.getProperty("env-type"));
                upBrowserConfig();
                break;
            case CSVDataBrowser:
                updateCSVSheet(System.getProperty("env-type"));
                upBrowserConfig();
                break;
            case NoSourceBrowser:
                upBrowserConfig();
                break;
            case CSVData:
                updateCSVSheet(System.getProperty("env-type"));
                break;
            case XmlData:
                updateXml(System.getProperty("env-type"));
                break;
            case Invalid:
                break;
            default:
                break;
        }
        else {
            LOGGER.debug("方法:" + methodName + " 未启用不需要设置数据");
        }
    }

    /**
     * 设置浏览器配置
     *
     * @param browserConfList 浏览器配置
     */
    public void setBrowserConfig(List<IBrowserConfig> browserConfList) {
        this.browserConfig = browserConfList;
    }

    /**
     * 设置测试数据提供方法
     *
     * @param methodAnnotation 方法注解
     * @param testMethod       测试类
     */
    public void setDataProvider(IDataProvidable methodAnnotation, Method testMethod) {
        if (testMethod.getGenericParameterTypes().length == 2 &&
                testMethod.getGenericParameterTypes()[0].equals(IBrowserConfig.class) &&
                testMethod.getGenericParameterTypes()[1].equals(IProperty.class)) {
            verify_UpdateDataProviderName(methodAnnotation, testMethod);
            verify_UpdateDataProviderClass(methodAnnotation, testMethod);
        } else if (testMethod.getGenericParameterTypes().length == 1 &&
                (testMethod.getGenericParameterTypes()[0].equals(IBrowserConfig.class) ||
                        testMethod.getGenericParameterTypes()[0].equals(IProperty.class))) {
            verify_UpdateDataProviderName(methodAnnotation, testMethod);
            verify_UpdateDataProviderClass(methodAnnotation, testMethod);
        } else if (testMethod.getGenericParameterTypes().length == 0) {
            dataSource = DataSource.Invalid;
        }
    }

    /**
     * 设置失败测试重试
     *
     * @param methodAnnotation the method annotation
     */
    public void setRetryAnalyser(ITestAnnotation methodAnnotation) {
        if (methodAnnotation.getRetryAnalyzer() == null) {
            methodAnnotation.setRetryAnalyzer(RetryAnalyzer.class);
            retryAnalyzer = methodAnnotation.getRetryAnalyzer();
        } else {
            retryAnalyzer = methodAnnotation.getRetryAnalyzer();
        }
        LOGGER.debug("设置失败重试" + methodAnnotation.getRetryAnalyzer() + " 方法: " + methodName);
    }

    /**
     * 设置测试数据
     *
     * @param testData the test data
     */
    public void setTestData(List<IProperty> testData) {
        this.testData = testData;
    }

    /**
     * 更新浏览器配置
     */
    public void upBrowserConfig() {
        IMappingData mapD = refinedMappedData.getMethodData(method);
        BrowserXmlParser bxp = BrowserXmlParser.getInstance();
        this.browserConfig = bxp.getBrowserConfig(mapD);
        this.runStrategy = mapD.getRunStrategy();
    }

    /**
     * 检查注解
     *
     * @param classToCheck       检查类
     * @param annotationToVerify 注解验证类
     * @param <T>                泛型
     */
    private <T extends Annotation> boolean checkAnnotation(Class<?> classToCheck, Class<T> annotationToVerify) {
        if (!classToCheck.getName().contains("java.lang.Object")) {
            for (Method method : classToCheck.getMethods()) {
                if (method.getAnnotation(annotationToVerify) != null) {
                    return true;
                }
            }
            return checkAnnotation(classToCheck.getSuperclass(), annotationToVerify);
        }
        return false;
    }

    /**
     * 获取数据源参数
     */
    private DataSource getDataSourceParameter() {
        if (!isDataSourceCalculated) {
            Map<String, String> tempMap = Maps.newHashMap();
            PrepareDriverConfig configuration = new PrepareDriverConfig(tempMap);
            try {
                dataSource =
                        DataSource.valueOf(configuration.refineBrowserValues().checkForRules().get().getDataSource());
            } catch (Exception exp) {
                throw new FrameworkException("没有为测试方法提供数据源参数: " + methodName, exp);
            }
            isDataSourceCalculated = true;
        }
        return dataSource;
    }

    /**
     * 设置测试前后方法
     */
    private void setBeforeAfterMethod() {
        beforeMethod = checkAnnotation(method.getDeclaringClass(), org.testng.annotations.BeforeMethod.class);
        afterMethod = checkAnnotation(method.getDeclaringClass(), org.testng.annotations.AfterMethod.class);
    }

    /**
     * 设置类/方法所属的组
     */
    private void setGroups() {
        if (null != method.getAnnotation(org.testng.annotations.Test.class))
            groups = method.getAnnotation(org.testng.annotations.Test.class).groups();
    }

    /**
     * 设置是否启用类/方法
     */
    private void setIsEnable() {
        if (null != method.getAnnotation(org.testng.annotations.Test.class))
            isEnabled = method.getAnnotation(org.testng.annotations.Test.class).enabled();
        if (null != method.getAnnotation(org.testng.annotations.Factory.class))
            isEnabled = method.getAnnotation(org.testng.annotations.Factory.class).enabled();
    }

    private void updateCSVSheet(String environment) {
        IMappingData mapD = refinedMappedData.getMethodData(method);
        if (Utils.getResources(mapD.getDataProviderPath()) == null) {
            return;
        }
        if (environment != null && !StringUtils.isBlank(environment)) {
            // 从MappingParser静态方法获取csv名称
            this.testData = CSVApplicationData.getInstance().getAppData(mapD.getTestData(), environment);
        } else {
            this.testData = CSVApplicationData.getInstance().getAppData(mapD.getTestData());
        }
    }

    /**
     * 从XML更新测试数据
     *
     * @param environment 环境
     */
    private void updateXml(String environment) {
        IMappingData mapD = refinedMappedData.getMethodData(method);
        if (Utils.getResources(mapD.getDataProviderPath()) == null) {
            return;
        }
        if (environment != null && !StringUtils.isBlank(environment)) {
            // 从MappingParser静态方法获取xml名称
            this.testData = XmlApplicationData.getInstance().getAppData(mapD.getTestData(), environment);
        } else {
            this.testData = XmlApplicationData.getInstance().getAppData(mapD.getTestData());
        }
    }

    /**
     * 验证数据源
     *
     * @param dataProperty 数据源名称
     */
    private void validateDataProviderName(String dataProperty) {
        try {
            dataSource = DataSource.valueOf(dataProperty);
        } catch (IllegalArgumentException e) {
            throw new FrameworkException("请修复方法中的DataProvider数据源的名称:" + dataProperty + " ，应该是DataSource枚举", e);
        }
    }

    /**
     * 验证和更新数据源提供类
     *
     * @param testAnnotation 方法注解
     * @param testMethod     测试类
     */
    private void verify_UpdateDataProviderClass(IDataProvidable testAnnotation, Method testMethod) {

        if (testAnnotation.getDataProviderClass() != null &&
                StringUtils.isNotBlank(testAnnotation.getDataProviderClass().toString())) {
            if (!testAnnotation.getDataProviderClass().equals(confucian.data.DataProvider.class)) {
                throw new FrameworkException("请修复数据源提供程序类方法:" + testMethod.getName());
            }
        } else {
            testAnnotation.setDataProviderClass(confucian.data.DataProvider.class);
            LOGGER.debug(
                    "设置数据提供程序方法: " + testMethod.getName() + " 值 " + testAnnotation.getDataProviderClass().getName());
        }
    }

    /**
     * 验证和更新数据源名称
     *
     * @param testAnnotation 方法注解
     * @param testMethod     测试方法
     */
    private void verify_UpdateDataProviderName(IDataProvidable testAnnotation, Method testMethod) {
        if (StringUtils.isNotBlank(testAnnotation.getDataProvider())) {
            validateDataProviderName(testAnnotation.getDataProvider());
        } else {
            testAnnotation.setDataProvider(getDataSourceParameter().name());
            dataSource = getDataSourceParameter();
            LOGGER.debug("设置数据提供程序方法: " + testMethod.getName() + " 值: " + testAnnotation.getDataProvider());
        }
    }

}
