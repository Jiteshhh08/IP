import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext.jsx";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ studentId: "", name: "", email: "", password: "" });
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
      await register(form);
      navigate("/student");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-wrap">
      <form className="auth-card" onSubmit={submit}>
        <div className="auth-head">
          <span className="brand-mark">CC</span>
          <h1>Create account</h1>
          <p className="muted">Register to submit and track complaints.</p>
        </div>
        {error && <div className="alert error">{error}</div>}
        <label>
          Student ID
          <input value={form.studentId} onChange={update("studentId")} />
        </label>
        <label>
          Full name
          <input value={form.name} onChange={update("name")} />
        </label>
        <label>
          Email
          <input type="email" value={form.email} onChange={update("email")} />
        </label>
        <label>
          Password
          <input type="password" value={form.password} onChange={update("password")} />
        </label>
        <button className="btn" disabled={busy}>
          {busy ? "Creating…" : "Create account"}
        </button>
        <p className="auth-foot">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  );
}
