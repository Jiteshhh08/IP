package config;

import dao.ComplaintDAO;
import dao.UserDAO;
import util.PasswordUtil;

/**
 * DbInit - creates tables + seeds demo data.
 * Runs automatically on server start (idempotent). Passwords are hashes.
 *
 * Defaults: admin/admin123, STU001..STU004/pass123
 * Also upgrades the OLD MVP schema (no password_hash) by recreating tables.
 */
public class DbInit {

    public static void ensure() throws Exception {
        // 1. Server-level connection (no database) so we can create it on a
        //    fresh machine. Connecting straight to a missing DB would fail.
        String db = DatabaseConnection.databaseName();
        if (db != null) {
            var server = DatabaseConnection.getServerConnection();
            var s0 = server.createStatement();
            s0.execute("CREATE DATABASE IF NOT EXISTS `" + db + "`");
            s0.close();
            server.close();
        } else {
            System.out.println("[WARN] DB_URL has no usable database name; skipping auto-create.");
            System.out.println("       Run database/schema.sql and use a URL that includes the database.");
        }

        // 2. Now connect to the database and ensure the tables exist.
        var con = DatabaseConnection.getConnection();
        var st = con.createStatement();
        if (db != null) st.execute("USE `" + db + "`");

        if (oldSchema(st)) {
            System.out.println("[INFO] Old schema detected (no password_hash) -> recreating tables.");
            st.execute("SET FOREIGN_KEY_CHECKS=0");
            st.execute("DROP TABLE IF EXISTS complaints");
            st.execute("DROP TABLE IF EXISTS students");
            st.execute("DROP TABLE IF EXISTS admin");
            st.execute("SET FOREIGN_KEY_CHECKS=1");
        }

        st.execute("CREATE TABLE IF NOT EXISTS students ("
                + "student_id VARCHAR(20) PRIMARY KEY,"
                + "name VARCHAR(100) NOT NULL,"
                + "email VARCHAR(150) NOT NULL UNIQUE,"
                + "password_hash VARCHAR(255) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        st.execute("CREATE TABLE IF NOT EXISTS admin ("
                + "username VARCHAR(50) PRIMARY KEY,"
                + "password_hash VARCHAR(255) NOT NULL)");
        st.execute("CREATE TABLE IF NOT EXISTS complaints ("
                + "complaint_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "student_id VARCHAR(20) NOT NULL,"
                + "title VARCHAR(150) NOT NULL,"
                + "category VARCHAR(50) NOT NULL,"
                + "description TEXT NOT NULL,"
                + "status VARCHAR(20) NOT NULL DEFAULT 'Pending',"
                + "admin_remark TEXT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,"
                + "INDEX idx_complaint_student (student_id),"
                + "INDEX idx_complaint_status (status),"
                + "CHECK (status IN ('Pending','In Progress','Resolved','Rejected')))");
        st.close();
        con.close();
        seed();
    }

    static boolean oldSchema(java.sql.Statement st) {
        try {
            return (tableExists(st, "students") && !columnExists(st, "students", "password_hash"))
                    || (tableExists(st, "admin") && !columnExists(st, "admin", "password_hash"));
        } catch (Exception e) {
            return false;
        }
    }

    static boolean tableExists(java.sql.Statement st, String table) throws Exception {
        var rs = st.executeQuery("SELECT COUNT(*) FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + table + "'");
        rs.next();
        boolean ex = rs.getInt(1) > 0;
        rs.close();
        return ex;
    }

    static boolean columnExists(java.sql.Statement st, String table, String column) throws Exception {
        var rs = st.executeQuery("SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + table
                + "' AND COLUMN_NAME = '" + column + "'");
        rs.next();
        boolean ex = rs.getInt(1) > 0;
        rs.close();
        return ex;
    }

    public static void seed() throws Exception {
        if (UserDAO.findAdminHash("admin") == null) {
            UserDAO.createAdmin("admin", PasswordUtil.hash("admin123"));
            System.out.println("Seeded admin/admin123");
        }
        seedStudent("STU001", "Rahul", "rahul@college.edu", "pass123");
        seedStudent("STU002", "Priya Sharma", "priya@college.edu", "pass123");
        seedStudent("STU003", "Amit Kumar", "amit@college.edu", "pass123");
        seedStudent("STU004", "Sneha Patel", "sneha@college.edu", "pass123");

        if (ComplaintDAO.stats(null)[0] == 0) {
            ComplaintDAO.create("STU001", "Fan not working", "Infrastructure",
                    "Fan in classroom 203 is not working.");
            int b = ComplaintDAO.create("STU002", "Library timing", "Library",
                    "Please extend library timing during exams.");
            ComplaintDAO.update(b, "Resolved", "Timing has been updated to 9 PM.");
            int c = ComplaintDAO.create("STU001", "Book not available", "Library",
                    "Data Structures book is not available in library.");
            ComplaintDAO.update(c, "In Progress", "New books have been ordered.");
            ComplaintDAO.create("STU003", "Mess food quality", "Canteen",
                    "Canteen food quality is very poor since last week.");
            int e = ComplaintDAO.create("STU002", "WiFi not working in hostel", "Hostel",
                    "Hostel Block B WiFi is down for 3 days.");
            ComplaintDAO.update(e, "Rejected", "WiFi is managed by outside vendor. Complaint closed.");
            ComplaintDAO.create("STU004", "Fees receipt not received", "Academic",
                    "Paid semester fees but did not get receipt.");
            System.out.println("Seeded 6 sample complaints.");
        }
    }

    static void seedStudent(String id, String name, String email, String pass) throws Exception {
        if (UserDAO.findStudent(id) == null) {
            UserDAO.createStudent(id, name, email, PasswordUtil.hash(pass));
            System.out.println("Seeded " + id + "/" + pass);
        }
    }
}
