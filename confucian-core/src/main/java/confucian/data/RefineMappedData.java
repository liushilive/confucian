package confucian.data;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import confucian.common.Utils;
import confucian.exception.FrameworkException;

/**
 * 完善数据基于层次结构的方法 -- 类 -- 包，
 * 获取并返回 {@link IDataSource}
 */
class RefineMappedData {
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<String, IMappingData> primaryDataMap;

    /**
     * 实例化一个新的映射数据
     *
     * @param dataSource 数据源
     */
    RefineMappedData(IDataSource dataSource) {
        primaryDataMap = dataSource.getPrimaryData();
        // 主映射值
        primaryDataMap.forEach((k, v) -> LOGGER.debug("主键:" + k + "值:" + v.getRunStrategy()));
    }

    /**
     * 获取测试数据文件
     */
    private String getDataProviderPath(Method method) {
        IMappingData methodDataProviderPath = primaryDataMap.get(Utils.getFullMethodName(method));
        IMappingData classDataProviderPath = primaryDataMap.get(method.getDeclaringClass().getName());
        IMappingData packageDataProviderPath = primaryDataMap.get(method.getDeclaringClass().getPackage().getName());
        if (methodDataProviderPath != null && methodDataProviderPath.getDataProviderPath() != null) {
            return methodDataProviderPath.getDataProviderPath();
        } else if (classDataProviderPath != null && classDataProviderPath.getDataProviderPath() != null) {
            return classDataProviderPath.getDataProviderPath();
        } else if (packageDataProviderPath != null && packageDataProviderPath.getDataProviderPath() != null) {
            return packageDataProviderPath.getDataProviderPath();
        }
        return null;
    }

    /**
     * 获取方法数据
     *
     * @param methodName the method name
     *
     * @return method data
     */
    IMappingData getMethodData(Method methodName) {
        return new ImplementIMap.Builder().withTestData(getRefinedTestData(methodName))
                .withClientEnvironment(getRefinedClientEnvironment(methodName))
                .withRunStrategy(getRunStrategy(methodName).toString())
                .withDataProviderPath(getDataProviderPath(methodName)).build();
    }

    /**
     * 获取客户端环境
     */
    private List<String> getRefinedClientEnvironment(Method method) {
        IMappingData methodClientData = primaryDataMap.get(Utils.getFullMethodName(method));
        IMappingData classClientData = primaryDataMap.get(method.getDeclaringClass().getName());
        IMappingData packageClientData = primaryDataMap.get(method.getDeclaringClass().getPackage().getName());

        //如果列表第一条为0，可以肯定客户端环境为假列表
        if (methodClientData != null && !methodClientData.getClientEnvironment().isEmpty() &&
                StringUtils.isNotBlank(methodClientData.getClientEnvironment().get(0))) {
            LOGGER.debug("方法:" + methodClientData.getClientEnvironment().get(0));
            return methodClientData.getClientEnvironment();
        } else if (classClientData != null && !classClientData.getClientEnvironment().isEmpty() &&
                StringUtils.isNotBlank(classClientData.getClientEnvironment().get(0))) {
            LOGGER.debug("类" + classClientData.getClientEnvironment().get(0));
            return classClientData.getClientEnvironment();
        } else if (packageClientData != null && !packageClientData.getClientEnvironment().isEmpty() &&
                StringUtils.isNotBlank(packageClientData.getClientEnvironment().get(0))) {
            LOGGER.debug("包:" + packageClientData.getClientEnvironment().get(0));
            return packageClientData.getClientEnvironment();
        }
        return null;
        // throw new FrameworkException("没有客户端环境/浏览器定义的方法:" + method.getName() + "在映射或方法/类/包中缺少映射");
    }

    /**
     * 获取测试数据
     */
    private String getRefinedTestData(Method method) {
        IMappingData methodVal = primaryDataMap.get(Utils.getFullMethodName(method));
        IMappingData classVal = primaryDataMap.get(method.getDeclaringClass().getName());
        IMappingData packageVal = primaryDataMap.get(method.getDeclaringClass().getPackage().getName());

        if (methodVal != null && (methodVal.getTestData() != null)) {
            return methodVal.getTestData();
        } else if (classVal != null && classVal.getTestData() != null) {
            return classVal.getTestData();
        } else if (packageVal != null && packageVal.getTestData() != null) {
            return packageVal.getTestData();
        }
        throw new FrameworkException("没有定义测试数据的方法:" + method.getName() + "在映射或方法/类/包中缺少映射");
    }

    /**
     * 获取运行策略
     */
    private DataProvider.MapStrategy getRunStrategy(Method method) {
        IMappingData methodRunStrategy = primaryDataMap.get(Utils.getFullMethodName(method));
        IMappingData classRunStrategy = primaryDataMap.get(method.getDeclaringClass().getName());
        IMappingData packageRunStrategy = primaryDataMap.get(method.getDeclaringClass().getPackage().getName());
        if (methodRunStrategy != null && methodRunStrategy.getRunStrategy() != null) {
            return methodRunStrategy.getRunStrategy();
        } else if (classRunStrategy != null && classRunStrategy.getRunStrategy() != null) {
            return classRunStrategy.getRunStrategy();
        } else if (packageRunStrategy != null && packageRunStrategy.getRunStrategy() != null) {
            return packageRunStrategy.getRunStrategy();
        }
        return DataProvider.MapStrategy.Optimal;
    }
}
