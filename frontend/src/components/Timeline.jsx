import { useEffect, useState } from "react";
import { api } from "../api.js";
import { dateTime } from "../format.js";

const TYPE_LABEL = {
  CREATED: "Submitted",
  STATUS: "Status",
  COMMENT: "Comment",
};

export default function Timeline({ endpoint, refreshKey = 0 }) {
  const [entries, setEntries] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  function load() {
    setError("");
    api(endpoint)
      .then(setEntries)
      .catch((err) => setError(err.message));
  }

  useEffect(() => {
    setEntries(null);
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [endpoint, refreshKey]);

  async function submit(event) {
    event.preventDefault();
    if (!message.trim()) {
      return;
    }
    setError("");
    setBusy(true);
    try {
      await api(endpoint, {
        method: "POST",
        body: JSON.stringify({ message }),
      });
      setMessage("");
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="panel">
      <div className="panel-head">
        <h2>Timeline</h2>
        {entries && <span className="muted small">{entries.length} updates</span>}
      </div>

      {error && <div className="alert error">{error}</div>}

      {!entries ? (
        <p className="muted">Loading…</p>
      ) : entries.length === 0 ? (
        <p className="empty">No activity yet.</p>
      ) : (
        <ol className="timeline">
          {entries.map((entry) => (
            <li key={entry.id} className={`timeline-item ${entry.type.toLowerCase()}`}>
              <span className="timeline-dot" />
              <div className="timeline-body">
                <div className="timeline-meta">
                  <strong>{entry.authorName}</strong>
                  <span className={`timeline-tag ${entry.authorRole.toLowerCase()}`}>
                    {entry.authorRole === "ADMIN" ? "Admin" : "Student"}
                  </span>
                  <span className="muted small">{TYPE_LABEL[entry.type] || entry.type}</span>
                  <span className="muted small timeline-date">{dateTime(entry.createdAt)}</span>
                </div>
                <p>{entry.message}</p>
              </div>
            </li>
          ))}
        </ol>
      )}

      <form className="timeline-form" onSubmit={submit}>
        <textarea
          value={message}
          onChange={(event) => setMessage(event.target.value)}
          placeholder="Add a comment…"
          rows={3}
          maxLength={2000}
        />
        <button className="btn" disabled={busy || !message.trim()}>
          {busy ? "Posting…" : "Add comment"}
        </button>
      </form>
    </section>
  );
}
