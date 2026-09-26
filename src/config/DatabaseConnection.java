package config;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * DatabaseConnection - single place for MySQL credentials.
 * Reads environment variables first, falls back to defaults.
 * Never commit real passwords: set DB_PASSWORD on shared machines.
 *
 *   Windows: set DB_PASSWORD=secret
 *   Linux:   export DB_PASSWORD=secret
 */
public class DatabaseConnection {
    public static final String URL = env("DB_URL",
            "jdbc:mysql://localhost:3306/student_complaint_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    public static final String USER = env("DB_USER", "root");
    public static final String PASSWORD = env("DB_PASSWORD", "");

    static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
