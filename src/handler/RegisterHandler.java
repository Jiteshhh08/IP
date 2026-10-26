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
 * RegisterHandler - POST /api/register
 * Creates a student (password is hashed) and logs them in.
 */
public class RegisterHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!ex.getRequestMethod().equals("POST")) {
                Http.sendJson(ex, 405, Json.err("Method not allowed."));
                return;
            }
            String body = Http.readBody(ex);
            String err = AuthService.registerStudent(
                    Http.field(body, "student_id"),
                    Http.field(body, "name"),
                    Http.field(body, "email"),
                    Http.field(body, "password"));
            if (err != null) {
                Http.sendJson(ex, 400, Json.err(err));
                return;
            }
            User u = AuthService.loginStudent(Http.field(body, "student_id"), Http.field(body, "password"));
            ex.getResponseHeaders().set("Set-Cookie", SessionManager.cookieHeader(SessionManager.create(u)));
            Http.sendJson(ex, 200, Json.user(u));
        } catch (Exception e) {
            e.printStackTrace();
            try { Http.sendJson(ex, 500, Json.err("Something went wrong. Please try again.")); }
            catch (Exception ignored) { }
        }
    }
}
