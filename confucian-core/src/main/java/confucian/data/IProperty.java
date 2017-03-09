package confucian.data;

import java.util.List;
import java.util.Set;

/**
 * 接口属性文件
 */
public interface IProperty {

    /**
     * 获取所有的key
     *
     * @return keys
     */
    Set<String> getKeys();

    /**
     * 获取Node的Name属性值
     *
     * @param typeName  the type name
     * @param NodesName the nodes name
     *
     * @return Nodes的Name属性值List node name
     */
    List<String> getNodeName(String typeName, String NodesName);

    /**
     * 获取Node的Name属性值
     *
     * @param typeName the type name
     *
     * @return Nodes的Name属性值List node name
     */
    List<String> getNodeName(String typeName);

    /**
     * 获取Nodes的Name属性值
     *
     * @param typeName the type name
     *
     * @return Nodes的Name属性值List nodes name
     */
    List<String> getNodesName(String typeName);

    /**
     * 获取Page的Name属性值
     *
     * @return page的Name属性值 page name
     */
    List<String> getPageName();

    /**
     * 获取Type的Name属性值
     *
     * @return Type的Name属性值List type name
     */
    List<String> getTypeName();

    /**
     * 获取值
     *
     * @param <E>      类型参数
     * @param envValue 枚举值
     *
     * @return {@code <E extends Enum<E>>} 限定形态参数实例化的对象，只能是Enum
     */
    <E extends Enum<E>> String getValue(E envValue);

    /**
     * 获取值
     *
     * @param key 键
     *
     * @return 值 value
     */
    String getValue(String... key);
}
