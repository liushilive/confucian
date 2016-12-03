package confucian.combinedTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 术语:
 * Inventory: 要在测试场景中使用的所有参数集的完整定义
 * Scenario: 来自一个参数定义的单个值集合
 * Molecule: 来自不同参数集的值（Atom）的组合必须被测试（最初这总是一个“对”，但是我们想要能够做次序3，次数4，次数n的组合）
 * Atom: 来自参数集的单个值，其组合变成“Molecules”
 * <p>
 * 实例:
 * Param1: a, b, c
 * Param2: i, j, k, l
 * Param3: x, y
 * <p>
 * 类中字段设置如下:
 * <p>
 * 参数值:[ a, b, c, i, j, k, l, x, y ] --  所有可能值的数组
 * 参数集计数:3
 * 参数值计数:9
 * 对计数:
 * 合法值:[ [ 0, 1, 2 ], [ 3, 4, 5, 6 ], [ 7, 8 ] ] -- set(x)的数组列表, 与指针指向 "parameterValues" array(y)
 * 所有分子:
 * 参数位置:[ 0, 0, 0, 1, 1, 1, 1, 2, 2 ]  --  允许我们查找给定值附加到哪个参数集
 * 一个可能的测试集:[ 2, 4, 7 ], 表示来自每个参数集的一个值（索引）。这可以想象是一个“测试用例”
 */
class PairwiseInventory implements IInventory {
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * 分子信息
     */
    private List<Molecule> allMolecules = null;
    /**
     * 场景
     */
    private Scenario scenario;
    /**
     * 尚未使用的分子。 在使用它们时，它们将从此列表中删除
     */
    private List<Molecule> unusedMolecules = null;
    private int[][] unusedMoleculesSearch = null;
    private int[] unusedParameterIndexCounts;

    @Override
    public TestDataSet getTestDataSet() {
        TestDataSet dataSet = new TestDataSet(this, scenario);
        dataSet.buildTestCases();
        // dataSet.logFullCombinationCount();
        return dataSet;
    }

    @Override
    public int numberMoleculesCaptured(int[] testSet) {
        int moleculesCapturedCount = 0;
        for (int i = 0; i <= testSet.length - 2; ++i)
            for (int j = i + 1; j <= testSet.length - 1; ++j)
                if (unusedMoleculesSearch[testSet[i]][testSet[j]] == 1)
                    ++moleculesCapturedCount;
        return moleculesCapturedCount;
    }

    @Override
    public int[] getBestMolecule() {
        // 通过循环使用未使用的组来加权对
        int bestWeight = 0;
        int indexOfBestMolecule = 0;
        for (int unusedMoleculeIndex = 0; unusedMoleculeIndex < this.getUnusedMolecules().size();
             unusedMoleculeIndex++) {
            int[] curr = this.getUnusedMolecules().get(unusedMoleculeIndex).getAtoms();
            int weight = this.getUnusedParameterIndexCounts()[curr[0]] + this.getUnusedParameterIndexCounts()[curr[1]];
            LOGGER.debug(String.format("对 %d: [%s,%s], 权重: %2d", unusedMoleculeIndex,
                    scenario.getParameterValues().get(curr[0]), scenario.getParameterValues().get(curr[1]), weight));

            // 如果新对的权重高于前一个，使它成为新的“最佳”
            if (weight > bestWeight) {
                bestWeight = weight;
                indexOfBestMolecule = unusedMoleculeIndex;
            }
        }

        // 日志，并返回最佳的对
        int[] best = this.getUnusedMolecules().get(indexOfBestMolecule).getAtoms();
        LOGGER.debug(String.format("最佳对是 [%s, %s] 在 %d 权重 %d", scenario.getParameterValues().get(best[0]),
                scenario.getParameterValues().get(best[1]), indexOfBestMolecule, bestWeight));
        return best;
    }

    @Override
    public void updateAllCounts(int[] bestTestSet) {
        for (int i = 0; i <= scenario.getParameterSetCount() - 2; ++i) {
            for (int j = i + 1; j <= scenario.getParameterSetCount() - 1; ++j) {
                int v1 = bestTestSet[i]; // 新添加的值1对
                int v2 = bestTestSet[j]; // 新添加的值2对

                LOGGER.debug("调整未使用的计数 [{}][{}]", v1, v2);
                --unusedParameterIndexCounts[v1];
                --unusedParameterIndexCounts[v2];

                LOGGER.debug("设置获取未使用的分子搜索在 [{}][{}] to 0", v1, v2);
                this.getUnusedMoleculesSearch()[v1][v2] = 0;

                // 设置未使用的分子的新列表，然后将其分配回未使用的Molecules字段 - 否则ConcurrentModificationException
                List<Molecule> tempUnusedMolecules = new ArrayList<>();
                tempUnusedMolecules.addAll(this.getUnusedMolecules());

                for (Molecule molecule : this.getUnusedMolecules()) {
                    int[] curr = molecule.getAtoms();

                    if (curr[0] == v1 && curr[1] == v2) {
                        LOGGER.debug("删除对 [{}, {}] 从未使用的分子列表中", v1, v2);
                        tempUnusedMolecules.remove(molecule);
                    }
                }
                this.unusedMolecules = tempUnusedMolecules;
            }
        }
    }

    @Override
    public void processUnusedValues() {
        // 索引是参数值，单元格值是参数值在analyzer.getUsedParts()集合中显示的次数的计数
        int[] unusedCounts = new int[scenario.getParameterValuesCount()];
        for (Molecule molecule : this.getAllMolecules()) {
            ++unusedCounts[molecule.getAtoms()[0]];
            ++unusedCounts[molecule.getAtoms()[1]];
        }

        this.logUnusedMolecules(unusedMolecules);
        this.unusedParameterIndexCounts = unusedCounts;
    }

    /**
     * 构建分子
     */
    @Override
    public void buildMolecules() {
        // 尚未捕获的对的列表
        List<Molecule> unusedMolecules = new ArrayList<>();
        List<Molecule> allMolecules = new ArrayList<>();

        int[][] unusedMoleculesSearch = new int[scenario.getParameterValuesCount()][scenario.getParameterValuesCount()];
        for (int parameterSet = 0; parameterSet < scenario.getLegalValues().length - 1; parameterSet++) {
            for (int nextParameterValue = parameterSet + 1; nextParameterValue < scenario.getLegalValues().length;
                 nextParameterValue++) {
                int[] firstRow = scenario.getLegalValues()[parameterSet];
                int[] secondRow = scenario.getLegalValues()[nextParameterValue];

                for (int aFirstRow : firstRow) {
                    for (int aSecondRow : secondRow) {
                        int[] atoms = new int[]{aFirstRow,
                                aSecondRow};
                        Molecule molecule = new Molecule();
                        molecule.setAtoms(atoms);

                        unusedMolecules.add(molecule);
                        unusedMoleculesSearch[aFirstRow][aSecondRow] = 1;
                        allMolecules.add(molecule);
                        logUnusedMolecules(unusedMolecules);
                    }
                }
            }
        }

        scenario.updateParameterPositions();
        this.allMolecules = allMolecules;
        this.unusedMolecules = unusedMolecules;
        this.unusedMoleculesSearch = unusedMoleculesSearch;
        this.processUnusedValues();
        this.logAllMolecules(this.getAllMolecules());
        this.logUnusedMolecules(this.getUnusedMolecules());
    }

    @Override
    public int[][] getUnusedMoleculesSearch() {
        return unusedMoleculesSearch;
    }

    @Override
    public List<Molecule> getUnusedMolecules() {
        return unusedMolecules;
    }

    @Override
    public List<Molecule> getAllMolecules() {
        return allMolecules;
    }

    @Override
    public long getFullCombinationCount() {
        long count = 1;
        // 乘以所有参数, X * Y * Z
        for (ParameterSet<?> set : scenario.getParameterSets()) {
            count *= set.getParameterValues().size();
        }
        return count;
    }

    @Override
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    /**
     * Get unused parameter index counts int [ ].
     *
     * @return the int [ ]
     */
    private int[] getUnusedParameterIndexCounts() {
        return this.unusedParameterIndexCounts;
    }

    /**
     * 日志输出所有分子
     *
     * @param allMolecules the all molecules
     */
    private void logAllMolecules(List<Molecule> allMolecules) {
        LOGGER.debug("所有的分子:");
        int moleculeCount = 0;
        for (Molecule molecule : allMolecules) {
            LOGGER.debug(String.format("%03d: %s", moleculeCount, Arrays.toString(molecule.getAtoms())));
            moleculeCount++;
        }
    }

    /**
     * 日志输出未使用的分子
     *
     * @param unusedMolecules the unused molecules
     */
    private void logUnusedMolecules(List<Molecule> unusedMolecules) {
        final String[] unusedPairsStr = {"未使用的分子: "};
        unusedMolecules.stream().filter(Objects::nonNull)
                .forEachOrdered(molecule -> unusedPairsStr[0] += (molecule + ","));
        unusedPairsStr[0] = unusedPairsStr[0].substring(0, unusedPairsStr[0].length() - 1);
        LOGGER.debug(unusedPairsStr[0]);
    }
}