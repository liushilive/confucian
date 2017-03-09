package confucian.data.csv;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import confucian.common.Utils;
import confucian.data.IProperty;
import confucian.data.PropertyMapping;

/**
 * 传递测试数据xml返回{@link IProperty}列表
 */
public class CSVApplicationData {

    private static final Map<String, List<IProperty>> dataBucket = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();
    private static CSVApplicationData instance = null;
    private CsvParser parser;

    private CSVApplicationData() {
        CsvParserSettings settings = new CsvParserSettings();
        // 文件中使用 '\n' 作为行分隔符
        // 确保像MacOS和Windows这样的系统
        // 也可以正确处理（MacOS使用'\r'；Windows使用'\r\n'）
        settings.getFormat().setLineSeparator("\n");
        // 可以配置解析器自动检测输入中的行分隔符
        settings.setLineSeparatorDetectionEnabled(true);
        // 引号内部的值，如果包含引号需要用反斜线转义 \"
        settings.getFormat().setQuoteEscape('\\');
        // 如果发现引号内部有两个反斜线，表示值中包含了一个反斜线
        settings.getFormat().setCharToEscapeQuoteEscaping('\\');
        // 设置解析结果为null时填入的默认值
        // settings.setNullValue("NULL");
        // 设置解析文件的标题
        // settings.setHeaderExtractionEnabled(true);
        // 创建CSV解析器
        parser = new CsvParser(settings);
    }

    /**
     * 获取实例
     *
     * @return 实例 instance
     */
    public static CSVApplicationData getInstance() {
        if (null == instance) {
            synchronized (CSVApplicationData.class) {
                if (null == instance) {
                    instance = new CSVApplicationData();
                }
            }
        }
        return instance;
    }

    /**
     * 获取应用数据列表
     *
     * @param csvName     xml名称
     * @param environment 环境
     *
     * @return the list
     */
    public List<IProperty> getAppData(String csvName, String environment) {
        csvName = environment;
        return getAppData(csvName);
    }

    /**
     * 在自动化中使用键值对
     *
     * @param csvName csv名称
     *
     * @return 应用数据 app data
     */
    public List<IProperty> getAppData(String csvName) {
        List<String[]> parseAll = parser.parseAll(Utils.getReader(csvName));
        if (dataBucket.containsKey(csvName))
            return dataBucket.get(csvName);
        else {
            // 头
            String[] heads = parseAll.get(0);
            List<IProperty> dataProperty = Lists.newArrayList();
            HashMap<String, String> keyValue;
            for (int i = 1; i < parseAll.size(); i++) {
                keyValue = Maps.newHashMap();
                String[] strings = parseAll.get(i);
                int n = 0;
                int length = heads.length;
                for (int i1 = 0; i1 < length; i1++) {
                    String string = strings[i1];
                    if (string == null)
                        n++;
                    keyValue.put(heads[i1], string);
                }
                if (n != length)
                    dataProperty.add(new PropertyMapping(keyValue));
            }
            dataBucket.put(csvName, dataProperty);
            LOGGER.debug("读取 " + csvName + " 完毕");
            return dataProperty;
        }
    }
}
