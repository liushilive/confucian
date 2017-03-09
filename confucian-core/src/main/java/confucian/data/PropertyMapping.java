package confucian.data;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import confucian.common.Utils;
import confucian.exception.FrameworkException;

import static com.google.common.collect.Sets.newHashSet;

/**
 * 加载属性文件
 */
public class PropertyMapping implements IProperty {

    private static final Logger LOGGER = LogManager.getLogger();
    private Set<String> classEnumCheck = newHashSet();
    private FileInputStream fis;
    private Properties prop = new Properties();
    private Map<String, String> propertiesValue = Collections.synchronizedMap(Maps.newHashMap());

    /**
     * 实例化一个新的属性映射
     *
     * @param prop 属性
     */
    private PropertyMapping(Properties prop) {
        this.prop = prop;
        createHashMap(prop);
    }

    @SuppressWarnings("unused")
    private PropertyMapping() {
    }

    /**
     * 实例化一个新的属性映射
     *
     * @param filePath 文件路径
     */
    public PropertyMapping(String filePath) {
        loadFile(filePath);
    }

    /**
     * 实例化一个新的属性映射
     *
     * @param filePaths 文件路径s
     */
    private PropertyMapping(String[] filePaths) {
        for (String filePath : filePaths) {
            loadFile(filePath);
        }
    }

    /**
     * 实例化一个新的属性映射
     *
     * @param data the data
     */
    public PropertyMapping(Map<String, String> data) {
        propertiesValue = data;
    }

    public Set<String> getKeys() {
        return propertiesValue.keySet();
    }

    /**
     * 获取Node的Name属性值
     *
     * @param typeName  the type name
     * @param NodesName the nodes name
     *
     * @return Nodes的Name属性值List node name
     */
    public List<String> getNodeName(String typeName, String NodesName) {
        return Utils.getSplitList(getValue(typeName, NodesName, "node"), "->");
    }

    /**
     * 获取Node的Name属性值
     *
     * @param typeName the type name
     *
     * @return Nodes的Name属性值List node name
     */
    public List<String> getNodeName(String typeName) {
        return Utils.getSplitList(getValue(typeName, "node"), "->");
    }

    /**
     * 获取Nodes的Name属性值
     *
     * @param typeName the type name
     *
     * @return Nodes的Name属性值List nodes name
     */
    public List<String> getNodesName(String typeName) {
        return Utils.getSplitList(getValue(typeName, "nodes"), "->");
    }

    /**
     * 获取page的Name属性值
     *
     * @return page的Name属性值List
     */
    public List<String> getPageName() {
        return Utils.getSplitList(getValue("page"), "->");
    }

    /**
     * 获取Type的Name属性值
     *
     * @return Type的Name属性值List
     */
    public List<String> getTypeName() {
        return Utils.getSplitList(getValue("type"), "->");
    }

    /**
     * 返回key的值
     *
     * @param keys keys
     *
     * @return value
     */
    public String getValue(String... keys) {
        Joiner joiner = Joiner.on("->").skipNulls();
        String key = joiner.join(keys);
        try {
            String value = propertiesValue.get(key);
            if (value == null) {
                throw new NullPointerException();
            }
            return value;
        } catch (NullPointerException e) {
            LOGGER.warn("在数据文件中没有指定的 Key = " + key, e);
            return null;
        }
    }

    /**
     * 获取值
     *
     * @param <E> 类型参数
     * @param key 枚举值
     *
     * @return {@code <E extends Enum<E>>} 限定形态参数实例化的对象，只能是Enum
     */
    public <E extends Enum<E>> String getValue(E key) {
        checkEnumMapping(key);
        String value;
        try {
            value = propertiesValue.get(key.toString());
            if (value == null) {
                throw new NullPointerException();
            }
            return value;
        } catch (NullPointerException e) {
            LOGGER.error("在数据文件中没有指定的 Key = " + key, e);
            return null;
        }
    }

    @Override
    public String toString() {
        Integer randomNum = 30000 + (int) (Math.random() * 90000000);

        StringBuilder sb = new StringBuilder();
        sb.append("<script language='JavaScript'>");
        sb.append("function showDiv").append(randomNum).append("() {");
        sb.append("document.getElementById('testData").append(randomNum).append("').style.display = 'block';");
        sb.append("document.getElementById('showData").append(randomNum).append("').style.display = 'none';");
        sb.append("document.getElementById('hideData").append(randomNum).append("').style.display = 'block';}");
        sb.append("function hideDiv").append(randomNum).append("() {");
        sb.append("document.getElementById('testData").append(randomNum).append("').style.display = 'none';");
        sb.append("document.getElementById('showData").append(randomNum).append("').style.display = 'block';");
        sb.append("document.getElementById('hideData").append(randomNum).append("').style.display = 'none';}");
        sb.append("</script>");
        sb.append("<br>");

        StringBuilder newSb = new StringBuilder();

        List<String> list = Lists.newArrayList();
        list.addAll(propertiesValue.keySet());
        Collections.sort(list);
        for (String key : list) {
            newSb.append("<span style='font-weight:normal'>").append(key).append("</span>").append(" : ")
                    .append("<span style='font-weight:bold'>").append(propertiesValue.get(key)).append("</span>")
                    .append("<br>");
        }
        sb.append("<a id='showData").append(randomNum).append("' onclick='showDiv").append(randomNum)
                .append("()''>显示测试数据</a>");
        sb.append("<a id='hideData").append(randomNum).append("' style='display:none' onclick='hideDiv")
                .append(randomNum).append("()''>隐藏测试数据</a>");
        sb.append("<div id='testData").append(randomNum).append("' style='display:none'>").append(newSb.toString())
                .append("</div>");
        return sb.toString();
    }

    /**
     * 比较枚举与属性文件，防止运行时为NUll
     */
    @SuppressWarnings("unused")
    private <E extends Enum<E>> void checkEnumMapping(E key) {
        String lineSeparator = System.getProperty("line.separator");
        if (!classEnumCheck.contains(key.getClass().getName())) {
            Set<String> misMatchEnum = newHashSet();
            for (Enum<?> value : key.getClass().getEnumConstants()) {
                if (propertiesValue.get(value.toString()) == null) {
                    misMatchEnum
                            .add("加载文件中不存在 KEY = " + value.toString() + " 在 class=" + key.getClass().getName() + " 中");
                }
            }
            if (!misMatchEnum.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String aMisMatchEnum : misMatchEnum) {
                    sb.append(aMisMatchEnum);
                    sb.append(lineSeparator);
                }
                throw new FrameworkException(sb.toString());
            }
            classEnumCheck.add(key.getClass().getName());
        }
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

    /**
     * 加载文件
     *
     * @param filePath 文件路径
     */
    private void loadFile(String filePath) {
        try {
            fis = new FileInputStream(new File(filePath));
            prop.load(fis);
            fis.close();
            createHashMap(prop);
        } catch (IOException | NullPointerException e) {
            LOGGER.error(e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
}
