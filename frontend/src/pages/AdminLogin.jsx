import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext.jsx";

export default function AdminLogin() {
  const { adminLogin } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setError("");
    setBusy(true);
    try {
      await adminLogin(username, password);
      navigate("/admin");
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
          <h1>Administrator login</h1>
          <p className="muted">Manage student complaints.</p>
        </div>
        {error && <div className="alert error">{error}</div>}
        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
        </label>
        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </label>
        <button className="btn" disabled={busy}>
          {busy ? "Signing in…" : "Sign in"}
        </button>
        <p className="auth-foot">
          <Link to="/login">Student login</Link>
        </p>
      </form>
    </div>
  );
}
