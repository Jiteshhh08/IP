import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api.js";
import { reference, shortDate, statusClass } from "../format.js";

export default function Complaints() {
  const [complaints, setComplaints] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api("/api/student/complaints")
      .then(setComplaints)
      .catch((err) => setError(err.message));
  }, []);

  if (error) return <div className="alert error">{error}</div>;
  if (!complaints) return <p className="muted">Loading…</p>;

  return (
    <>
      <div className="page-head">
        <div>
          <h1>My complaints</h1>
          <p className="muted">All complaints you have submitted.</p>
        </div>
        <Link className="btn" to="/student/complaints/new">
          Submit complaint
        </Link>
      </div>

      <section className="panel">
        {complaints.length === 0 ? (
          <p className="empty">You have not submitted any complaints yet.</p>
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
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {complaints.map((complaint) => (
                  <tr key={complaint.complaintId}>
                    <td>{reference(complaint.complaintId)}</td>
                    <td>{complaint.title}</td>
                    <td>{complaint.category}</td>
                    <td>{shortDate(complaint.createdAt)}</td>
                    <td>
                      <span className={statusClass(complaint.status)}>{complaint.status}</span>
                    </td>
                    <td>
                      <Link to={`/student/complaints/${complaint.complaintId}`}>View</Link>
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
