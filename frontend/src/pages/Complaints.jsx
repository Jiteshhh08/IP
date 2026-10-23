import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api.js";
import { reference, shortDate, statusClass } from "../format.js";

const STATUSES = ["Pending", "In Progress", "Resolved", "Rejected"];
const CATEGORIES = ["Academic", "Infrastructure", "Hostel", "Library", "Canteen", "Other"];

export default function Complaints() {
  const [complaints, setComplaints] = useState(null);
  const [filters, setFilters] = useState({ search: "", status: "", category: "" });
  const [error, setError] = useState("");

  useEffect(() => {
    api("/api/student/complaints")
      .then(setComplaints)
      .catch((err) => setError(err.message));
  }, []);

  const filtered = useMemo(() => {
    if (!complaints) {
      return [];
    }
    const query = filters.search.trim().toLowerCase();
    return complaints.filter((complaint) => {
      if (filters.status && complaint.status !== filters.status) {
        return false;
      }
      if (filters.category && complaint.category !== filters.category) {
        return false;
      }
      if (query && !complaint.title.toLowerCase().includes(query)) {
        return false;
      }
      return true;
    });
  }, [complaints, filters]);

  function update(field) {
    return (event) => setFilters({ ...filters, [field]: event.target.value });
  }

  function clear() {
    setFilters({ search: "", status: "", category: "" });
  }

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

      <form className="filters" onSubmit={(event) => event.preventDefault()}>
        <input
          placeholder="Search by title"
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
        <button type="button" className="btn ghost" onClick={clear}>
          Clear
        </button>
      </form>

      <section className="panel">
        <p className="muted small">
          Showing {filtered.length} of {complaints.length} complaints
        </p>
        {filtered.length === 0 ? (
          <p className="empty">
            {complaints.length === 0
              ? "You have not submitted any complaints yet."
              : "No complaints match these filters."}
          </p>
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
                {filtered.map((complaint) => (
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
