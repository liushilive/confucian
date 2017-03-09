package confucian.combinedTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 测试用例集
 */
public class TestDataSet {
    private static final Logger LOGGER = LogManager.getLogger();

    private Scenario scenario;
    private IInventory inventory;
    private List<int[]> testSets = new ArrayList<>();
    private Random r = new Random(2);

    /**
     * 实例化一个新的测试数据集。
     *
     * @param inventory the inventory
     * @param scenario  the scenario
     */
    TestDataSet(IInventory inventory, Scenario scenario) {
        this.inventory = inventory;
        this.scenario = scenario;
    }

    /**
     * 测试集
     *
     * @return the test sets
     */
    public List<Map<String, Object>> getTestSets() {
        List<int[]> testSetIndexes = getRawTestSets();
        List<Map<String, Object>> completeDataSet = new ArrayList<>();
        for (int[] testSetIndex : testSetIndexes) {
            Map<String, Object> singleTestSet = new LinkedHashMap<>();
            for (int j = 0; j < scenario.getParameterSetCount(); j++) {
                Object value = scenario.getParameterValues().get(testSetIndex[j]);
                singleTestSet.put(scenario.getParameterSet(scenario.getParameterPositions()[testSetIndex[j]]).getName(),
                        value);
            }
            completeDataSet.add(singleTestSet);
        }
        return completeDataSet;
    }

    /**
     * 日志输出
     */
    public void logResults() {
        logFullCombinationCount();
        LOGGER.info("结果测试集: ");
        for (int i = 0; i < testSets.size(); i++) {
            StringBuffer outputStr = new StringBuffer(i + 1);
            int[] curr = testSets.get(i);
            for (int j = 0; j < scenario.getParameterSetCount(); j++) {
                outputStr.append(scenario.getParameterSets().get(j).getName()).append(":")
                        .append(scenario.getParameterValues().get(curr[j])).append(" ");
            }
            LOGGER.info(outputStr);
        }
    }

    /**
     * 构建测试用例。
     */
    void buildTestCases() {
        // 候选测试的数量在选择要添加到testSets列表的数组之前设置要生成的数组
        int poolSize = 1;
        LOGGER.debug("候选池大小: {}", poolSize);
        // 迭代,直到所有成对使用
        while (inventory.getUnusedMolecules().size() > 0) {
            // 只要有未使用的对就可以解决
            LOGGER.debug("未使用的对计数: {}", inventory.getUnusedMolecules().size());
            // 保存候选测试集
            int[][] candidateSets = new int[poolSize][scenario.getParameterSetCount()];

            for (int candidate = 0; candidate < poolSize; ++candidate) {
                LOGGER.debug("候选: {}", candidate);
                int[] testSet = getSingleTestSet();
                logCandidateTestSet(testSet);
                // 将候选testSet添加到候选Sets数组
                candidateSets[candidate] = testSet;
            }
            logCandidateTestSets(candidateSets);
            int[] bestTestSet = determineBestCandidateSet(candidateSets);
            // 添加最佳的候选人到主testSets列表
            testSets.add(bestTestSet);
            inventory.updateAllCounts(bestTestSet);
        }
    }

    /**
     * 确定最佳候选集合int []
     *
     * @param candidateSets the candidate sets
     *
     * @return the int [ ]
     */
    private int[] determineBestCandidateSet(int[][] candidateSets) {
        // 遍历candidateSets来确定最佳人选
        r.setSeed(r.nextLong());
        int indexOfBestCandidate = r.nextInt(candidateSets.length);
        // 选择一个随机指数作为最佳的
        int mostPairsCaptured = inventory.numberMoleculesCaptured(candidateSets[indexOfBestCandidate]);

        // 确定使用“最佳”的候选
        for (int i = 0; i < candidateSets.length; ++i) {
            int pairsCaptured = inventory.numberMoleculesCaptured(candidateSets[i]);
            if (pairsCaptured > mostPairsCaptured) {
                mostPairsCaptured = pairsCaptured;
                indexOfBestCandidate = i;
            }
            LOGGER.debug("候选{}捕获{}", i, mostPairsCaptured);
        }
        LOGGER.debug("候选{}是最佳的", indexOfBestCandidate);

        return candidateSets[indexOfBestCandidate];
    }

    /**
     * 获取参数排序int []
     *
     * @param firstPos  the first pos
     * @param secondPos the second pos
     *
     * @return the int [ ]
     */
    private int[] getParameterOrdering(int firstPos, int secondPos) {
        // 生成一个随机的顺序来填补位置参数
        int[] ordering = new int[scenario.getLegalValues().length];
        for (int i = 0; i < scenario.getLegalValues().length; i++) {
            // 最初都是按顺序
            ordering[i] = i;
        }

        ordering[0] = firstPos;
        ordering[firstPos] = 0;

        int t = ordering[1];
        ordering[1] = secondPos;
        ordering[secondPos] = t;

        for (int i = 2; i < ordering.length; i++) {
            int j = r.nextInt(ordering.length - i) + i;
            int temp = ordering[j];
            ordering[j] = ordering[i];
            ordering[i] = temp;
        }
        LOGGER.debug("顺序: {}", Arrays.toString(ordering));
        return ordering;
    }

    /**
     * 原始的测试集
     *
     * @return the raw test sets
     */
    private List<int[]> getRawTestSets() {
        return testSets;
    }

    /**
     * 获取单一测试集--很难弄清楚如何将它分成更小的块 - 一切都相互依存
     *
     * @return int [ ]
     */
    private int[] getSingleTestSet() {
        int[] bestMolecule = inventory.getBestMolecule();

        // 第一参数集的位置从最佳未使用的对
        int firstPos = scenario.getParameterPositions()[bestMolecule[0]];
        // 第二参数集的位置从最佳未使用的对
        int secondPos = scenario.getParameterPositions()[bestMolecule[1]];
        LOGGER.debug("最佳的对属于位置{}和{}", firstPos, secondPos);

        // 将来自最佳未使用对的两个参数值放入候选testSet中
        int[] testSet = new int[scenario.getParameterSetCount()];
        testSet[firstPos] = bestMolecule[0];
        testSet[secondPos] = bestMolecule[1];

        int[] ordering = getParameterOrdering(firstPos, secondPos);

        // 对于候选testSet中的剩余参数位置，尝试每个可能的合法值，挑选捕获未使用的对的值
        for (int i = 2; i < scenario.getParameterSetCount(); i++) {
            int currPos = ordering[i];
            int[] possibleValues = scenario.getLegalValues()[currPos];
            logPossibleValues(currPos, possibleValues);

            int highestCount = 0;
            int bestJ = 0;
            for (int j = 0; j < possibleValues.length; j++) {
                int currentCount = 0;
                for (int p = 0; p < i; ++p) {
                    int[] candidatePair = new int[]{possibleValues[j],
                            testSet[ordering[p]]};
                    if (inventory.getUnusedMoleculesSearch()[candidatePair[0]][candidatePair[1]] == 1 ||
                            inventory.getUnusedMoleculesSearch()[candidatePair[1]][candidatePair[0]] == 1)
                        ++currentCount;
                }
                if (currentCount > highestCount) {
                    highestCount = currentCount;
                    bestJ = j;
                }
            }
            LOGGER.debug(String.format("最佳可能值：[%d：%s]，参数集[%d：%s]，测试设置位置%d", possibleValues[bestJ],
                    scenario.getParameterValues().get(possibleValues[bestJ]), i,
                    scenario.getParameterSet(currPos).getName(), currPos));
            testSet[currPos] = possibleValues[bestJ];
        }

        return testSet;
    }

    private void logCandidateTestSet(int[] testSet) {
        LOGGER.debug("将候选测试分子添加到候选集数组： ");
        LOGGER.debug("候选测试集（索引）： {}", Arrays.toString(testSet));
        for (int i = 0; i < testSet.length; i++) {
            LOGGER.debug("候选测试集: (参数 {}): {}", i, scenario.getParameterValues().get(testSet[i]));
        }
    }

    private void logCandidateTestSets(int[][] candidateSets) {
        LOGGER.debug("候选测试分子: ");
        for (int i = 0; i < candidateSets.length; ++i) {
            int[] curr = candidateSets[i];
            LOGGER.debug(String.format(" 参数集 %d: 当前: %s, 捕获: %d", i, Arrays.toString(curr),
                    inventory.numberMoleculesCaptured(curr)));
        }
    }

    /**
     * 记录完全组合计数
     */
    private void logFullCombinationCount() {
        LOGGER.info("所有可能的组合: {}", inventory.getFullCombinationCount());
        LOGGER.info("最佳组合: {}", this.getRawTestSets().size());
    }

    private void logPossibleValues(int paramSetIndex, int[] possibleValues) {
        LOGGER.debug("可能的值是 ");
        for (int z = 0; z < possibleValues.length; z++) {
            LOGGER.debug(String.format("%d->%d: %s", paramSetIndex, possibleValues[z],
                    scenario.getParameterValues().get(scenario.getLegalValues()[paramSetIndex][z])));
        }
    }
}
