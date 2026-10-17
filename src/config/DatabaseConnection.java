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

    // Same server, but no database selected. Needed to CREATE DATABASE on a
    // fresh machine: connecting straight to a missing DB throws "Unknown database".
    public static Connection getServerConnection() throws Exception {
        return DriverManager.getConnection(serverUrl(), USER, PASSWORD);
    }

    // jdbc:mysql://host:port/db?params -> jdbc:mysql://host:port?params
    static String serverUrl() {
        int q = URL.indexOf('?');
        String base = q >= 0 ? URL.substring(0, q) : URL;
        String params = q >= 0 ? URL.substring(q) : "";
        int dbSlash = databaseSlash(base);
        if (dbSlash >= 0) base = base.substring(0, dbSlash);
        return base + params;
    }

    // Database name from the URL, or null if none is given.
    public static String databaseName() {
        int q = URL.indexOf('?');
        String base = q >= 0 ? URL.substring(0, q) : URL;
        int dbSlash = databaseSlash(base);
        if (dbSlash < 0 || dbSlash + 1 >= base.length()) return null;
        return base.substring(dbSlash + 1);
    }

    static int databaseSlash(String base) {
        int schemeEnd = base.indexOf("://");
        return schemeEnd >= 0 ? base.indexOf('/', schemeEnd + 3) : -1;
    }
}
