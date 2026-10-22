import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api.js";

const CATEGORIES = ["Academic", "Infrastructure", "Hostel", "Library", "Canteen", "Other"];

export default function SubmitComplaint() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ title: "", category: "", description: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  function update(field) {
    return (event) => setForm({ ...form, [field]: event.target.value });
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    setBusy(true);
    try {
      const created = await api("/api/student/complaints", {
        method: "POST",
        body: JSON.stringify(form),
      });
      navigate(`/student/complaints/${created.complaintId}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Submit a complaint</h1>
          <p className="muted">Describe the issue clearly so it can be resolved quickly.</p>
        </div>
      </div>

      <section className="panel narrow">
        <form onSubmit={submit}>
          {error && <div className="alert error">{error}</div>}
          <label>
            Title
            <input value={form.title} onChange={update("title")} maxLength={150} />
          </label>
          <label>
            Category
            <select value={form.category} onChange={update("category")}>
              <option value="">Select a category</option>
              {CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </label>
          <label>
            Description
            <textarea value={form.description} onChange={update("description")} rows={6} maxLength={5000} />
          </label>
          <button className="btn" disabled={busy}>
            {busy ? "Submitting…" : "Submit complaint"}
          </button>
        </form>
      </section>
    </>
  );
}
