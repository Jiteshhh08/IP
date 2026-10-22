import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api.js";
import { reference, shortDate, statusClass } from "../format.js";

const STATUSES = ["Pending", "In Progress", "Resolved", "Rejected"];

export default function AdminComplaintDetail() {
  const { id } = useParams();
  const [complaint, setComplaint] = useState(null);
  const [status, setStatus] = useState("");
  const [remark, setRemark] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

  function load() {
    api(`/api/admin/complaints/${id}`)
      .then((data) => {
        setComplaint(data);
        setStatus(data.status);
        setRemark(data.adminRemark || "");
      })
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function submit(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    setBusy(true);
    try {
      const updated = await api(`/api/admin/complaints/${id}`, {
        method: "PATCH",
        body: JSON.stringify({ status, remark }),
      });
      setComplaint(updated);
      setMessage("Complaint updated.");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (!complaint && !error) return <p className="muted">Loading…</p>;

  return (
    <>
      <div className="page-head">
        <div>
          <h1>{complaint ? reference(complaint.complaintId) : "Complaint"}</h1>
          {complaint && <p className="muted">Submitted {shortDate(complaint.createdAt)}</p>}
        </div>
        <Link className="btn ghost" to="/admin/complaints">
          Back
        </Link>
      </div>

      {error && <div className="alert error">{error}</div>}
      {message && <div className="alert ok">{message}</div>}

      {complaint && (
        <div className="two-col">
          <section className="panel">
            <div className="detail-row">
              <span>Status</span>
              <span className={statusClass(complaint.status)}>{complaint.status}</span>
            </div>
            <div className="detail-row">
              <span>Student</span>
              <strong>
                {complaint.student.name} ({complaint.student.studentId})
              </strong>
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
              <span>Current response</span>
              <p>{complaint.adminRemark || "No response yet."}</p>
            </div>
          </section>

          <section className="panel">
            <h2>Update complaint</h2>
            <form onSubmit={submit}>
              <label>
                Status
                <select value={status} onChange={(e) => setStatus(e.target.value)}>
                  {STATUSES.map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Remark
                <textarea value={remark} onChange={(e) => setRemark(e.target.value)} rows={5} maxLength={2000} />
              </label>
              <button className="btn" disabled={busy}>
                {busy ? "Saving…" : "Save changes"}
              </button>
            </form>
          </section>
        </div>
      )}
    </>
  );
}
