package util;

import model.Complaint;
import model.User;
import java.util.List;

/**
 * Json - tiny JSON builder with proper escaping (XSS-safe transport).
 * Frontend renders with esc()/textContent, so stored data can't execute.
 */
public class Json {
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }

    public static String ok() { return "{\"ok\":true}"; }

    public static String err(String msg) { return "{\"error\":\"" + escape(msg) + "\"}"; }

    public static String user(User u) {
        return "{\"id\":\"" + escape(u.id) + "\",\"name\":\"" + escape(u.name) + "\""
                + ",\"email\":\"" + escape(u.email == null ? "" : u.email) + "\""
                + ",\"role\":\"" + escape(u.role) + "\"}";
    }

    public static String one(Complaint c) {
        return "{\"complaint_id\":" + c.complaintId
                + ",\"student_id\":\"" + escape(c.studentId) + "\""
                + ",\"student_name\":\"" + escape(c.studentName == null ? "" : c.studentName) + "\""
                + ",\"title\":\"" + escape(c.title) + "\""
                + ",\"category\":\"" + escape(c.category) + "\""
                + ",\"description\":\"" + escape(c.description) + "\""
                + ",\"status\":\"" + escape(c.status) + "\""
                + ",\"admin_remark\":\"" + escape(c.adminRemark) + "\""
                + ",\"created_at\":\"" + escape(c.createdAt) + "\""
                + ",\"updated_at\":\"" + escape(c.updatedAt) + "\"}";
    }

    public static String list(List<Complaint> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(one(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String stats(int[] s) {
        return "{\"total\":" + s[0] + ",\"pending\":" + s[1]
                + ",\"in_progress\":" + s[2] + ",\"resolved\":" + s[3]
                + ",\"rejected\":" + s[4] + "}";
    }
}
