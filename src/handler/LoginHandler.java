package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import service.AuthService;
import util.Http;
import util.Json;
import util.SessionManager;
import java.io.IOException;

/**
 * LoginHandler - student login + current-session check.
 *   POST /api/login     {id, password}   -> sets SESSION_ID cookie
 *   GET  /api/me                         -> current session user or 401
 */
public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();

            if (path.equals("/api/login") && method.equals("POST")) {
                String body = Http.readBody(ex);
                String id = Http.field(body, "id");
                if (id.isEmpty()) id = Http.field(body, "student_id");
                String password = Http.field(body, "password");
                User u = AuthService.loginStudent(id, password);
                if (u == null) {
                    Http.sendJson(ex, 401, Json.err("Invalid ID/email or password."));
                    return;
                }
                ex.getResponseHeaders().set("Set-Cookie", SessionManager.cookieHeader(SessionManager.create(u)));
                Http.sendJson(ex, 200, Json.user(u));
                return;
            }

            if (path.equals("/api/me") && method.equals("GET")) {
                User u = Http.sessionUser(ex);
                if (u == null) { Http.sendJson(ex, 401, Json.err("Please log in.")); return; }
                Http.sendJson(ex, 200, Json.user(u));
                return;
            }

            Http.sendJson(ex, 404, Json.err("Unknown API."));
        } catch (Exception e) {
            e.printStackTrace(); // server log only
            try { Http.sendJson(ex, 500, Json.err("Something went wrong. Please try again.")); }
            catch (Exception ignored) { }
        }
    }
}
