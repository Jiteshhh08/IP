import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./AuthContext.jsx";
import Layout from "./components/Layout.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import AdminLogin from "./pages/AdminLogin.jsx";
import StudentDashboard from "./pages/StudentDashboard.jsx";
import Complaints from "./pages/Complaints.jsx";
import SubmitComplaint from "./pages/SubmitComplaint.jsx";
import ComplaintDetail from "./pages/ComplaintDetail.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";
import AdminComplaints from "./pages/AdminComplaints.jsx";
import AdminComplaintDetail from "./pages/AdminComplaintDetail.jsx";
import NotFound from "./pages/NotFound.jsx";

function Protected({ role, children }) {
  const { user, loading } = useAuth();
  if (loading) {
    return <div className="center">Loading…</div>;
  }
  if (!user) {
    return <Navigate to={role === "ADMIN" ? "/admin/login" : "/login"} replace />;
  }
  if (user.role !== role) {
    return <Navigate to={user.role === "ADMIN" ? "/admin" : "/student"} replace />;
  }
  return children;
}

function Home() {
  const { user, loading } = useAuth();
  if (loading) {
    return <div className="center">Loading…</div>;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <Navigate to={user.role === "ADMIN" ? "/admin" : "/student"} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/admin/login" element={<AdminLogin />} />

      <Route
        element={
          <Protected role="STUDENT">
            <Layout />
          </Protected>
        }
      >
        <Route path="/student" element={<StudentDashboard />} />
        <Route path="/student/complaints" element={<Complaints />} />
        <Route path="/student/complaints/new" element={<SubmitComplaint />} />
        <Route path="/student/complaints/:id" element={<ComplaintDetail />} />
      </Route>

      <Route
        element={
          <Protected role="ADMIN">
            <Layout />
          </Protected>
        }
      >
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/complaints" element={<AdminComplaints />} />
        <Route path="/admin/complaints/:id" element={<AdminComplaintDetail />} />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
