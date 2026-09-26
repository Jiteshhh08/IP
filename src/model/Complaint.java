package model;

/**
 * Complaint - one row of the complaints table.
 */
public class Complaint {
    public int complaintId;
    public String studentId;
    public String studentName; // via JOIN for admin display
    public String title;
    public String category;
    public String description;
    public String status;
    public String adminRemark;
    public String createdAt;
    public String updatedAt;

    public Complaint() {
    }
}
