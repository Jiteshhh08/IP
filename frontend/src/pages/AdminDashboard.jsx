import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api.js";
import { reference, shortDate, statusClass } from "../format.js";

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api("/api/admin/dashboard")
      .then(setData)
      .catch((err) => setError(err.message));
  }, []);

  if (error) return <div className="alert error">{error}</div>;
  if (!data) return <p className="muted">Loading…</p>;

  const { stats, recent } = data;

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Admin dashboard</h1>
          <p className="muted">All complaints across the college.</p>
        </div>
        <Link className="btn" to="/admin/complaints">
          Manage complaints
        </Link>
      </div>

      <div className="stats">
        <Stat label="Total" value={stats.total} />
        <Stat label="Pending" value={stats.pending} tone="pending" />
        <Stat label="In Progress" value={stats.inProgress} tone="progress" />
        <Stat label="Resolved" value={stats.resolved} tone="resolved" />
        <Stat label="Rejected" value={stats.rejected} tone="rejected" />
      </div>

      <section className="panel">
        <div className="panel-head">
          <h2>Recent complaints</h2>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Ref</th>
                <th>Student</th>
                <th>Title</th>
                <th>Date</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {recent.map((complaint) => (
                <tr key={complaint.complaintId}>
                  <td>
                    <Link to={`/admin/complaints/${complaint.complaintId}`}>
                      {reference(complaint.complaintId)}
                    </Link>
                  </td>
                  <td>
                    {complaint.student.name} ({complaint.student.studentId})
                  </td>
                  <td>{complaint.title}</td>
                  <td>{shortDate(complaint.createdAt)}</td>
                  <td>
                    <span className={statusClass(complaint.status)}>{complaint.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}

function Stat({ label, value, tone }) {
  return (
    <div className={`stat ${tone || ""}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
