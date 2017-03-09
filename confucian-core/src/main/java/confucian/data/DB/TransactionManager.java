package confucian.data.DB;

import java.sql.Connection;
import java.sql.SQLException;

import confucian.exception.FrameworkException;

/**
 * 事务管理
 */
class TransactionManager {
    private Connection connection;

    /**
     * 实例化一个新的事务管理器
     */
    TransactionManager(Connection connection) {
        this.connection = connection;
    }

    /**
     * Close.
     */
    final void close() {
        try {
            connection.setAutoCommit(true);
            connection.setReadOnly(false);
            connection.close();
        } catch (SQLException e) {
            throw new FrameworkException("无法关闭连接[" + connection + "].", e);
        }
    }

    /**
     * Commit.
     *
     * @throws SQLException the sql exception
     */
    final void commit() throws SQLException {
        connection.commit();
    }

    /**
     * 获得连接
     *
     * @return the connection
     */
    Connection getConnection() {
        return connection;
    }

    /**
     * 回滚
     */
    final void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new FrameworkException("无法回滚连接[" + connection + "].", e);
        }
    }

    /**
     * 开始
     *
     * @throws SQLException the sql exception
     */
    final void start() throws SQLException {
        connection.setAutoCommit(false);
    }
}
