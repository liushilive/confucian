package confucian.combinedTest;

import java.util.List;

/**
 * 组合测试功能清单
 */
public interface IInventory {
    /**
     * 处理合法值数组以填充所有集，未使用的对和未使用的对搜索集合
     */
    void buildMolecules();

    /**
     * 获取所有分子。
     *
     * @return the all molecules
     */
    List<Molecule> getAllMolecules();

    /**
     * 选择“最佳”未使用的分子 - 具有最高未使用值数的对
     *
     * @return int [ ]
     */
    int[] getBestMolecule();

    /**
     * 返回可从此参数集生成的可能组合数的计数
     *
     * @return 可能的组合数 ，检查100％
     */
    long getFullCombinationCount();

    /**
     * 返回的整个测试用例集这个库存了,通过运行“算法”后添加了所有的参数设置
     *
     * @return test data set
     */
    TestDataSet getTestDataSet();

    /**
     * 未使用的分子。
     *
     * @return the unused molecules
     */
    List<Molecule> getUnusedMolecules();

    /**
     * 获取未使用的分子搜索int [] []
     *
     * @return the int [ ] [ ]
     */
    int[][] getUnusedMoleculesSearch();

    /**
     * 返回给定测试集（参数索引集）尚未使用对的数量。
     *
     * @param testSet the test set
     *
     * @return int int
     */
    int numberMoleculesCaptured(int[] testSet);

    /**
     * 处理“已使用”集以确定哪些集尚未使用
     */
    void processUnusedValues();

    /**
     * 设置方案。
     *
     * @param scenario the scenario
     */
    void setScenario(Scenario scenario);

    /**
     * 更新所有计数
     *
     * @param bestTestSet the best test set
     */
    void updateAllCounts(int[] bestTestSet);

}
