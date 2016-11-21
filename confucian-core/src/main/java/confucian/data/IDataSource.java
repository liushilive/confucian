package confucian.data;

import java.util.Map;

/**
 * 数据源
 */
public interface IDataSource {
    /**
     * 所有映射数据的读取
     *
     * @return 主要数据
     */
    Map<String, IMappingData> getPrimaryData();
}
