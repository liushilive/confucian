package confucian.testng.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.asserts.Assertion;
import org.testng.asserts.IAssert;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import confucian.driver.Driver;
import confucian.driver.DriverInitialization;
import confucian.driver.DriverUtility;

/**
 * 断言类
 */
public class SAssert extends Assertion {
    /**
     * The constant assertMap.
     */
    public static final ThreadLocal<LinkedHashMap<IAssert, String>> assertMap =
            ThreadLocal.withInitial(LinkedHashMap::new);
    /**
     * The constant m_errors.
     */
    public static final ThreadLocal<LinkedHashMap<AssertionError, IAssert>> m_errors =
            ThreadLocal.withInitial(LinkedHashMap::new);
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 执行所有断言
     */
    public void assertAll() {
        if (!m_errors.get().isEmpty()) {
            StringBuilder sb = new StringBuilder("下面的断言失败:\n");
            boolean first = true;
            for (Map.Entry<AssertionError, IAssert> ae : m_errors.get().entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(ae.getValue().getMessage());
            }
            throw new AssertionError(sb.toString());
        }
    }

    @Override
    public void executeAssert(IAssert assertObj) {
        try {
            assertObj.doAssert();
            assertMap.get().put(assertObj, "true");
        } catch (AssertionError ex) {
            if (Driver.getBrowserConfig() != null && Driver.getBrowserConfig().isScreenShotFlag()) {
                String screenShotName = UUID.randomUUID().toString();
                String screenShotPath =
                        new File(DriverInitialization.outPutDir).getParent() + File.separator + "image" +
                                File.separator;
                DriverUtility
                        .takeScreenShot(screenShotPath, screenShotName, "断言：：  " + assertObj.getMessage() + "  ：：  失败",
                                "预期值： " + assertObj.getExpected(), "实际值： " + assertObj.getActual());
                screenShotPath = "../image" + "/" + screenShotName + ".png";
                assertMap.get().put(assertObj, screenShotPath);
            } else
                assertMap.get().put(assertObj, null);
            m_errors.get().put(ex, assertObj);
        }
    }

    @Override
    public void onAfterAssert(IAssert<?> assertCommand) {
        // LOGGER.info("断言：  " + assertCommand.getMessage() + " - " + assertCommand.getActual());
    }
}
