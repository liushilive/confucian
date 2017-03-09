package confucian.data;

import com.google.common.collect.Maps;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * 最小属性值
 */
public class PropertyValueMin {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * 文件输入流
     */
    private FileInputStream fis;
    /**
     * 属性值
     */
    private Map<String, String> propertiesValue = Collections.synchronizedMap(Maps.newHashMap());

    @SuppressWarnings("unused")
    private PropertyValueMin() {
    }

    /**
     * 实例化一个新的最小属性值
     *
     * @param filePath 文件路径
     */
    public PropertyValueMin(String filePath) {
        try {
            fis = new FileInputStream(new File(filePath));
            Properties prop = new Properties();
            prop.load(fis);
            fis.close();
            createHashMap(prop);
        } catch (IOException | NullPointerException e) {
            LOGGER.error(e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * 获取值
     *
     * @param <E> 类型参数
     * @param key bs_key
     *
     * @return 值 value
     */
    public <E extends Enum<E>> String getValue(E key) {
        try {
            String value = propertiesValue.get(key.toString());
            if (value == null) {
                throw new NullPointerException();
            }
            return value;
        } catch (NullPointerException e) {
            LOGGER.error("在数据文件中没有指定的 Key = " + key, e);
            return null;
        }
    }

    /**
     * 返回key的值
     *
     * @param key the key
     *
     * @return value
     */
    public String getValue(String key) {
        return propertiesValue.get(key);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("<script language='JavaScript'>");
        sb.append("function change(text){testData.innerHTML=text}");
        sb.append("</script>");

        StringBuilder newSb = new StringBuilder();
        propertiesValue.forEach((key, value) -> newSb.append(key).append(":").append(value).append("<br>"));
        sb.append("<a href='Data' onmouseover=\"javascript:change('").append(newSb.toString())
                .append("')\" onmouseout=\"javascript:change('')\">数据</a>");
        sb.append("<div id='testData'></div>");
        return sb.toString();
    }

    /**
     * 为所有已加载的属性文件创建散列映射
     *
     * @param prop 属性
     */
    private void createHashMap(Properties prop) {
        String key;
        for (Object o : prop.keySet()) {
            key = (String) o;
            propertiesValue.put(key, prop.getProperty(key));
        }
    }

}
