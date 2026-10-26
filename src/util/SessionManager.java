package util;

import model.User;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager - server-side sessions (no JWT, no framework).
 * Cookie: SESSION_ID=<uuid>, HttpOnly, Path=/.
 * Backend is the authority: identity always comes from here, never the browser.
 */
public class SessionManager {
    static class Session {
        User user;
        long expiresAt;
        Session(User u) {
            user = u;
            expiresAt = System.currentTimeMillis() + 8L * 3600 * 1000; // 8 hours
        }
        boolean expired() { return System.currentTimeMillis() > expiresAt; }
    }

    static ConcurrentHashMap<String, Session> store = new ConcurrentHashMap<>();

    public static String create(User u) {
        String id = UUID.randomUUID().toString();
        store.put(id, new Session(u));
        return id;
    }

    public static User get(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return null;
        Session s = store.get(sessionId);
        if (s == null) return null;
        if (s.expired()) { store.remove(sessionId); return null; }
        return s.user;
    }

    public static void destroy(String sessionId) {
        if (sessionId != null) store.remove(sessionId);
    }

    // Parse "Cookie: a=b; SESSION_ID=xyz" -> xyz
    public static String fromHeader(String cookieHeader) {
        if (cookieHeader == null) return null;
        for (String part : cookieHeader.split(";")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].trim().equals("SESSION_ID")) return kv[1].trim();
        }
        return null;
    }

    public static String cookieHeader(String sessionId) {
        return "SESSION_ID=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=28800";
    }

    public static String clearHeader() {
        return "SESSION_ID=; Path=/; HttpOnly; Max-Age=0";
    }
}
