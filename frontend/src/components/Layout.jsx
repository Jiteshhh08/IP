import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext.jsx";

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isStudent = user.role === "STUDENT";

  const links = isStudent
    ? [
        { to: "/student", label: "Dashboard", end: true },
        { to: "/student/complaints", label: "My Complaints" },
        { to: "/student/complaints/new", label: "Submit Complaint" },
      ]
    : [
        { to: "/admin", label: "Dashboard", end: true },
        { to: "/admin/complaints", label: "All Complaints" },
      ];

  async function handleLogout() {
    await logout();
    navigate("/");
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">CC</span>
          <span>College Complaints</span>
        </div>
        <nav>
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
        <button className="btn ghost logout" onClick={handleLogout}>
          Log out
        </button>
      </aside>

      <main className="main">
        <header className="topbar">
          <div>
            <strong>{user.name}</strong>
            <span className="muted"> · {isStudent ? user.id : "Administrator"}</span>
          </div>
          <span className="role-pill">{user.role}</span>
        </header>
        <div className="content">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
