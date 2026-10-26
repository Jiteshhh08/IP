package model;

/**
 * User - one authenticated person. Role is STUDENT or ADMIN.
 * Students log in with student_id or email + password.
 * Admins log in with username + password.
 */
public class User {
    public String id;    // student_id or admin username (students uppercase)
    public String name;
    public String email; // students only, null for admin
    public String role;  // "STUDENT" or "ADMIN"

    public User(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isStudent() { return "STUDENT".equals(role); }
}
