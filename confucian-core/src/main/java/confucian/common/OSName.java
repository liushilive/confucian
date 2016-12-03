package confucian.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import confucian.exception.FrameworkException;

/**
 * 当前操作系统
 */
public class OSName {

    private static final Logger LOGGER = LogManager.getLogger();
    private static String OS = System.getProperty("os.name").toLowerCase();

    /**
     * 获取当前操作系统.
     *
     * @return 当前操作系统
     */
    public static OSN get() {
        LOGGER.debug("系统名称：" + OS);
        if (OS.contains("win")) {
            return OSN.WIN;
        } else if (OS.contains("mac")) {
            return OSN.MAC;
        } else if (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0) {
            return OSN.UNIX;
        } else {
            throw new FrameworkException("无法找到操作系统名称，Java返回：" + OS);
        }
    }

    /**
     * 系统枚举.
     */
    public enum OSN {
        /**
         * Windows.
         */
        WIN, /**
         * Unix.
         */
        UNIX, /**
         * Mac.
         */
        MAC
    }
}
