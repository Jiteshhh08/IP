package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ComplaintDAO;
import model.Complaint;
import model.User;
import service.AuthService;
import service.ComplaintService;
import util.Http;
import util.Json;
import util.SessionManager;
import java.io.IOException;

/**
 * AdminHandler - admin endpoints (ADMIN role required, except login).
 *   POST /api/admin/login
 *   GET  /api/admin/stats
 *   GET  /api/admin/complaints?search=&status=&category=
 *   GET  /api/admin/complaints/{id}
 *   PUT  /api/admin/complaints/{id}   {status, admin_remark}
 *
 * A STUDENT calling these gets 403 Forbidden.
 */
public class AdminHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            // --- login is public ---
            if (path.equals("/api/admin/login") && method.equals("POST")) {
                String body = Http.readBody(ex);
                User u = AuthService.loginAdmin(Http.field(body, "username"), Http.field(body, "password"));
                if (u == null) {
                    Http.sendJson(ex, 401, Json.err("Wrong username or password."));
                    return;
                }
                ex.getResponseHeaders().set("Set-Cookie", SessionManager.cookieHeader(SessionManager.create(u)));
                Http.sendJson(ex, 200, Json.user(u));
                return;
            }

            // --- everything below requires ADMIN session ---
            User u = Http.sessionUser(ex);
            if (u == null) { Http.sendJson(ex, 401, Json.err("Please log in.")); return; }
            if (!u.isAdmin()) { Http.sendJson(ex, 403, Json.err("Admins only.")); return; }

            if (path.equals("/api/admin/stats") && method.equals("GET")) {
                Http.sendJson(ex, 200, Json.stats(ComplaintDAO.stats(null)));
                return;
            }

            if (path.equals("/api/admin/complaints") && method.equals("GET")) {
                Http.sendJson(ex, 200, Json.list(ComplaintDAO.findAll(
                        Http.query(ex, "search"),
                        Http.query(ex, "status"),
                        Http.query(ex, "category"))));
                return;
            }

            int id = Http.pathId(ex, "/api/admin/complaints/");
            if (id > 0 && method.equals("GET")) {
                Complaint c = ComplaintDAO.findById(id);
                if (c == null) { Http.sendJson(ex, 404, Json.err("Complaint not found.")); return; }
                Http.sendJson(ex, 200, Json.one(c));
                return;
            }

            if (id > 0 && method.equals("PUT")) {
                String body = Http.readBody(ex);
                String status = Http.field(body, "status").trim();
                String remark = Http.field(body, "admin_remark").trim();
                String err = ComplaintService.validateUpdate(status, remark);
                if (err != null) { Http.sendJson(ex, 400, Json.err(err)); return; }
                if (ComplaintDAO.findById(id) == null) {
                    Http.sendJson(ex, 404, Json.err("Complaint not found."));
                    return;
                }
                ComplaintDAO.update(id, status, remark);
                Http.sendJson(ex, 200, Json.ok());
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
