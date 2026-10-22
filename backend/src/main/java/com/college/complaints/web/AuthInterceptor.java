package com.college.complaints.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute("user");
        String path = request.getRequestURI();

        if (path.startsWith("/api/student")) {
            if (user == null) {
                return deny(response, 401, "Please log in.");
            }
            if (!"STUDENT".equals(user.role())) {
                return deny(response, 403, "Students only.");
            }
        } else if (path.startsWith("/api/admin")) {
            if (user == null) {
                return deny(response, 401, "Please log in.");
            }
            if (!"ADMIN".equals(user.role())) {
                return deny(response, 403, "Admins only.");
            }
        }
        return true;
    }

    private boolean deny(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
        return false;
    }
}
