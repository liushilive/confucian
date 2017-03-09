package confucian.configuration;

import com.google.common.collect.Maps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

import confucian.data.DriverConfigurations;
import confucian.data.IProperty;
import confucian.data.driverConfig.IBrowserConfig;
import confucian.data.driverConfig.PrepareDriverConfig;

/**
 * 从命令行或属性文件配置驱动程序所需的变量，
 * 命令行优先
 */
public class DefaultBrowserConfig {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();
    private static IBrowserConfig browserConf;
    private static IProperty customProp;

    private DefaultBrowserConfig() {
    }

    /**
     * 返回 {@link IBrowserConfig} 取决于 {@link CustomBrowserConf} 是否使用默认值
     *
     * @return 浏览器配置
     */
    public static IBrowserConfig get() {
        if (browserConf == null) {
            synchronized (DefaultBrowserConfig.class) {
                if (browserConf == null) {
                    setEscapePropertyForReportNG();
                    if (customProp == null) {
                        browserConf = new PrepareDriverConfig().refineBrowserValues().checkForRules().get();
                    } else {
                        browserConf =
                                new PrepareDriverConfig(getKeyValue()).refineBrowserValues().checkForRules().get();
                    }
                }
            }
        }
        return browserConf;
    }

    /**
     * 删除浏览器默认配置，设置自定义属性
     *
     * @param prop 自定义属性
     */
    static void loadCustomPropertiesFile(IProperty prop) {
        browserConf = null;
        customProp = prop;
    }

    /**
     * 获取自定义属性
     *
     * @return 自定义属性
     */
    private static HashMap<String, String> getKeyValue() {
        HashMap<String, String> f_map = Maps.newHashMap();
        for (DriverConfigurations.LocalEnvironmentConfig localConfig : DriverConfigurations.LocalEnvironmentConfig
                .values()) {
            f_map.put(localConfig.toString(), customProp.getValue(localConfig));
        }
        for (DriverConfigurations.HubConfig hubConfig : DriverConfigurations.HubConfig.values()) {
            f_map.put(hubConfig.toString(), customProp.getValue(hubConfig));
        }
        for (DriverConfigurations.FrameworkConfig frameworkConfig : DriverConfigurations.FrameworkConfig.values()) {
            f_map.put(frameworkConfig.toString(), customProp.getValue(frameworkConfig));
        }
        return f_map;
    }

    /**
     * 设置ReportNG
     */
    private static void setEscapePropertyForReportNG() {
        System.setProperty("confucian.report.coverage-report", "true");
        final String ESCAPE_PROPERTY = "confucian.report.escape-output";
        System.setProperty(ESCAPE_PROPERTY, "false");
    }
}
