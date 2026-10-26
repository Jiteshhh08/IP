/* student.js - student pages: dashboard, complaints list, submit, detail, profile */
"use strict";

/* ---- /student/dashboard ---- */
async function loadStudentDashboard() {
  const me = await requireRole("STUDENT");
  if (!me) return;
  document.getElementById("hello").textContent = "Good day, " + me.name + " 👋";
  document.getElementById("who").textContent = me.id + (me.email ? " · " + me.email : "");
  try {
    const s = await api("/api/student/stats");
    document.getElementById("stats").innerHTML =
      stat("Total", s.total) + stat("Pending", s.pending)
      + stat("In Progress", s.in_progress) + stat("Resolved", s.resolved);
    const list = await api("/api/student/complaints");
    const tb = document.getElementById("rows");
    if (!list.length) {
      setTableState("rows", 6, '<div class="empty">No complaints yet. Submit your first complaint to get started.</div>');
      return;
    }
    tb.innerHTML = "";
    list.slice(0, 5).forEach(c => {
      const tr = document.createElement("tr");
      tr.innerHTML = "<td>" + esc(fmtId(c.complaint_id)) + "</td><td>" + esc(c.title) + "</td><td>"
        + esc(c.category) + "</td><td>" + esc((c.created_at || "").slice(0, 10)) + "</td><td>"
        + badge(c.status) + '</td><td><a href="/student/complaints/' + c.complaint_id + '">View</a></td>';
      tb.appendChild(tr);
    });
  } catch (e) {
    showErr("err", e.message);
    setTableState("rows", 6, '<div class="empty">Something went wrong. Please try again.</div>');
  }
}
function stat(k, v) {
  return '<div class="stat"><div class="k">' + esc(k) + '</div><div class="v">' + esc(v) + "</div></div>";
}

/* ---- /student/complaints ---- */
async function loadMyComplaints() {
  const me = await requireRole("STUDENT");
  if (!me) return;
  try {
    const list = await api("/api/student/complaints");
    if (!list.length) {
      setTableState("rows", 6, '<div class="empty">No complaints yet. Submit your first complaint to get started.</div>');
      return;
    }
    const tb = document.getElementById("rows");
    tb.innerHTML = "";
    list.forEach(c => {
      const tr = document.createElement("tr");
      tr.innerHTML = "<td>" + esc(fmtId(c.complaint_id)) + "</td><td>" + esc(c.title) + "</td><td>"
        + esc(c.category) + "</td><td>" + badge(c.status) + "</td><td>"
        + esc(c.admin_remark || "-") + '</td><td><a href="/student/complaints/' + c.complaint_id + '">View</a></td>';
      tb.appendChild(tr);
    });
  } catch (e) {
    showErr("err", e.message);
    setTableState("rows", 6, '<div class="empty">Something went wrong. Please try again.</div>');
  }
}

/* ---- /student/complaints/new ---- */
async function submitComplaint() {
  hideAlert("err"); hideAlert("ok");
  const title = document.getElementById("title").value.trim();
  const category = document.getElementById("cat").value;
  const description = document.getElementById("desc").value.trim();
  if (!title) return showErr("err", "Title is required.");
  if (!category) return showErr("err", "Please choose a category.");
  if (!description) return showErr("err", "Description is required.");
  const btn = document.getElementById("btn");
  btn.disabled = true;
  try {
    const r = await api("/api/student/complaints", {
      method: "POST", body: JSON.stringify({ title, category, description })
    });
    showOk("ok", "Submitted as " + fmtId(r.complaint_id) + ". Track it under My Complaints.");
    document.getElementById("title").value = "";
    document.getElementById("desc").value = "";
    document.getElementById("cat").value = "";
  } catch (e) { showErr("err", e.message); }
  btn.disabled = false;
}

/* ---- /student/complaints/{id} ---- */
async function loadComplaintDetail() {
  const me = await requireRole("STUDENT");
  if (!me) return;
  const id = location.pathname.split("/").pop();
  try {
    const c = await api("/api/student/complaints/" + encodeURIComponent(id));
    document.getElementById("head").textContent = "Complaint #" + fmtId(c.complaint_id);
    document.getElementById("info").innerHTML =
      "<div class='detail'><b>Title:</b> " + esc(c.title) + "</div>" +
      "<div class='detail'><b>Category:</b> " + esc(c.category) + "</div>" +
      "<div class='detail'><b>Submitted on:</b> " + esc((c.created_at || "").slice(0, 10)) + "</div>" +
      "<div class='detail'><b>Description:</b><div class='detail-block'>" + esc(c.description) + "</div></div>" +
      "<div class='detail'><b>Status:</b> " + badge(c.status) + "</div>" +
      "<div class='detail'><b>Admin Response:</b><div class='detail-block'>" + esc(c.admin_remark || "-") + "</div></div>";
  } catch (e) { showErr("err", e.message); }
}

/* ---- /student/profile ---- */
async function loadStudentProfile() {
  const me = await requireRole("STUDENT");
  if (!me) return;
  document.getElementById("info").innerHTML =
    "<div class='detail'><b>Name:</b> " + esc(me.name) + "</div>" +
    "<div class='detail'><b>Student ID:</b> " + esc(me.id) + "</div>" +
    "<div class='detail'><b>Email:</b> " + esc(me.email || "-") + "</div>" +
    "<div class='detail'><b>Role:</b> " + esc(me.role) + "</div>";
}
