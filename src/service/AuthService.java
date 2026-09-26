package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;

/**
 * AuthService - validation + login/register rules.
 * Keeps handlers thin. All auth rules in one place.
 */
public class AuthService {

    public static String validStudentId(String id) {
        if (id == null) return "Student ID required.";
        id = id.trim().toUpperCase();
        if (id.isEmpty()) return "Student ID required.";
        if (!id.matches("[A-Z0-9]{3,20}")) return "Student ID must be 3-20 letters/digits.";
        return null;
    }

    public static String validEmail(String e) {
        if (e == null || e.trim().isEmpty()) return "Email required.";
        if (!e.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) return "Enter a valid email.";
        return null;
    }

    public static String validPassword(String p) {
        if (p == null || p.length() < 6) return "Password must be at least 6 characters.";
        if (p.length() > 100) return "Password too long.";
        return null;
    }

    // Returns User on success, null on bad credentials.
    public static User loginStudent(String idOrEmail, String password) throws Exception {
        if (idOrEmail == null || password == null) return null;
        idOrEmail = idOrEmail.trim();
        String lookup = idOrEmail.contains("@") ? idOrEmail : idOrEmail.toUpperCase();
        String[] row = UserDAO.findStudent(lookup);
        if (row == null) return null;
        if (!PasswordUtil.verify(password, row[3])) return null;
        return new User(row[0], row[1], row[2], "STUDENT");
    }

    // Returns error string, or null on success.
    public static String registerStudent(String studentId, String name, String email, String password) throws Exception {
        String e;
        if ((e = validStudentId(studentId)) != null) return e;
        if (name == null || name.trim().isEmpty() || name.trim().length() > 100) return "Name required.";
        if ((e = validEmail(email)) != null) return e;
        if ((e = validPassword(password)) != null) return e;
        boolean ok = UserDAO.createStudent(studentId.trim().toUpperCase(), name.trim(),
                email.trim().toLowerCase(), PasswordUtil.hash(password));
        if (!ok) return "Student ID or email already registered.";
        return null; // null = success
    }

    public static User loginAdmin(String username, String password) throws Exception {
        if (username == null || password == null) return null;
        String h = UserDAO.findAdminHash(username.trim());
        if (h == null) return null;
        if (!PasswordUtil.verify(password, h)) return null;
        return new User(username.trim(), "Administrator", null, "ADMIN");
    }
}
