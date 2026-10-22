import { createContext, useContext, useEffect, useState } from "react";
import { api } from "./api.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api("/api/auth/me")
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  async function login(identifier, password) {
    const student = await api("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ identifier, password }),
    });
    const nextUser = {
      id: student.studentId,
      name: student.name,
      email: student.email,
      role: "STUDENT",
    };
    setUser(nextUser);
    return nextUser;
  }

  async function adminLogin(username, password) {
    const admin = await api("/api/auth/admin/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
    setUser(admin);
    return admin;
  }

  async function register(payload) {
    const student = await api("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    const nextUser = {
      id: student.studentId,
      name: student.name,
      email: student.email,
      role: "STUDENT",
    };
    setUser(nextUser);
    return nextUser;
  }

  async function logout() {
    try {
      await api("/api/auth/logout", { method: "POST" });
    } catch {
      // ignore
    }
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, adminLogin, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
