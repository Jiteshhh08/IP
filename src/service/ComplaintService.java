package service;

import dao.ComplaintDAO;

/**
 * ComplaintService - input validation for complaints.
 * Returns error string, or null on success.
 */
public class ComplaintService {

    public static String validateNew(String title, String category, String description) {
        if (title == null || title.trim().isEmpty()) return "Title is required.";
        if (title.trim().length() > 150) return "Title too long (max 150).";
        if (category == null || !ComplaintDAO.validCategory(category)) return "Choose a valid category.";
        if (description == null || description.trim().isEmpty()) return "Description is required.";
        if (description.trim().length() > 5000) return "Description too long (max 5000).";
        return null;
    }

    public static String validateUpdate(String status, String remark) {
        if (status == null || !ComplaintDAO.validStatus(status)) return "Choose a valid status.";
        if (remark != null && remark.length() > 2000) return "Remark too long (max 2000).";
        return null;
    }
}
