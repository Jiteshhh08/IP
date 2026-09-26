import config.DbInit;
import handler.AdminHandler;
import handler.ComplaintHandler;
import handler.LoginHandler;
import handler.LogoutHandler;
import handler.RegisterHandler;
import handler.StaticHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.net.InetSocketAddress;

/**
 * Main - starts the plain-Java HTTP server (no Tomcat, no Spring).
 *
 * Browser (HTML/CSS/JS)
 *     ↓ HTTP
 * com.sun.net.httpserver.HttpServer   (this class)
 *     ↓ handlers -> services -> DAOs (PreparedStatement)
 * MySQL via JDBC
 *
 * Run:
 *   javac -d out -cp "lib/mysql-connector-j.jar" src/Main.java (plus every file under src)
 *   (simplest: use build.bat / see README for the one-liner)
 *   java -cp "out;lib/mysql-connector-j.jar" Main        (Windows)
 *   java -cp "out:lib/mysql-connector-j.jar" Main        (Mac/Linux)
 * Open http://localhost:8080
 */
public class Main {

    public static String WEB_ROOT = "frontend";
    public static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        // allow running from project root or from src/
        if (!new File(WEB_ROOT + "/index.html").exists() && new File("../frontend/index.html").exists()) {
            WEB_ROOT = "../frontend";
        }
        handler.StaticHandler.setWebRoot(WEB_ROOT);

        // 1. Create tables + seed demo data (idempotent, hashes only)
        try {
            DbInit.ensure();
        } catch (Exception e) {
            System.out.println("[WARN] DB init failed: " + e.getMessage());
            System.out.println("       Is MySQL running? Set DB_URL / DB_USER / DB_PASSWORD.");
        }

        // 2. Start HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/login", new LoginHandler());       // POST login
        server.createContext("/api/me", new LoginHandler());          // GET session user
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/logout", new LogoutHandler());
        server.createContext("/api/student", new ComplaintHandler());
        server.createContext("/api/admin", new AdminHandler());
        server.createContext("/", new StaticHandler());               // pages + static files

        server.setExecutor(null);
        server.start();

        System.out.println("==========================================");
        System.out.println(" Server started!  http://localhost:" + PORT);
        System.out.println(" Frontend: " + new File(WEB_ROOT).getCanonicalPath());
        System.out.println(" Stop: Ctrl+C");
        System.out.println("==========================================");
    }
}
