package confucian.data.driverConfig;

import com.google.common.collect.Maps;

import java.util.Map;

import confucian.data.DriverConfigurations;

/**
 * 驱动配置
 */
public class PrepareDriverConfig {
    /**
     * 框架配置文件名称
     */
    private static final String FILENAME = "Framework.properties";

    /**
     * 浏览器配置
     */
    private RefinedBrowserConfig rbc;
    private Map<String, String> refinedBrowserConfig = Maps.newHashMap();

    /**
     * 实例化一个新的驱动
     */
    public PrepareDriverConfig() {
        this(Maps.newHashMap());
    }

    /**
     * 实例化一个新的驱动
     *
     * @param clientBrowserConf 客户端浏览器配置
     */
    public PrepareDriverConfig(Map<String, String> clientBrowserConf) {
        rbc = new RefinedBrowserConfig(clientBrowserConf, FILENAME);

    }

    /**
     * 检查规则准备驱动程序配置
     *
     * @return 准备驱动配置
     */
    public PrepareDriverConfig checkForRules() {
        ValidateBrowserRules vbr = new ValidateBrowserRules(refinedBrowserConfig);
        vbr.validate();
        return this;

    }

    /**
     * 获取浏览器配置
     *
     * @return 浏览器配置
     */
    public IBrowserConfig get() {
        return new BrowserConfig(refinedBrowserConfig, rbc.getDesiredCapabilities());
    }

    /**
     * 优化浏览器的值准备驱动配置
     *
     * @return 准备驱动配置
     */
    public PrepareDriverConfig refineBrowserValues() {
        for (DriverConfigurations.LocalEnvironmentConfig localConfig : DriverConfigurations.LocalEnvironmentConfig
                .values())
            updateRefinedMap(localConfig.toString(), rbc.get(localConfig.toString(), localConfig.get()));

        for (DriverConfigurations.HubConfig hubConfig : DriverConfigurations.HubConfig.values())
            updateRefinedMap(hubConfig.toString(), rbc.get(hubConfig.toString(), hubConfig.get()));

        for (DriverConfigurations.FrameworkConfig frameworkConfig : DriverConfigurations.FrameworkConfig.values())
            updateRefinedMap(frameworkConfig.toString(), rbc.get(frameworkConfig.toString(), frameworkConfig.get()));

        for (DriverConfigurations.FrameworkConfig frameworkConfig : DriverConfigurations.FrameworkConfig.values())
            updateRefinedMap(frameworkConfig.toString(), rbc.get(frameworkConfig.toString(), frameworkConfig.get()));
        return this;
    }

    private void updateRefinedMap(String key, String refinedValue) {
        refinedBrowserConfig.put(key, refinedValue);
    }

}
