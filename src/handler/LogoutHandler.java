package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.Http;
import util.Json;
import util.SessionManager;
import java.io.IOException;

/**
 * LogoutHandler - POST /api/logout
 * Destroys the server-side session and clears the cookie.
 */
public class LogoutHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            SessionManager.destroy(Http.sessionId(ex));
            ex.getResponseHeaders().set("Set-Cookie", SessionManager.clearHeader());
            Http.sendJson(ex, 200, Json.ok());
        } catch (Exception e) {
            try { Http.sendJson(ex, 500, Json.err("Logout failed.")); }
            catch (Exception ignored) { }
        }
    }
}
