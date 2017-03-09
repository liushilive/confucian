package confucian.testng.support;

import confucian.data.IMethodContext;

/**
 * 上下文集合
 */
public class MethodContextCollection {

    /**
     * Get方法上下文
     *
     * @param methodName 方法名称
     *
     * @return 方法上下文
     */
    public static IMethodContext getMethodContext(String methodName) {
        return RetryIAnnotationTransformer.methodContextHolder.get(methodName);
    }

}
