package confucian.report;

/**
 * 在生成报告期间发生不可恢复的错误时抛出未检查的异常。
 */
public class ReportNGException extends RuntimeException {
    public ReportNGException(String string) {
        super(string);
    }


    public ReportNGException(String string, Throwable throwable) {
        super(string, throwable);
    }
}
