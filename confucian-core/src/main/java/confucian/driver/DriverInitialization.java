package confucian.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.File;
import java.util.UUID;

import confucian.common.Utils;
import confucian.data.IMethodContext;
import confucian.data.IProperty;
import confucian.data.driverConfig.IBrowserConfig;
import confucian.exception.FrameworkException;
import confucian.testng.support.HtmlTable;
import confucian.testng.support.MethodContextCollection;
import confucian.testng.support.SAssert;

/**
 * 初始化现场局部变量，清理浏览器驱动。生成报告，屏幕截图等功能
 */
public class DriverInitialization implements IInvokedMethodListener {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * 输出路径：当前正在使用TestNg SAssert
     */
    public static String outPutDir;

    /**
     * 设置驱动程序，{@link org.testng.annotations.BeforeMethod} 配置,或 {@link org.testng.annotations.Test}
     * 配置
     */
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        // 设置输出路径
        if (outPutDir == null) {
            outPutDir = testResult.getTestContext().getOutputDirectory();
        }
        if (method.getTestMethod().isBeforeMethodConfiguration()) {
            LOGGER.debug("设置WebDriver 在 BeforeMethod");
            // 初始化浏览器，以便所有的子线程相同
            Driver.browserConfig.set(null);
        }

        if (method.isTestMethod()) {
            //如果使用工厂类，则不需要browserConfig设置为null，应注意清理
            if (isPartOfFactoryTest(method)) {
                Driver.browserConfig.set(null);
            }
            // 需要设置，否则会产生意外的输出
            SAssert.assertMap.get();
            SAssert.m_errors.get();
        }
    }

    /**
     * 退出驱动程序并生成报告，如果 {@link org.testng.annotations.BeforeMethod}不存在，
     * {@link org.testng.annotations.AfterMethod} 存在，测试类将在 Driver.getDriver() 后清空
     */
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            publishHtmlTable(testResult);
            if (isPartOfFactoryTest(method)) {
                IMethodContext methodContext = MethodContextCollection.getMethodContext(
                        Utils.getFullMethodName(method.getTestMethod().getConstructorOrMethod().getMethod()));
                boolean beforeMethodPresent = methodContext.isBeforeMethod();
                boolean afterMethodShouldPresent = methodContext.isAfterMethod();
                addScreenShot(testResult);
                if (!beforeMethodPresent || !afterMethodShouldPresent) {
                    cleanup(method, testResult);
                }
            } else {
                cleanup(method, testResult);
            }
        }

        // 检查AfterMethod，浏览器退出
        if (method.getTestMethod().isAfterMethodConfiguration()) {
            if (Driver.driver.get() != null) {
                cleanup(method, testResult);
            }
        }
    }

    /**
     * 是否为测试工厂模式
     *
     * @param method 方法
     * @return bool
     */
    private boolean isPartOfFactoryTest(IInvokedMethod method) {
        java.lang.reflect.Method testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        int length = testMethod.getGenericParameterTypes().length;
        if (length == 0)
            return false;
        if (length == 1 && testMethod.getGenericParameterTypes()[0].equals(IBrowserConfig.class))
            return true;
        else if (length == 2 && testMethod.getGenericParameterTypes()[0].equals(IBrowserConfig.class) &&
                testMethod.getGenericParameterTypes()[1].equals(IProperty.class))
            return true;
        else
            throw new FrameworkException("测试方法不属于测试工程模式，请检查传入参数类型!");
    }

    /**
     * 清理
     *
     * @param method     方法
     * @param testResult 测试结果
     */
    private void cleanup(IInvokedMethod method, ITestResult testResult) {
        LOGGER.debug("清理浏览器驱动");
        try {
            // Reporter.setCurrentTestResult(testResult);
            LOGGER.debug("退出浏览器驱动方法:" + method.getTestMethod().getMethodName());
            if (!Driver.driverRemovedStatus()) {
                Driver.tearDown();
            }
        } catch (Exception e) {
            LOGGER.warn("捕捉异常在调用后，所以测试结果没有改变", e);
        } finally {
            if (!Driver.driverRemovedStatus()) {
                Driver.tearDown();
            }
            SAssert.m_errors.get().clear();
            SAssert.assertMap.get().clear();
        }
    }

    /**
     * 添加报告数据HTML表
     */
    private void publishHtmlTable(ITestResult testResult) {
        try {
            Reporter.setCurrentTestResult(testResult);
            String report = new HtmlTable(SAssert.assertMap.get(), testResult.getMethod().getDescription()).getTable();
            Reporter.log(" 报告:::" + report + "</br>");
        } catch (Exception e) {
            LOGGER.error("捕捉异常在公共HTML方法：", e);
        }
    }

    /**
     * 添加屏幕截图
     */
    private void addScreenShot(ITestResult testResult) {
        try {
            Reporter.setCurrentTestResult(testResult);
            if (Driver.getBrowserConfig() != null)
                if (Driver.getBrowserConfig().isScreenShotFlag()) {
                    if (testResult.getThrowable() != null) {
                        String throwMessage = (testResult.getThrowable().getMessage() != null) ?
                                testResult.getThrowable().getMessage() :
                                "";
                        if ((testResult.getThrowable().getCause() != null)) {
                            throwMessage += testResult.getThrowable().getCause().toString();
                        }
                        if (!throwMessage.contains("asserts failed")) {
                            String screenShotName = UUID.randomUUID().toString();
                            String filePath = new File(testResult.getTestContext().getOutputDirectory()).getParent() +
                                    File.separator + "image" + File.separator;
                            DriverUtility.takeScreenShot(filePath, screenShotName, throwMessage);
                            // 追加日志中的屏幕截图
                            Reporter.log("测试用例 -" + testResult.getName() + " 由于异常而失败的屏幕截图");
                            Reporter.log("<div style=\"height:400px; width:800px; overflow:scroll;\">" +
                                    "<a href='../image" + "/" + screenShotName + ".png' target='_blank'>" +
                                    "<img style=\"width: auto; height: auto; max-width: 100%;\" src=\"" + "../image" +
                                    "/" + screenShotName + ".png" + "\"></a></div>");
                        }
                    }
                }
        } catch (Exception e) {
            LOGGER.error("添加屏幕截图：", e);
        }
    }
}
