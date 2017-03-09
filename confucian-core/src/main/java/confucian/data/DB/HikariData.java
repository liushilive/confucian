package confucian.data.DB;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import confucian.exception.FrameworkException;

/**
 * 数据库公共类
 */
public class HikariData {
    private static HikariDataSource ds = null;

    /**
     * Close Connection
     *
     * @param connection the connection
     */
    public static void close(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new FrameworkException("无法关闭连接[" + connection + "].", e);
        }
    }

    /**
     * Close.
     */
    public static void close() {
        if (ds != null)
            synchronized (HikariData.class) {
                if (!ds.isClosed())
                    ds.close();
            }
    }

    /**
     * Gets connection.
     *
     * @return the connection
     */
    public static Connection getConnection() {
        try {
            if (ds != null)
                if (!ds.isClosed())
                    return ds.getConnection();
                else throw new FrameworkException("数据库连接关闭，连接池已释放！");
            else throw new FrameworkException("数据库连接为空，检查是否初始化；");
        } catch (SQLException e) {
            throw new FrameworkException("Sql连接错误 ", e);
        }
    }

    /**
     * Gets hikeri data soure.
     *
     * @return the hikeri data soure
     */
    public static HikariDataSource getDataSoure() {
        if (ds == null)
            throw new FrameworkException("数据库连接为空，检查是否初始化");
        else
            return ds;
    }

    /**
     * Gets hikeri data soure.
     *
     * @param dbcURL   the dbc url
     * @param username the username
     * @param password the password
     *
     * @return the hikeri data soure
     */
    public static void init(String dbcURL, String username, String password) {
        if (ds == null) {
            synchronized (HikariData.class) {
                if (ds == null) {
                    ds = new HikariDataSource();
                    ds.setJdbcUrl(dbcURL);
                    ds.setUsername(username);
                    ds.setPassword(password);
                    ds.setMaximumPoolSize(100);
                }
            }
        }
    }
}
