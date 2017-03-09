package confucian.data;

/**
 * 数据源枚举
 */
public enum DataSource {
    /**
     * CSV数据源,无浏览器配置
     */
    CSVData,
    /**
     * XML数据源,无浏览器配置
     */
    XmlData,
    /**
     * CSV数据源,带浏览器配置
     */
    CSVDataBrowser,
    /**
     * XML数据源,带浏览器配置
     */
    XmlDataBrowser,
    /**
     * 没有数据源,带浏览器配置
     */
    NoSourceBrowser,
    /**
     * 无效的数据源
     */
    Invalid
}