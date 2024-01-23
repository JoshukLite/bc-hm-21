package org.pool.datasource;

import org.pool.connection.PoolConnection;
import org.pool.exception.PooledConnectionException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Usage
 *
 * <pre>
 * public class Test {
 *     public static void main(String... args) throws Exception {
 *         try (CustomDataSource customDataSource = new CustomDataSource("jdbc:postgresql://localhost:5432/{DATABASE}", "{USER}", "{PASSWORD}")) {
 *             try (Connection connection = customDataSource.getConnection()) {
 *                 Connection conn = cp.getConnection();
 *                 var sql = "SOME SELECT SQL ...";
 *                 conn.createStatement().execute(sql);
 *                 conn.close();
 *             }
 *         }
 *     }
 * }
 * </pre>
 */
public class CustomDataSource implements DataSource, AutoCloseable {

    private static final int DEFAULT_POOL_SIZE = 10;

    private final List<Connection> connectionRegistry = new ArrayList<>(DEFAULT_POOL_SIZE);
    private final BlockingDeque<Connection> connectionPool = new LinkedBlockingDeque<>(DEFAULT_POOL_SIZE);

    public CustomDataSource(String url, String user, String password) {
        initializeConnectionPool(url, user, password);
    }

    private void initializeConnectionPool(String url, String user, String password) {
        try {
            for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
                Connection delegateConnection = DriverManager.getConnection(url, user, password);
                PoolConnection pooledConnection = new PoolConnection(connectionPool, delegateConnection);
                connectionPool.add(pooledConnection);
                connectionRegistry.add(delegateConnection);
            }
        } catch (SQLException e) {
            throw new PooledConnectionException("Could not initialize delegate connection for pool", e);
        }
    }

    public Connection getConnection() {
        try {
            return connectionPool.poll(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PooledConnectionException("Failed to get connection from connection pool", e);
        }
    }

    @Override
    public void close() throws SQLException {
        for (Connection connection : connectionRegistry) {
            connection.close();
        }
    }

    @Override
    public Connection getConnection(String username, String password) {
        throw new UnsupportedOperationException("Method 'getConnection(username, password)' is not supported");
    }

    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("Method 'getLogWriter' is not implemented");
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        throw new UnsupportedOperationException("Method 'setLogWriter(out)' is not implemented");
    }

    @Override
    public void setLoginTimeout(int seconds) {
        throw new UnsupportedOperationException("Method 'setLoginTimeout(seconds)' is not implemented");
    }

    @Override
    public int getLoginTimeout() {
        throw new UnsupportedOperationException("Method 'getLoginTimeout' is not implemented");
    }

    @Override
    public Logger getParentLogger() {
        throw new UnsupportedOperationException("Method 'getParentLogger' is not implemented");
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        throw new UnsupportedOperationException("Method 'unwrap' is not implemented");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        throw new UnsupportedOperationException("Method 'isWrapperFor' is not implemented");
    }
}
