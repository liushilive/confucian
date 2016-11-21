package confucian.data.driverConfig;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * 准备浏览器所需功能
 */
@SuppressWarnings("WhileLoopReplaceableByForEach")
public class PrepareDesiredCapability {

    private static final String DESIRED_CAPABILITIES = "desiredCapabilities.";
    private DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
    private Map<String, String> initialDesiredCap = Maps.newHashMap();
    private Properties props;

    /**
     * 实例化一个新的浏览器功能
     *
     * @param props 属性
     */
    PrepareDesiredCapability(Properties props) {
        this.props = props;
        convertToMap();
    }

    /**
     * 实例化一个新的浏览器功能
     *
     * @param desiredCap 功能
     */
    PrepareDesiredCapability(Map<String, String> desiredCap) {
        this.initialDesiredCap.putAll(desiredCap);
    }

    /**
     * 转换属性为映射
     */
    private void convertToMap() {
        Enumeration<?> keys = props.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            initialDesiredCap.put(key, (String) props.get(key));
        }
    }

    /**
     * 从属性映射 {@link DesiredCapabilities}
     *
     * @return 功能
     */
    public DesiredCapabilities get() {
        for (String key : initialDesiredCap.keySet()) {
            KeyValueHolder kv = getKey(key);
            if (kv != null && StringUtils.isNotBlank(kv.value)) {
                desiredCapabilities.setCapability(kv.key, kv.value);
            }
        }
        return desiredCapabilities;
    }

    private KeyValueHolder getKey(String key) {
        if (key.startsWith(DESIRED_CAPABILITIES) && key.length() > 3) {
            return new KeyValueHolder(key.substring(3), initialDesiredCap.get(key));
        }
        return null;
    }

    /**
     * 实例化一个新的键值对
     */
    private class KeyValueHolder {
        /**
         * Key
         */
        String key;
        /**
         * Value
         */
        String value;

        /**
         * 实例化一个新的键值对
         *
         * @param key   键
         * @param value 值
         */
        KeyValueHolder(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

}