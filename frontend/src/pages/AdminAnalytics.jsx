import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api.js";

const STATUSES = ["Pending", "In Progress", "Resolved", "Rejected"];
const CATEGORIES = ["Academic", "Infrastructure", "Hostel", "Library", "Canteen", "Other"];

export default function AdminAnalytics() {
  const [complaints, setComplaints] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api("/api/admin/complaints")
      .then(setComplaints)
      .catch((err) => setError(err.message));
  }, []);

  const analytics = useMemo(() => {
    if (!complaints) {
      return null;
    }
    const total = complaints.length;
    const byStatus = STATUSES.map((status) => ({
      label: status,
      value: complaints.filter((complaint) => complaint.status === status).length,
    }));
    const byCategory = CATEGORIES.map((category) => ({
      label: category,
      value: complaints.filter((complaint) => complaint.category === category).length,
    }));
    const months = [];
    const now = new Date();
    for (let index = 5; index >= 0; index -= 1) {
      const date = new Date(now.getFullYear(), now.getMonth() - index, 1);
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
      months.push({
        key,
        label: date.toLocaleString("en-US", { month: "short" }),
        value: complaints.filter(
          (complaint) => String(complaint.createdAt).slice(0, 7) === key
        ).length,
      });
    }
    const resolved = complaints.filter((complaint) => complaint.status === "Resolved").length;
    const open =
      complaints.filter(
        (complaint) =>
          complaint.status === "Pending" || complaint.status === "In Progress"
      ).length;
    return {
      total,
      resolved,
      open,
      rate: total ? Math.round((resolved / total) * 100) : 0,
      byStatus,
      byCategory,
      months,
    };
  }, [complaints]);

  if (error) {
    return <div className="alert error">{error}</div>;
  }
  if (!analytics) {
    return <p className="muted">Loading…</p>;
  }

  const maxStatus = Math.max(1, ...analytics.byStatus.map((item) => item.value));
  const maxCategory = Math.max(1, ...analytics.byCategory.map((item) => item.value));
  const maxMonth = Math.max(1, ...analytics.months.map((item) => item.value));

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Analytics</h1>
          <p className="muted">Complaint trends across the college.</p>
        </div>
        <Link className="btn ghost" to="/admin/complaints">
          All complaints
        </Link>
      </div>

      <div className="stats">
        <Stat label="Total" value={analytics.total} />
        <Stat label="Open" value={analytics.open} tone="pending" />
        <Stat label="Resolved" value={analytics.resolved} tone="resolved" />
        <Stat label="Resolution rate" value={`${analytics.rate}%`} tone="progress" />
      </div>

      <div className="chart-grid">
        <section className="panel">
          <h2>By status</h2>
          <div className="bars">
            {analytics.byStatus.map((item) => (
              <Bar
                key={item.label}
                label={item.label}
                value={item.value}
                max={maxStatus}
                tone={toneFor(item.label)}
              />
            ))}
          </div>
        </section>

        <section className="panel">
          <h2>By category</h2>
          <div className="bars">
            {analytics.byCategory.map((item) => (
              <Bar
                key={item.label}
                label={item.label}
                value={item.value}
                max={maxCategory}
              />
            ))}
          </div>
        </section>
      </div>

      <section className="panel">
        <h2>Last 6 months</h2>
        <div className="trend">
          {analytics.months.map((month) => (
            <div className="trend-col" key={month.key}>
              <span className="trend-value">{month.value}</span>
              <div className="trend-track">
                <div
                  className="trend-fill"
                  style={{ height: `${(month.value / maxMonth) * 100}%` }}
                />
              </div>
              <span className="trend-label">{month.label}</span>
            </div>
          ))}
        </div>
      </section>
    </>
  );
}

function Bar({ label, value, max, tone }) {
  return (
    <div className="bar-row">
      <span className="bar-label">{label}</span>
      <div className="bar-track">
        <div
          className={`bar-fill ${tone || ""}`}
          style={{ width: `${max ? (value / max) * 100 : 0}%` }}
        />
      </div>
      <strong className="bar-value">{value}</strong>
    </div>
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

function toneFor(status) {
  if (status === "Pending") return "pending";
  if (status === "In Progress") return "progress";
  if (status === "Resolved") return "resolved";
  return "rejected";
}
