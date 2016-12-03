package confucian.combinedTest;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组合测试工厂
 */
public class PairwiseInventoryFactory {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 解析字符串表示的内容场景中,并返回该场景
     *
     * @param contents 场景字符串-测试内容
     * @return the Scenario
     */
    public static IInventory generateParameterInventory(String contents) {
        IInventory inventory = new PairwiseInventory();
        Scenario scenario = generateScenario(contents);
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        return inventory;
    }

    /**
     * 通过参数设置填充的列表ParameterSets
     *
     * @param contents 测试场景内容
     * @return 返回场景，完全填充
     */
    private static Scenario generateScenario(String contents) {
        Scenario scenario = new Scenario();
        for (String line : StringUtils.split(contents, System.getProperty("line.separator"))) {
            scenario.addParameterSet(processOneLine(line));
        }
        return scenario;
    }

    /**
     * 处理单个输入的行
     *
     * @param line 一行,包含一个参数空间(如：“标题:Value1,Value2,Value3”)
     * @return parameter set
     */
    private static ParameterSet<String> processOneLine(String line) {
        LOGGER.debug("处理行: {}", line);
        String[] lineTokens = line.split(":", 2);
        List<String> strValues = splitAndTrim(lineTokens[1]);
        ParameterSet<String> parameterSet = new ParameterSet<>(strValues);
        parameterSet.setName(lineTokens[0]);
        return parameterSet;
    }

    /**
     * 分割并去除空格
     *
     * @param lineTokens 行数据
     * @return 分割后数据列表
     */
    private static List<String> splitAndTrim(String lineTokens) {
        String[] rawTokens = lineTokens.split(",");
        String[] processedTokens = new String[rawTokens.length];
        for (int i = 0; i < rawTokens.length; i++) {
            processedTokens[i] = StringUtils.trim(rawTokens[i]);
        }
        return Arrays.asList(processedTokens);
    }

    /**
     * 解析流表示的内容场景中,并返回该场景
     *
     * @param stream 流
     * @return inventory inventory
     * @throws IOException the io exception
     */
    public static IInventory generateParameterInventory(InputStream stream) throws IOException {
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(isr);
        Multimap<String, String> stringMultimap = ArrayListMultimap.create();
        Multimap<String, Map<String, Object>> testDataSets = ArrayListMultimap.create();
        String key = "";
        String line;
        Scenario scenario = new Scenario();
        while ((line = br.readLine()) != null) {
            if (line.contains("@")) {
                key = line;
            } else if (StringUtils.isNotEmpty(line))
                stringMultimap.put(key, line);
        }
        if (stringMultimap.containsKey("@ROOT")) {
            Map<Boolean, List<String>> booleanListMap =
                    stringMultimap.get("@ROOT").stream().collect(Collectors.partitioningBy(s -> s.startsWith("$")));

            booleanListMap.get(true).parallelStream().forEachOrdered(s -> {
                Scenario scenarioTmp = new Scenario();
                stringMultimap.get(s.replace("$", "@")).parallelStream().map(PairwiseInventoryFactory::processOneLine)
                        .forEachOrdered(scenarioTmp::addParameterSet);
                if (scenarioTmp.getParameterValuesCount() > 0) {
                    IInventory inventoryTmp = new PairwiseInventory();
                    inventoryTmp.setScenario(scenarioTmp);
                    inventoryTmp.buildMolecules();
                    inventoryTmp.getTestDataSet().getTestSets()
                            .forEach(testSet -> testDataSets.put(s.replace("$", ""), testSet));
                }
            });
            booleanListMap.get(false).parallelStream().forEachOrdered(s -> testDataSets.put(s, null));
            List<ParameterSet<?>> tmpList = Lists.newArrayList();
            for (String s : testDataSets.keySet()) {
                if (testDataSets.get(s).contains(null)) {
                    tmpList.add(processOneLine(s));
                } else
                    scenario.addParameterSet(processList(s, new ArrayList<>(testDataSets.get(s))));
            }
            tmpList.forEach(scenario::addParameterSet);
        } else {
            stringMultimap.values().forEach(s -> scenario.addParameterSet(processOneLine(s)));
        }
        IInventory inventory = new PairwiseInventory();
        inventory.setScenario(scenario);
        inventory.buildMolecules();
        return inventory;
    }

    /**
     * 处理List输入
     *
     * @param s    名称
     * @param maps 列表
     * @return parameter Map
     */
    private static ParameterSet<Map> processList(String s, ArrayList<Map> maps) {
        LOGGER.debug("处理行: {}", maps.toString());
        ParameterSet<Map> parameterSet = new ParameterSet<>(maps);
        parameterSet.setName(s);
        return parameterSet;
    }
}
