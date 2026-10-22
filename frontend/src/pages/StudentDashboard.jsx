import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api.js";
import { reference, shortDate, statusClass } from "../format.js";

export default function StudentDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api("/api/student/dashboard")
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
          <h1>Dashboard</h1>
          <p className="muted">An overview of your complaints.</p>
        </div>
        <Link className="btn" to="/student/complaints/new">
          Submit complaint
        </Link>
      </div>

      <div className="stats">
        <Stat label="Total" value={stats.total} />
        <Stat label="Pending" value={stats.pending} tone="pending" />
        <Stat label="In Progress" value={stats.inProgress} tone="progress" />
        <Stat label="Resolved" value={stats.resolved} tone="resolved" />
      </div>

      <section className="panel">
        <div className="panel-head">
          <h2>Recent complaints</h2>
          <Link to="/student/complaints">View all</Link>
        </div>
        {recent.length === 0 ? (
          <p className="empty">No complaints yet.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Ref</th>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {recent.map((complaint) => (
                  <tr key={complaint.complaintId}>
                    <td>
                      <Link to={`/student/complaints/${complaint.complaintId}`}>
                        {reference(complaint.complaintId)}
                      </Link>
                    </td>
                    <td>{complaint.title}</td>
                    <td>{complaint.category}</td>
                    <td>{shortDate(complaint.createdAt)}</td>
                    <td>
                      <span className={statusClass(complaint.status)}>{complaint.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
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
