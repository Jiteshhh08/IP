package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ComplaintDAO;
import model.Complaint;
import model.User;
import service.ComplaintService;
import util.Http;
import util.Json;
import java.io.IOException;

/**
 * ComplaintHandler - student endpoints (STUDENT role required).
 *   GET  /api/student/profile
 *   GET  /api/student/stats
 *   GET  /api/student/complaints          (own complaints only)
 *   POST /api/student/complaints          (student_id from SESSION, not browser)
 *   GET  /api/student/complaints/{id}     (ownership enforced here)
 */
public class ComplaintHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            User u = Http.sessionUser(ex);
            if (u == null) { Http.sendJson(ex, 401, Json.err("Please log in.")); return; }
            if (!u.isStudent()) { Http.sendJson(ex, 403, Json.err("Students only.")); return; }

            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if (path.equals("/api/student/profile") && method.equals("GET")) {
                Http.sendJson(ex, 200, Json.user(u));
                return;
            }

            if (path.equals("/api/student/stats") && method.equals("GET")) {
                Http.sendJson(ex, 200, Json.stats(ComplaintDAO.stats(u.id)));
                return;
            }

            if (path.equals("/api/student/complaints") && method.equals("GET")) {
                Http.sendJson(ex, 200, Json.list(ComplaintDAO.findByStudent(u.id)));
                return;
            }

            if (path.equals("/api/student/complaints") && method.equals("POST")) {
                String body = Http.readBody(ex);
                String title = Http.field(body, "title").trim();
                String category = Http.field(body, "category").trim();
                String desc = Http.field(body, "description").trim();
                String err = ComplaintService.validateNew(title, category, desc);
                if (err != null) { Http.sendJson(ex, 400, Json.err(err)); return; }
                int id = ComplaintDAO.create(u.id, title, category, desc);
                Http.sendJson(ex, 200, "{\"ok\":true,\"complaint_id\":" + id + "}");
                return;
            }

            int id = Http.pathId(ex, "/api/student/complaints/");
            if (id > 0 && method.equals("GET")) {
                Complaint c = ComplaintDAO.findById(id);
                if (c == null) { Http.sendJson(ex, 404, Json.err("Complaint not found.")); return; }
                if (!u.id.equals(c.studentId)) { // ownership: never trust browser
                    Http.sendJson(ex, 403, Json.err("Not your complaint."));
                    return;
                }
                Http.sendJson(ex, 200, Json.one(c));
                return;
            }

            Http.sendJson(ex, 404, Json.err("Unknown API."));
        } catch (Exception e) {
            e.printStackTrace();
            try { Http.sendJson(ex, 500, Json.err("Something went wrong. Please try again.")); }
            catch (Exception ignored) { }
        }
    }
}
