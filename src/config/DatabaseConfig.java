package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {
    private static final String DB_HOST = System.getProperty("db.host", "127.0.0.1");
    private static final String DB_PORT = System.getProperty("db.port", "3306");
    private static final String DB_NAME = System.getProperty("db.name", "dashboards_populacionais");
    
    private static final String READ_USER = System.getProperty("db.read.user", "user_read");
    private static final String READ_PASS = System.getProperty("db.read.pass", "Kk1M_sQZuGzI6WzL");

    private static final String WRITE_USER = System.getProperty("db.write.user", "insert_user");
    private static final String WRITE_PASS = System.getProperty("db.write.pass", "1iZzBn1fP/2M_Gxc");

    private static final String JDBC_URL = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC", DB_HOST, DB_PORT, DB_NAME);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC do MySQL não encontrado", e);
        }
    }

    private DatabaseConfig() {
    }

    public static Connection getReadOnlyConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL, READ_USER, READ_PASS);
        connection.setReadOnly(true);
        return connection;
    }

    public static Connection getWriteConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL, WRITE_USER, WRITE_PASS);
        connection.setReadOnly(false);
        return connection;
    }

    public static Properties getDatasourceProperties(boolean readOnly) {
        Properties properties = new Properties();
        properties.setProperty("url", JDBC_URL);
        if (readOnly) {
            properties.setProperty("user", READ_USER);
            properties.setProperty("password", READ_PASS);
            properties.setProperty("readOnly", "true");
        } else {
            properties.setProperty("user", WRITE_USER);
            properties.setProperty("password", WRITE_PASS);
            properties.setProperty("readOnly", "false");
        }
        return properties;
    }

    public static SQLException wrapSQLException(String contexto, SQLException e) {
        return new SQLException(contexto + ": " + e.getMessage(), e);
    }
}
