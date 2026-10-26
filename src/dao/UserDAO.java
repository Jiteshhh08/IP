package dao;

import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * UserDAO - all auth SQL. Every query uses PreparedStatement (no string concat).
 */
public class UserDAO {

    // Register new student. Returns false if ID or email already taken.
    public static boolean createStudent(String studentId, String name, String email, String passwordHash) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement dup = con.prepareStatement(
                    "SELECT student_id FROM students WHERE student_id = ? OR email = ?");
            dup.setString(1, studentId);
            dup.setString(2, email);
            ResultSet rs = dup.executeQuery();
            boolean exists = rs.next();
            rs.close();
            dup.close();
            if (exists) return false;

            PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO students (student_id, name, email, password_hash) VALUES (?, ?, ?, ?)");
            ins.setString(1, studentId);
            ins.setString(2, name);
            ins.setString(3, email);
            ins.setString(4, passwordHash);
            ins.executeUpdate();
            ins.close();
            return true;
        } finally {
            con.close();
        }
    }

    // Load student by ID or email (login accepts either).
    // Returns {student_id, name, email, password_hash} or null.
    public static String[] findStudent(String idOrEmail) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT student_id, name, email, password_hash FROM students "
                    + "WHERE student_id = ? OR email = ? LIMIT 1");
            ps.setString(1, idOrEmail);
            ps.setString(2, idOrEmail);
            ResultSet rs = ps.executeQuery();
            String[] out = null;
            if (rs.next()) {
                out = new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)};
            }
            rs.close();
            ps.close();
            return out;
        } finally {
            con.close();
        }
    }

    public static String findAdminHash(String username) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT password_hash FROM admin WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            String h = null;
            if (rs.next()) h = rs.getString(1);
            rs.close();
            ps.close();
            return h;
        } finally {
            con.close();
        }
    }

    public static void createAdmin(String username, String passwordHash) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT IGNORE INTO admin (username, password_hash) VALUES (?, ?)");
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
            ps.close();
        } finally {
            con.close();
        }
    }
}
