import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api.js";
import { reference, shortDate, statusClass } from "../format.js";

const STATUSES = ["Pending", "In Progress", "Resolved", "Rejected"];
const CATEGORIES = ["Academic", "Infrastructure", "Hostel", "Library", "Canteen", "Other"];

export default function AdminComplaints() {
  const [filters, setFilters] = useState({ search: "", status: "", category: "" });
  const [complaints, setComplaints] = useState(null);
  const [error, setError] = useState("");

  function load() {
    const params = new URLSearchParams();
    if (filters.search) params.set("search", filters.search);
    if (filters.status) params.set("status", filters.status);
    if (filters.category) params.set("category", filters.category);
    setError("");
    api(`/api/admin/complaints?${params.toString()}`)
      .then(setComplaints)
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function update(field) {
    return (event) => setFilters({ ...filters, [field]: event.target.value });
  }

  function submit(event) {
    event.preventDefault();
    load();
  }

  return (
    <>
      <div className="page-head">
        <div>
          <h1>All complaints</h1>
          <p className="muted">Search, filter, and manage complaints.</p>
        </div>
      </div>

      <form className="filters" onSubmit={submit}>
        <input
          placeholder="Search ref, student, or title"
          value={filters.search}
          onChange={update("search")}
        />
        <select value={filters.status} onChange={update("status")}>
          <option value="">All statuses</option>
          {STATUSES.map((status) => (
            <option key={status} value={status}>
              {status}
            </option>
          ))}
        </select>
        <select value={filters.category} onChange={update("category")}>
          <option value="">All categories</option>
          {CATEGORIES.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
        </select>
        <button className="btn">Apply</button>
      </form>

      {error && <div className="alert error">{error}</div>}

      <section className="panel">
        {!complaints ? (
          <p className="muted">Loading…</p>
        ) : complaints.length === 0 ? (
          <p className="empty">No complaints match these filters.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Ref</th>
                  <th>Student</th>
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
                    <td>
                      {complaint.student.name}
                      <div className="muted small">{complaint.student.studentId}</div>
                    </td>
                    <td>{complaint.title}</td>
                    <td>{complaint.category}</td>
                    <td>{shortDate(complaint.createdAt)}</td>
                    <td>
                      <span className={statusClass(complaint.status)}>{complaint.status}</span>
                    </td>
                    <td>
                      <Link to={`/admin/complaints/${complaint.complaintId}`}>Manage</Link>
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
