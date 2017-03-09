package confucian.data.DB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import confucian.exception.FrameworkException;

/**
 * 启用事务注释代理
 */
public class TransactionProxyManager {
    private TransactionManager transactionManager;

    /**
     * 实例化一个新的事务注释启用代理，默认自动释放资源
     */
    public TransactionProxyManager() {
        this.transactionManager = new TransactionManager(HikariData.getConnection());
    }

    /**
     * 获得连接
     *
     * @return the connection
     */
    public Connection getConnection() {
        return transactionManager.getConnection();
    }

    /**
     * 代理对象
     *
     * @param object the object
     *
     * @return the object
     */
    public Object proxyFor(Object object) {
        return Proxy.newProxyInstance(object.getClass().getClassLoader(),
                object.getClass().getInterfaces(),
                new AnnotationTransactionInvocationHandler(object, transactionManager));
    }
}

/**
 * 注释类型事务调用处理程序
 */
class AnnotationTransactionInvocationHandler implements InvocationHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private Object proxied;
    private TransactionManager transactionManager;

    /**
     * 实例化一个新的注释事务调用处理程序
     *
     * @param object             the object
     * @param transactionManager the transaction manager
     */
    AnnotationTransactionInvocationHandler(Object object, TransactionManager transactionManager) {
        this.proxied = object;
        this.transactionManager = transactionManager;
    }

    public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {
        Method originalMethod = proxied.getClass().getMethod(method.getName(), method.getParameterTypes());
        Object result;
        try {
            if (!originalMethod.isAnnotationPresent(Transactional.class))
                result = method.invoke(proxied, objects);
            else {
                transactionManager.start();
                result = method.invoke(proxied, objects);
                transactionManager.commit();
            }
        } catch (Exception e) {
            transactionManager.rollback();
            throw new FrameworkException("事务回滚", e);
        } finally {
            transactionManager.close();
        }
        return result;
    }
}