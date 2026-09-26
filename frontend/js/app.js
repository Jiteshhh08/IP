/* app.js - shared helpers: API client, escaping, badges, role guard.
   MySQL via Java is the single source of truth. No hardcoded data. */
"use strict";

async function api(path, opts) {
  opts = opts || {};
  opts.credentials = "include"; // send SESSION_ID cookie
  opts.headers = Object.assign({ "Content-Type": "application/json" }, opts.headers || {});
  const res = await fetch(path, opts);
  let data = null;
  try { data = await res.json(); } catch (e) { data = null; }
  if (!res.ok) throw new Error((data && data.error) || ("Request failed (" + res.status + ")"));
  return data;
}

function esc(s) {
  return String(s == null ? "" : s)
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}
function fmtId(n) {
  n = Number(n);
  if (n < 10) return "C00" + n;
  if (n < 100) return "C0" + n;
  return "C" + n;
}
function badge(status) {
  const cls = status === "Pending" ? "pending"
    : status === "In Progress" ? "progress"
    : status === "Resolved" ? "resolved" : "rejected";
  return '<span class="badge ' + cls + '">' + esc(status) + "</span>";
}
function showErr(id, msg) {
  const el = document.getElementById(id);
  if (!el) return;
  el.className = "alert error show";
  el.textContent = msg;
}
function showOk(id, msg) {
  const el = document.getElementById(id);
  if (!el) return;
  el.className = "alert ok show";
  el.textContent = msg;
}
function hideAlert(id) {
  const el = document.getElementById(id);
  if (el) el.className = "alert";
}
function setTableState(id, colCount, html) {
  const el = document.getElementById(id);
  if (el) el.innerHTML = '<tr><td colspan="' + colCount + '">' + html + "</td></tr>";
}

/* Guard a page: require a session with the given role, else redirect.
   Server enforces this too (StaticHandler) - this is just UX. */
async function requireRole(role) {
  try {
    const me = await api("/api/me");
    if (me.role !== role) {
      window.location.replace(me.role === "ADMIN" ? "/admin/dashboard" : "/student/dashboard");
      return null;
    }
    return me;
  } catch (e) {
    window.location.replace(role === "ADMIN" ? "/admin/login" : "/login");
    return null;
  }
}

async function logout() {
  try { await api("/api/logout", { method: "POST" }); } catch (e) {}
  window.location.href = "/";
}
