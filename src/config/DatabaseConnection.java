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

    /**
     * Same URL but with the database segment removed, so we can connect to the
     * server and CREATE DATABASE on a fresh machine. Query params are kept.
     */
    public static Connection getServerConnection() throws Exception {
        return DriverManager.getConnection(serverUrl(URL), USER, PASSWORD);
    }

    /**
     * jdbc:mysql://host:port/db?params  ->  jdbc:mysql://host:port?params
     *
     * The URL is returned unchanged when it has no authority ("://") part, so
     * unusual/custom JDBC URLs never get mangled. Never returns a trailing "/".
     */
    static String serverUrl(String url) {
        if (url == null) return null;
        int q = url.indexOf('?');
        String base = q >= 0 ? url.substring(0, q) : url;
        String params = q >= 0 ? url.substring(q) : "";
        int slash = databaseSlash(base);
        return (slash < 0 ? base : base.substring(0, slash)) + params;
    }

    /**
     * Database name from the URL, or null when the URL has none:
     * no "/db" segment, an empty/trailing slash, or a name that cannot be
     * safely quoted (contains a backtick). Extra path segments are ignored.
     */
    public static String databaseName(String url) {
        if (url == null) return null;
        int q = url.indexOf('?');
        String base = q >= 0 ? url.substring(0, q) : url;
        int slash = databaseSlash(base);
        if (slash < 0 || slash + 1 >= base.length()) return null;
        String path = base.substring(slash + 1);
        int next = path.indexOf('/');            // ignore any extra path segments
        if (next >= 0) path = path.substring(0, next);
        return path.isEmpty() || path.indexOf('`') >= 0 ? null : path;
    }

    // Convenience overloads bound to the configured URL.
    public static String serverUrl() { return serverUrl(URL); }

    public static String databaseName() { return databaseName(URL); }

    /**
     * Index of the "/" that starts the database/path segment, or -1.
     * Skips the "jdbc:<subprotocol>://" prefix, so it also works for
     * jdbc:mysql:replication:// and IPv6 authorities like //[::1]:3306/db.
     */
    static int databaseSlash(String base) {
        int schemeEnd = base.indexOf("://");
        if (schemeEnd < 0) return -1;
        return base.indexOf('/', schemeEnd + 3);
    }
}
