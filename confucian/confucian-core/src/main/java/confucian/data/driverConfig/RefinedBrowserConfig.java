package confucian.data.driverConfig;

import confucian.common.Utils;
import confucian.data.PropertyValueMin;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * 基于层次结构改进浏览器配置，优先级：
 * default < Framework < ClientEnv < CommandLine
 */
public class RefinedBrowserConfig {

    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * 浏览器数据
     */
    private final Map<String, String> clientBrowserData;
    /**
     * 框架配置文件
     */
    private final String fileName;
    /**
     * 框架属性数据
     */
    private PropertyValueMin frameworkPropData = null;
    /**
     * 是否已经赋值框架属性
     */
    private boolean isFrameworkProperties;

    /**
     * 实例化一个新的浏览器配置
     *
     * @param clientBrowserData     客户端浏览器数据
     * @param standByLookUpFileName 查找文件名
     */
    RefinedBrowserConfig(Map<String, String> clientBrowserData, String standByLookUpFileName) {
        this.clientBrowserData = clientBrowserData;
        this.fileName = standByLookUpFileName;
        setFrameworkProperties(standByLookUpFileName);
    }

    /**
     * 配置框架属性
     *
     * @param standByLookUpFileName 查找文件名
     */
    private void setFrameworkProperties(String standByLookUpFileName) {
        String pathOfFile = Utils.getResources(standByLookUpFileName);
        if (null == pathOfFile) {
            isFrameworkProperties = false;
        } else {
            isFrameworkProperties = true;
            frameworkPropData = new PropertyValueMin(Utils.getResources(standByLookUpFileName));
        }
    }

    /**
     * 获取字符串，优先级：
     * default < Framework < ClientEnv < CommandLine
     *
     * @param key          key
     * @param defaultValue 默认值
     * @return string string
     */
    public String get(String key, String defaultValue) {
        String refinedValue = defaultValue;
        refinedValue = getFromFrameworkProp(key, refinedValue);
        refinedValue = getFromClientEnv(key, refinedValue);
        refinedValue = getFromJvmArgs(key, refinedValue);
        return refinedValue;
    }

    /**
     * 从框架配置文件获取键值对
     *
     * @param key   key
     * @param value value
     * @return key-value
     */
    private String getFromFrameworkProp(String key, String value) {
        if (isFrameworkProperties) {
            String tempValue = frameworkPropData.getValue(key);
            if (isNotBlank(tempValue)) {
                return tempValue;
            }
        }
        return value;
    }

    /**
     * 判断是否为空
     *
     * @param value value
     * @return bool
     */
    private boolean isNotBlank(String value) {
        return StringUtils.isNotBlank(value);
    }

    /**
     * 从客户端环境获取键值对
     *
     * @param key   key
     * @param value value
     * @return key-value
     */
    private String getFromClientEnv(String key, String value) {
        String tempValue = clientBrowserData.get(key.toLowerCase().trim());
        if (isNotBlank(tempValue)) {
            return tempValue;
        }
        return value;
    }

    /**
     * 从命令行获取键值对
     *
     * @param key   key
     * @param value value
     * @return key-value
     */
    private String getFromJvmArgs(String key, String value) {
        String tempValue = System.getProperty(key);
        return isNotBlank(tempValue) ?
                tempValue :
                value;
    }

    /**
     * 获取所需的功能
     *
     * @return 功能 desired capabilities
     */
    DesiredCapabilities getDesiredCapabilities() {
        DesiredCapabilities dc = new DesiredCapabilities();
        if (isFrameworkProperties) {
            dc.merge(getDesiredCapabilitiesFrameworkProp());
        }
        dc.merge(getDCClient());
        dc.merge(getDCJvm());
        return dc;
    }

    /**
     * 从配置文件获取浏览器配置
     *
     * @return 浏览器配置
     */
    private DesiredCapabilities getDesiredCapabilitiesFrameworkProp() {
        if (isFrameworkProperties) {
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream(new File(Utils.getResources(fileName))));
            } catch (IOException e) {
                LOGGER.error(e);
            }
            PrepareDesiredCapability pdcFrameworkProperties = new PrepareDesiredCapability(prop);
            return pdcFrameworkProperties.get();
        }
        return null;
    }

    /**
     * 从命令行获取浏览器配置
     *
     * @return 浏览器配置
     */
    private DesiredCapabilities getDCJvm() {
        PrepareDesiredCapability systemCapability = new PrepareDesiredCapability(System.getProperties());
        return systemCapability.get();
    }

    /**
     * 从客户端配置获取浏览器配置
     *
     * @return 浏览器配置
     */
    private DesiredCapabilities getDCClient() {
        PrepareDesiredCapability clientBrowserCapability = new PrepareDesiredCapability(clientBrowserData);
        return clientBrowserCapability.get();
    }

}
