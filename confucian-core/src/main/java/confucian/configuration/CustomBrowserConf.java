package confucian.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import confucian.data.IProperty;
import confucian.data.PropertyMapping;
import confucian.data.driverConfig.IBrowserConfig;

/**
 * 自定义初始化 {@link IBrowserConfig}
 */
public interface CustomBrowserConf {
    Logger LOGGER = LogManager.getLogger();

    /**
     * 加载自定义属性文件并初始化 {@link DefaultBrowserConfig} 参数
     *
     * @param filePath 新的属性文件完整路径
     */
    static void loadPropFile(String filePath) {
        LOGGER.info("加载新的属性文件:" + filePath);
        IProperty prop = new PropertyMapping(filePath);
        DefaultBrowserConfig.loadCustomPropertiesFile(prop);
    }

    /**
     * 加载属性文件
     *
     * @param prop 属性接口
     */
    static void loadPropFile(IProperty prop) {
        LOGGER.info("加载新的属性：IProperty");
        DefaultBrowserConfig.loadCustomPropertiesFile(prop);
    }
}
