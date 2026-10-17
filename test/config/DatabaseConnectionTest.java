package config;

/**
 * DatabaseConnectionTest - plain-Java tests for the JDBC URL parsing used to
 * auto-create the database. No JUnit, no external dependencies.
 *
 * Run (from project root, PowerShell):
 *   javac -d out-test -cp out test/config/DatabaseConnectionTest.java
 *   java -cp "out-test;out" config.DatabaseConnectionTest
 * Mac/Linux: use ":" instead of ";" in the -cp value.
 */
public class DatabaseConnectionTest {

    static int passed = 0;
    static int failed = 0;

    static void eq(String label, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (ok) {
            passed++;
            System.out.println("  PASS  " + label);
        } else {
            failed++;
            System.out.println("  FAIL  " + label);
            System.out.println("          expected: " + expected);
            System.out.println("          actual:   " + actual);
        }
    }

    public static void main(String[] args) {
        // Typical URL with query params.
        String u1 = "jdbc:mysql://localhost:3306/student_complaint_system"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        eq("db name (typical)", "student_complaint_system", DatabaseConnection.databaseName(u1));
        eq("server url (typical) keeps params",
                "jdbc:mysql://localhost:3306?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                DatabaseConnection.serverUrl(u1));

        // No query params.
        String u2 = "jdbc:mysql://localhost:3306/shop";
        eq("db name (no params)", "shop", DatabaseConnection.databaseName(u2));
        eq("server url (no params)", "jdbc:mysql://localhost:3306", DatabaseConnection.serverUrl(u2));

        // No database segment at all.
        String u3 = "jdbc:mysql://localhost:3306?useSSL=false";
        eq("db name (none)", null, DatabaseConnection.databaseName(u3));
        eq("server url (none) unchanged", "jdbc:mysql://localhost:3306?useSSL=false",
                DatabaseConnection.serverUrl(u3));

        // Trailing slash -> empty database.
        String u4 = "jdbc:mysql://localhost:3306/?useSSL=false";
        eq("db name (trailing slash)", null, DatabaseConnection.databaseName(u4));
        eq("server url (trailing slash)", "jdbc:mysql://localhost:3306?useSSL=false",
                DatabaseConnection.serverUrl(u4));

        // No port.
        String u5 = "jdbc:mysql://dbhost/app";
        eq("db name (no port)", "app", DatabaseConnection.databaseName(u5));
        eq("server url (no port)", "jdbc:mysql://dbhost", DatabaseConnection.serverUrl(u5));

        // IPv6 authority.
        String u6 = "jdbc:mysql://[::1]:3306/campus?useSSL=false";
        eq("db name (ipv6)", "campus", DatabaseConnection.databaseName(u6));
        eq("server url (ipv6)", "jdbc:mysql://[::1]:3306?useSSL=false", DatabaseConnection.serverUrl(u6));

        // Replication subprotocol (multiple hosts, no double slash before ://).
        String u7 = "jdbc:mysql:replication://master:3306,slave:3306/mydb?useSSL=false";
        eq("db name (replication)", "mydb", DatabaseConnection.databaseName(u7));
        eq("server url (replication)", "jdbc:mysql:replication://master:3306,slave:3306?useSSL=false",
                DatabaseConnection.serverUrl(u7));

        // Extra path segment -> only the first segment is the database.
        String u8 = "jdbc:mysql://localhost:3306/mydb/extra?x=1";
        eq("db name (extra path)", "mydb", DatabaseConnection.databaseName(u8));
        eq("server url (extra path strips all)", "jdbc:mysql://localhost:3306?x=1",
                DatabaseConnection.serverUrl(u8));

        // No authority part.
        String u9 = "jdbc:mysql:somewhere";
        eq("db name (no authority)", null, DatabaseConnection.databaseName(u9));
        eq("server url (no authority) unchanged", "jdbc:mysql:somewhere", DatabaseConnection.serverUrl(u9));

        // Null / empty.
        eq("db name (null)", null, DatabaseConnection.databaseName(null));
        eq("server url (null)", null, DatabaseConnection.serverUrl(null));
        eq("db name (empty)", null, DatabaseConnection.databaseName(""));
        eq("server url (empty) unchanged", "", DatabaseConnection.serverUrl(""));

        // Unusable name (backtick cannot be quoted safely in CREATE DATABASE).
        String u10 = "jdbc:mysql://localhost:3306/bad`name?x=1";
        eq("db name (backtick rejected)", null, DatabaseConnection.databaseName(u10));
        eq("server url (backtick) still strips path", "jdbc:mysql://localhost:3306?x=1",
                DatabaseConnection.serverUrl(u10));

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
