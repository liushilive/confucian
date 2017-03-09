package confucian.data;

import java.util.List;

import confucian.data.DataProvider.MapStrategy;

/**
 * 构建数据类，实现接口 {@link IMappingData}
 */
public class ImplementIMap implements IMappingData {
    private List<String> clientEnvironment = null;
    private String dataProviderPath = null;
    private MapStrategy runStrategy = null;
    private String testData = null;

    /**
     * 实现测试数据映射接口
     */
    private ImplementIMap(Builder build) {
        this.clientEnvironment = build.clientEnvironmentData;
        this.testData = build.testData;
        this.runStrategy = build.runStrategy;
        this.dataProviderPath = build.dataProviderPath;
    }

    /**
     * 获取环境配置
     */
    @Override
    public List<String> getClientEnvironment() {
        return clientEnvironment;
    }

    /**
     * 获取数据源文件
     *
     * @return the data provider path
     */
    @Override
    public String getDataProviderPath() {
        return dataProviderPath;
    }

    /**
     * 获取运行策略
     */
    @Override
    public MapStrategy getRunStrategy() {
        return runStrategy;
    }

    /**
     * 获取测试数据
     */
    @Override
    public String getTestData() {
        return testData;
    }

    /**
     * 构建类生成器
     */
    public static class Builder {

        private List<String> clientEnvironmentData;
        private String dataProviderPath;
        private MapStrategy runStrategy;
        private String testData;

        /**
         * 执行构建
         *
         * @return implement i map
         */
        public ImplementIMap build() {
            return new ImplementIMap(this);
        }

        /**
         * 客户端环境
         *
         * @param clientEData 客户端环境列表数据
         *
         * @return this builder
         */
        public Builder withClientEnvironment(List<String> clientEData) {
            this.clientEnvironmentData = clientEData;
            return this;
        }

        /**
         * 数据源文件
         *
         * @param dataProviderPath the data provider path
         *
         * @return the builder
         */
        public Builder withDataProviderPath(String dataProviderPath) {
            this.dataProviderPath = dataProviderPath;
            return this;
        }

        /**
         * 运行策略
         *
         * @param runStrategy the run strategy
         *
         * @return this builder
         */
        public Builder withRunStrategy(String runStrategy) {
            if ("full".equalsIgnoreCase(runStrategy)) {
                this.runStrategy = MapStrategy.Full;
            } else if ("optimal".equalsIgnoreCase(runStrategy)) {
                this.runStrategy = MapStrategy.Optimal;
            } else {
                this.runStrategy = null;
            }
            return this;
        }

        /**
         * 测试数据
         *
         * @param testData the test data
         *
         * @return this builder
         */
        public Builder withTestData(String testData) {
            this.testData = testData;
            return this;
        }
    }
}
