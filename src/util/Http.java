package util;

import com.sun.net.httpserver.HttpExchange;
import model.User;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

/**
 * Http - small shared helpers for handlers: JSON in/out, redirects,
 * session lookup, query/body parsing.
 */
public class Http {

    // ---- session shortcuts ----
    public static String sessionId(HttpExchange ex) {
        return SessionManager.fromHeader(ex.getRequestHeaders().getFirst("Cookie"));
    }

    public static User sessionUser(HttpExchange ex) {
        return SessionManager.get(sessionId(ex));
    }

    // ---- responses ----
    public static void sendJson(HttpExchange ex, int code, String json) throws java.io.IOException {
        byte[] b = json.getBytes("UTF-8");
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(code, b.length);
        OutputStream os = ex.getResponseBody();
        os.write(b);
        os.close();
    }

    public static void sendText(HttpExchange ex, int code, String msg) throws java.io.IOException {
        byte[] b = msg.getBytes("UTF-8");
        ex.sendResponseHeaders(code, b.length);
        OutputStream os = ex.getResponseBody();
        os.write(b);
        os.close();
    }

    public static void redirect(HttpExchange ex, String to) throws java.io.IOException {
        ex.getResponseHeaders().set("Location", to);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    // ---- request parsing ----
    public static String readBody(HttpExchange ex) throws java.io.IOException {
        InputStream is = ex.getRequestBody();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
            if (bos.size() > 100000) break; // 100KB cap
        }
        return bos.toString("UTF-8");
    }

    // Extract one value from a flat JSON body: getField(body, "email")
    // Kept tiny on purpose - no JSON library needed for our simple forms.
    public static String field(String json, String key) {
        String q = "\"" + key + "\"";
        int i = json.indexOf(q);
        if (i == -1) return "";
        int colon = json.indexOf(":", i + q.length());
        if (colon == -1) return "";
        int s = colon + 1;
        while (s < json.length() && (json.charAt(s) == ' ' || json.charAt(s) == '\n'
                || json.charAt(s) == '\r' || json.charAt(s) == '\t')) s++;
        if (s >= json.length()) return "";
        if (json.charAt(s) == '"') {
            StringBuilder sb = new StringBuilder();
            for (int k = s + 1; k < json.length(); k++) {
                char ch = json.charAt(k);
                if (ch == '\\' && k + 1 < json.length()) {
                    char nx = json.charAt(k + 1);
                    if (nx == '"') sb.append('"');
                    else if (nx == 'n') sb.append('\n');
                    else if (nx == '\\') sb.append('\\');
                    else sb.append(nx);
                    k++;
                } else if (ch == '"') break;
                else sb.append(ch);
            }
            return sb.toString().trim();
        }
        int e = s;
        while (e < json.length() && json.charAt(e) != ',' && json.charAt(e) != '}') e++;
        return json.substring(s, e).trim().replace("\"", "");
    }

    public static String query(HttpExchange ex, String key) {
        String q = ex.getRequestURI().getQuery();
        if (q == null) return "";
        for (String part : q.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                try { return URLDecoder.decode(kv[1], "UTF-8"); }
                catch (Exception e) { return kv[1]; }
            }
        }
        return "";
    }

    public static int pathId(HttpExchange ex, String prefix) {
        // "/api/student/complaints/12" with prefix "/api/student/complaints/" -> 12
        String p = ex.getRequestURI().getPath();
        if (!p.startsWith(prefix)) return -1;
        String tail = p.substring(prefix.length());
        if (!tail.matches("\\d+")) return -1;
        return Integer.parseInt(tail);
    }
}

