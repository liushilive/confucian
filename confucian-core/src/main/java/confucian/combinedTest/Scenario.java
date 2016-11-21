package confucian.combinedTest;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 场景
 */
class Scenario {
    private static final Logger LOGGER = LogManager.getLogger();
    private int[][] legalValues;
    /**
     * 给定的值的参数位置
     */
    private int[] parameterPositions = null;
    private List<ParameterSet<?>> parameterSets = new ArrayList<>();
    /**
     * 所有的参数设置的值
     */
    private List<?> parameterValues = new ArrayList();

    /**
     * 添加参数集
     *
     * @param parameterSet 参数集
     */
    void addParameterSet(ParameterSet<?> parameterSet) {
        parameterSets.add(parameterSet);
        int[] parameterValueIndexes = new int[parameterSet.getParameterValues().size()];

        // 每次从这里重建的各种元数据数组(因为我们永远不知道我们是否“完成”——他们可以继续添加参数设置)
        updateLegalValues(parameterSet, parameterValueIndexes);
        updateParameterValues(parameterSet);
        updateParameterPositions();
    }

    /**
     * 更新参数值
     *
     * @param parameterSet the parameter set
     */
    private void updateParameterValues(ParameterSet<?> parameterSet) {
        parameterValues.addAll((Collection) parameterSet.getParameterValues());
    }

    /**
     * 更新合法参数值
     *
     * @param parameterSet          参数集
     * @param parameterValueIndexes 参数值的索引
     */
    private void updateLegalValues(ParameterSet<?> parameterSet, int[] parameterValueIndexes) {
        for (int i = 0, j = getParameterValuesCount();
             j < getParameterValuesCount() + parameterSet.getParameterValues().size(); i++, j++) {
            parameterValueIndexes[i] = j;
        }
        legalValues = ArrayUtils.addAll(legalValues, parameterValueIndexes);
    }

    /**
     * 参数值总数
     *
     * @return parameter values count
     */
    int getParameterValuesCount() {
        return parameterValues.size();
    }

    /**
     * parameterPositions字段(int[])代表“参数位置”为每个给定的值
     */
    void updateParameterPositions() {
        int[] parameterPositions = new int[this.getParameterValuesCount()];

        int k = 0; // 附加到此值的参数集的索引
        for (int i = 0; i < this.getLegalValues().length; ++i) {
            int[] curr = this.getLegalValues()[i];
            for (int ignored : curr) {
                parameterPositions[k++] = i;
            }
        }
        LOGGER.debug("参数位置: {}", Arrays.toString(parameterPositions));
        this.parameterPositions = parameterPositions;
    }

    /**
     * 获取合法的参数集与下标索引
     *
     * @return int [ ] [ ]
     */
    int[][] getLegalValues() {
        return legalValues;
    }

    /**
     * 获取参数位置
     *
     * @return the int [ ]
     */
    int[] getParameterPositions() {
        return this.parameterPositions;
    }

    /**
     * 根据下标获取参数集合
     *
     * @param index the index
     * @return the parameter set
     */
    ParameterSet<?> getParameterSet(int index) {
        return getParameterSets().get(index);
    }

    /**
     * 参数设置
     *
     * @return the parameter sets
     */
    List<ParameterSet<?>> getParameterSets() {
        return parameterSets;
    }

    /**
     * 参数值的总数
     *
     * @return parameter set count
     */
    int getParameterSetCount() {
        return legalValues.length;
    }

    /**
     * 得到参数集集合
     *
     * @return the parameter values
     */
    List<?> getParameterValues() {
        return parameterValues;
    }
}
