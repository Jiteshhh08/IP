package dao;

import config.DatabaseConnection;
import model.Complaint;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * ComplaintDAO - all complaint SQL (INSERT / SELECT / UPDATE only).
 * Ownership is enforced by handlers using the session user, never browser input.
 */
public class ComplaintDAO {

    public static final String[] CATEGORIES = {"Academic", "Infrastructure", "Hostel", "Library", "Canteen", "Other"};
    public static final String[] STATUSES = {"Pending", "In Progress", "Resolved", "Rejected"};

    public static boolean validCategory(String c) {
        for (String k : CATEGORIES) if (k.equals(c)) return true;
        return false;
    }

    public static boolean validStatus(String s) {
        for (String k : STATUSES) if (k.equals(s)) return true;
        return false;
    }

    public static int create(String studentId, String title, String category, String description) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO complaints (student_id, title, category, description, status) "
                    + "VALUES (?, ?, ?, ?, 'Pending')",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, studentId);
            ps.setString(2, title);
            ps.setString(3, category);
            ps.setString(4, description);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = 0;
            if (keys.next()) id = keys.getInt(1);
            keys.close();
            ps.close();
            return id;
        } finally {
            con.close();
        }
    }

    public static List<Complaint> findByStudent(String studentId) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM complaints WHERE student_id = ? ORDER BY complaint_id DESC");
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            List<Complaint> list = new ArrayList<>();
            while (rs.next()) list.add(row(rs));
            rs.close();
            ps.close();
            return list;
        } finally {
            con.close();
        }
    }

    // Admin list with optional search (id/student/title) + status/category filters.
    public static List<Complaint> findAll(String search, String status, String category) throws Exception {
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, s.name AS student_name FROM complaints c "
                + "LEFT JOIN students s ON c.student_id = s.student_id WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (status != null && !status.isEmpty() && validStatus(status)) {
            sql.append(" AND c.status = ?");
            params.add(status);
        }
        if (category != null && !category.isEmpty() && validCategory(category)) {
            sql.append(" AND c.category = ?");
            params.add(category);
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (CAST(c.complaint_id AS CHAR) LIKE ? "
                    + "OR c.student_id LIKE ? OR c.title LIKE ?)");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY c.complaint_id DESC LIMIT 500");

        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            List<Complaint> list = new ArrayList<>();
            while (rs.next()) {
                Complaint c = row(rs);
                try { c.studentName = rs.getString("student_name"); } catch (Exception e) { }
                list.add(c);
            }
            rs.close();
            ps.close();
            return list;
        } finally {
            con.close();
        }
    }

    public static Complaint findById(int id) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT c.*, s.name AS student_name FROM complaints c "
                    + "LEFT JOIN students s ON c.student_id = s.student_id WHERE c.complaint_id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Complaint c = null;
            if (rs.next()) {
                c = row(rs);
                try { c.studentName = rs.getString("student_name"); } catch (Exception e) { }
            }
            rs.close();
            ps.close();
            return c;
        } finally {
            con.close();
        }
    }

    public static void update(int id, String status, String remark) throws Exception {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE complaints SET status = ?, admin_remark = ? WHERE complaint_id = ?");
            ps.setString(1, status);
            ps.setString(2, remark);
            ps.setInt(3, id);
            ps.executeUpdate();
            ps.close();
        } finally {
            con.close();
        }
    }

    // {total, pending, inProgress, resolved, rejected}
    public static int[] stats(String studentIdOrNull) throws Exception {
        List<Complaint> list = studentIdOrNull == null
                ? findAll("", "", "")
                : findByStudent(studentIdOrNull);
        int[] s = new int[5];
        for (Complaint c : list) {
            s[0]++;
            if ("Pending".equals(c.status)) s[1]++;
            else if ("In Progress".equals(c.status)) s[2]++;
            else if ("Resolved".equals(c.status)) s[3]++;
            else if ("Rejected".equals(c.status)) s[4]++;
        }
        return s;
    }

    static Complaint row(ResultSet rs) throws Exception {
        Complaint c = new Complaint();
        c.complaintId = rs.getInt("complaint_id");
        c.studentId = rs.getString("student_id");
        c.title = rs.getString("title");
        c.category = rs.getString("category");
        c.description = rs.getString("description");
        c.status = rs.getString("status");
        c.adminRemark = rs.getString("admin_remark");
        if (c.adminRemark == null) c.adminRemark = "";
        try { c.createdAt = String.valueOf(rs.getTimestamp("created_at")); } catch (Exception e) { c.createdAt = ""; }
        try { c.updatedAt = String.valueOf(rs.getTimestamp("updated_at")); } catch (Exception e) { c.updatedAt = ""; }
        return c;
    }
}
