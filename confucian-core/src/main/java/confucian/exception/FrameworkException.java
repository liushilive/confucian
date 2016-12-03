package confucian.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Library 相关异常
 */
@SuppressWarnings("serial")
public class FrameworkException extends RuntimeException {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 实例化一个新的框架异常
     *
     * @param message 消息
     */
    public FrameworkException(String message) {
        super(message);
        LOGGER.error(message);
    }

    /**
     * 实例化一个新的框架异常
     *
     * @param message 消息
     * @param e       the e
     */
    public FrameworkException(String message, Throwable e) {
        super(message, e);
        LOGGER.error(message, e);
    }

    /**
     * 实例化一个新的框架异常
     *
     * @param e the e
     */
    public FrameworkException(Throwable e) {
        super(e);
        LOGGER.error(e);
    }
}
