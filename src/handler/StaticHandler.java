package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import util.Http;
import util.SessionManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * StaticHandler - serves frontend files with clean routes and page guards.
 *
 * Clean routes:
 *   /                       -> index.html            (public)
 *   /login                  -> login.html            (public)
 *   /register               -> register.html         (public)
 *   /admin/login            -> admin-login.html      (public)
 *   /student/dashboard      -> student-dashboard.html (STUDENT)
 *   /student/complaints     -> complaints.html        (STUDENT)
 *   /student/complaints/new -> submit-complaint.html  (STUDENT)
 *   /student/complaints/{id}-> complaint-details.html (STUDENT)
 *   /student/profile        -> profile.html           (STUDENT)
 *   /admin/dashboard        -> admin-dashboard.html   (ADMIN)
 *   /admin/complaints/{id}  -> admin-complaint.html   (ADMIN)
 *   /admin/profile          -> profile.html           (ADMIN)
 *   /css/*, /js/*, ...      -> static assets
 *
 * Unauthenticated on a protected page -> 302 to login.
 * Wrong role -> 302 to the user's own dashboard.
 */
public class StaticHandler implements HttpHandler {

    static String WEB_ROOT = "frontend"; // set by Main at startup

    public static void setWebRoot(String dir) { WEB_ROOT = dir; }

    static final Map<String, String> ROUTES = new HashMap<>();
    static {
        ROUTES.put("/", "index.html");
        ROUTES.put("/login", "login.html");
        ROUTES.put("/register", "register.html");
        ROUTES.put("/admin/login", "admin-login.html");
        ROUTES.put("/student/dashboard", "student-dashboard.html");
        ROUTES.put("/student/complaints", "complaints.html");
        ROUTES.put("/student/complaints/new", "submit-complaint.html");
        ROUTES.put("/student/profile", "profile.html");
        ROUTES.put("/admin/dashboard", "admin-dashboard.html");
        ROUTES.put("/admin/profile", "profile.html");
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        User user = SessionManager.get(Http.sessionId(ex));

        try {
            String file;
            if (path.matches("/student/complaints/\\d+")) {
                if (!guard(ex, user, "STUDENT")) return;
                file = "complaint-details.html";
            } else if (path.matches("/admin/complaints/\\d+")) {
                if (!guard(ex, user, "ADMIN")) return;
                file = "admin-complaint.html";
            } else if (ROUTES.containsKey(path)) {
                if (path.startsWith("/student/") && !guard(ex, user, "STUDENT")) return;
                if (path.startsWith("/admin/") && !path.equals("/admin/login")
                        && !guard(ex, user, "ADMIN")) return; // /admin/login is public
                file = ROUTES.get(path);
            } else {
                // static asset: /css/style.css, /js/app.js, favicon...
                file = path.startsWith("/") ? path.substring(1) : path;
                // also guard protected pages requested directly as .html
                if (file.equals("student-dashboard.html") || file.equals("complaints.html")
                        || file.equals("submit-complaint.html") || file.equals("complaint-details.html")) {
                    if (!guard(ex, user, "STUDENT")) return;
                } else if (file.equals("admin-dashboard.html") || file.equals("admin-complaint.html")) {
                    if (!guard(ex, user, "ADMIN")) return;
                } else if (file.equals("profile.html")) {
                    if (user == null) { Http.redirect(ex, "/login"); return; }
                }
            }

            File f = new File(WEB_ROOT, file);
            if (!f.getCanonicalPath().startsWith(new File(WEB_ROOT).getCanonicalPath())) {
                Http.sendText(ex, 403, "Forbidden");
                return;
            }
            if (!f.exists() || f.isDirectory()) {
                Http.sendText(ex, 404, "Page not found: " + path);
                return;
            }
            serve(ex, f);
        } catch (Exception e) {
            e.printStackTrace();
            Http.sendText(ex, 500, "Server error.");
        }
    }

    /**
     * Returns true if access is allowed (page will be served).
     * Otherwise sends the redirect and returns false.
     */
    static boolean guard(HttpExchange ex, User user, String role) throws IOException {
        if (user == null) {
            Http.redirect(ex, "ADMIN".equals(role) ? "/admin/login" : "/login");
            return false;
        }
        if ("STUDENT".equals(role) && !user.isStudent()) {
            Http.redirect(ex, "/admin/dashboard");
            return false;
        }
        if ("ADMIN".equals(role) && !user.isAdmin()) {
            Http.redirect(ex, "/student/dashboard");
            return false;
        }
        return true;
    }

    static void serve(HttpExchange ex, File f) throws IOException {
        String name = f.getName();
        String type = name.endsWith(".css") ? "text/css"
                : name.endsWith(".js") ? "application/javascript"
                : name.endsWith(".ico") ? "image/x-icon"
                : "text/html";
        byte[] b = Files.readAllBytes(f.toPath());
        ex.getResponseHeaders().set("Content-Type", type + "; charset=UTF-8");
        ex.sendResponseHeaders(200, b.length);
        var os = ex.getResponseBody();
        os.write(b);
        os.close();
    }
}
