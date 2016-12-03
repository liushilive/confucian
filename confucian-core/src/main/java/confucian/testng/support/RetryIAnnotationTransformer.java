package confucian.testng.support;

import com.google.common.collect.Maps;

import org.testng.IAnnotationTransformer2;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import confucian.common.Utils;
import confucian.data.IProperty;
import confucian.data.MethodContext;
import confucian.data.driverConfig.IBrowserConfig;
import confucian.exception.FrameworkException;

/**
 * 为追加重试注解关于全部测试用例和创建映射有@AfterMethod和@BeforeMethod的类
 */
public class RetryIAnnotationTransformer implements IAnnotationTransformer2 {
    /**
     * 方法上下文持有人
     */
    static final Map<String, MethodContext> methodContextHolder = Maps.newHashMap();

    /**
     * 转换
     *
     * @param annotation      测试注解
     * @param testClass       测试类
     * @param testConstructor 测试构建器
     * @param testMethod      测试方法
     */
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (testMethod != null) {
            if (isPartOfFactoryTest(testMethod)) {
                MethodContext context = new MethodContext(testMethod);
                // context.setRetryAnalyser(annotation);
                context.setDataProvider(annotation, testMethod);
                context.prepareData();
                //更新方法上下文持有者
                methodContextHolder.put(Utils.getFullMethodName(testMethod), context);
            }
        }
    }

    /**
     * 验证测试方法传入参数准确性
     *
     * @param testMethod 测试方法
     */
    private boolean isPartOfFactoryTest(Method testMethod) {
        int length = testMethod.getGenericParameterTypes().length;
        if (length == 2 && testMethod.getGenericParameterTypes()[0].equals(IBrowserConfig.class) &&
                testMethod.getGenericParameterTypes()[1].equals(IProperty.class))
            return true;
        else if (length == 1 && testMethod.getGenericParameterTypes()[0].equals(IBrowserConfig.class))
            return true;
        else if (length == 0)
            return false;
        else
            throw new FrameworkException("无法通过验证测试方法传入参数准确性");
    }

    /**
     * 转换
     *
     * @param annotation      测试注解
     * @param testClass       测试类
     * @param testConstructor 测试构建器
     * @param testMethod      测试方法
     */
    @SuppressWarnings("rawtypes")
    public void transform(IConfigurationAnnotation annotation, Class testClass, Constructor testConstructor,
                          Method testMethod) {
    }

    /**
     * 转换
     *
     * @param annotation 测试注解
     * @param method     方法
     */
    public void transform(IDataProviderAnnotation annotation, Method method) {
        //不需要添加任何执行重写测试接口
    }

    /**
     * 使用测试工厂类的单次执行
     */
    public void transform(IFactoryAnnotation annotation, Method testMethod) {
        if (testMethod != null) {
            MethodContext context = new MethodContext(testMethod);
            context.setDataProvider(annotation, testMethod);
            context.prepareData();
            //更新方法上下文持有者
            methodContextHolder.put(Utils.getFullMethodName(testMethod), context);
        }

    }

}