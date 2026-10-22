import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api.js";
import { reference, shortDate, statusClass } from "../format.js";

export default function ComplaintDetail() {
  const { id } = useParams();
  const [complaint, setComplaint] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api(`/api/student/complaints/${id}`)
      .then(setComplaint)
      .catch((err) => setError(err.message));
  }, [id]);

  if (error) return <div className="alert error">{error}</div>;
  if (!complaint) return <p className="muted">Loading…</p>;

  return (
    <>
      <div className="page-head">
        <div>
          <h1>{reference(complaint.complaintId)}</h1>
          <p className="muted">Submitted {shortDate(complaint.createdAt)}</p>
        </div>
        <Link className="btn ghost" to="/student/complaints">
          Back
        </Link>
      </div>

      <section className="panel">
        <div className="detail-row">
          <span>Status</span>
          <span className={statusClass(complaint.status)}>{complaint.status}</span>
        </div>
        <div className="detail-row">
          <span>Category</span>
          <strong>{complaint.category}</strong>
        </div>
        <div className="detail-row">
          <span>Title</span>
          <strong>{complaint.title}</strong>
        </div>
        <div className="detail-block">
          <span>Description</span>
          <p>{complaint.description}</p>
        </div>
        <div className="detail-block">
          <span>Admin response</span>
          <p>{complaint.adminRemark || "No response yet."}</p>
        </div>
      </section>
    </>
  );
}
