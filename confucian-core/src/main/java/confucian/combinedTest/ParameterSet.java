package confucian.combinedTest;

import java.util.ArrayList;
import java.util.List;

/**
 * “维度”
 *
 * @param <T> 类型参数
 */
class ParameterSet<T> {
    private String name;
    private List<T> parameterValues = new ArrayList<>();

    /**
     * 实例化一个新的参数集。
     *
     * @param values the values
     */
    ParameterSet(List<T> values) {
        parameterValues.addAll(values);
    }

    /**
     * 获取维度名称
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置维度名称
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取参数集集合
     *
     * @return the parameter values
     */
    public List<T> getParameterValues() {
        return parameterValues;
    }
}