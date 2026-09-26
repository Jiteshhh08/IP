/* admin.js - admin pages: dashboard (stats + search/filter), complaint update, profile */
"use strict";

async function loadAdminDashboard() {
  const me = await requireRole("ADMIN");
  if (!me) return;
  try {
    const s = await api("/api/admin/stats");
    document.getElementById("stats").innerHTML =
      stat("Total", s.total) + stat("Pending", s.pending) + stat("In Progress", s.in_progress)
      + stat("Resolved", s.resolved) + stat("Rejected", s.rejected);
  } catch (e) { showErr("err", e.message); }
  loadAdminList();
}
function stat(k, v) {
  return '<div class="stat"><div class="k">' + esc(k) + '</div><div class="v">' + esc(v) + "</div></div>";
}

let adminListTimer = null;
async function loadAdminList() {
  clearTimeout(adminListTimer);
  adminListTimer = setTimeout(async () => {
    const q = document.getElementById("q").value.trim();
    const st = document.getElementById("fstatus").value;
    const cat = document.getElementById("fcat").value;
    setTableState("rows", 7, '<div class="loading">Loading…</div>');
    try {
      const list = await api("/api/admin/complaints?search=" + encodeURIComponent(q)
        + "&status=" + encodeURIComponent(st) + "&category=" + encodeURIComponent(cat));
      if (!list.length) {
        setTableState("rows", 7, '<div class="empty">No complaints match these filters.</div>');
        return;
      }
      const tb = document.getElementById("rows");
      tb.innerHTML = "";
      list.forEach(c => {
        const tr = document.createElement("tr");
        tr.innerHTML = "<td>" + esc(fmtId(c.complaint_id)) + "</td><td>" + esc(c.student_id) + "</td><td>"
          + esc(c.title) + "</td><td>" + esc(c.category) + "</td><td>" + esc((c.created_at || "").slice(0, 10))
          + "</td><td>" + badge(c.status) + '</td><td><a href="/admin/complaints/' + c.complaint_id + '">View</a></td>';
        tb.appendChild(tr);
      });
    } catch (e) {
      showErr("err", e.message);
      setTableState("rows", 7, '<div class="empty">Something went wrong. Please try again.</div>');
    }
  }, 250);
}

/* ---- /admin/complaints/{id} : view + update ---- */
let adminCid = null;
async function loadAdminComplaint() {
  const me = await requireRole("ADMIN");
  if (!me) return;
  adminCid = location.pathname.split("/").pop();
  try {
    const c = await api("/api/admin/complaints/" + encodeURIComponent(adminCid));
    document.getElementById("head").textContent = "Complaint #" + fmtId(c.complaint_id);
    document.getElementById("info").innerHTML =
      "<div class='detail'><b>Student:</b> " + esc(c.student_name || "") + " (" + esc(c.student_id) + ")</div>" +
      "<div class='detail'><b>Title:</b> " + esc(c.title) + "</div>" +
      "<div class='detail'><b>Category:</b> " + esc(c.category) + "</div>" +
      "<div class='detail'><b>Submitted:</b> " + esc((c.created_at || "").slice(0, 10)) + "</div>" +
      "<div class='detail'><b>Description:</b><div class='detail-block'>" + esc(c.description) + "</div></div>" +
      "<div class='detail'><b>Status:</b> " + badge(c.status) + "</div>" +
      "<div class='detail'><b>Current Response:</b><div class='detail-block'>" + esc(c.admin_remark || "-") + "</div></div>";
    document.getElementById("status").value = c.status;
    document.getElementById("remark").value = c.admin_remark || "";
  } catch (e) { showErr("err", e.message); }
}

async function updateComplaint() {
  hideAlert("err"); hideAlert("ok");
  const status = document.getElementById("status").value;
  const admin_remark = document.getElementById("remark").value.trim();
  if (!status) return showErr("err", "Please select a status.");
  const btn = document.getElementById("btn");
  btn.disabled = true;
  try {
    await api("/api/admin/complaints/" + encodeURIComponent(adminCid), {
      method: "PUT", body: JSON.stringify({ status, admin_remark })
    });
    showOk("ok", "Updated. The student sees this immediately from the database.");
    setTimeout(() => loadAdminComplaint(), 600);
  } catch (e) { showErr("err", e.message); btn.disabled = false; }
}

/* ---- /admin/profile ---- */
async function loadAdminProfile() {
  const me = await requireRole("ADMIN");
  if (!me) return;
  document.getElementById("info").innerHTML =
    "<div class='detail'><b>Username:</b> " + esc(me.id) + "</div>" +
    "<div class='detail'><b>Name:</b> " + esc(me.name) + "</div>" +
    "<div class='detail'><b>Role:</b> " + esc(me.role) + "</div>";
}
