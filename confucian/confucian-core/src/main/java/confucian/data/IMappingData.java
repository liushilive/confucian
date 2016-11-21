package confucian.data;

import confucian.data.DataProvider.MapStrategy;

import java.util.List;

/**
 * 接口有测试数据，客户端环境列表，运行策略
 */
public interface IMappingData {

    /**
     * 获取客户端环境
     *
     * @return 客户端环境 client environment
     */
    List<String> getClientEnvironment();

    /**
     * 获取数据源文件
     *
     * @return the data provider path
     */
    String getDataProviderPath();

    /**
     * 获取运行策略
     *
     * @return 运行策略 run strategy
     */
    MapStrategy getRunStrategy();

    /**
     * 获取测试数据
     *
     * @return the test data
     */
    String getTestData();
}
