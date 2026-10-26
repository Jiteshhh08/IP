/* auth.js - login / register pages (student + admin). */
"use strict";

async function studentLogin() {
  hideAlert("err");
  const id = document.getElementById("id").value.trim();
  const pw = document.getElementById("pw").value;
  if (!id) return showErr("err", "Student ID or email is required.");
  if (!pw) return showErr("err", "Password is required.");
  const btn = document.getElementById("btn");
  btn.disabled = true;
  try {
    await api("/api/login", { method: "POST", body: JSON.stringify({ id, password: pw }) });
    window.location.href = "/student/dashboard";
  } catch (e) { showErr("err", e.message); btn.disabled = false; }
}

async function adminLogin() {
  hideAlert("err");
  const username = document.getElementById("u").value.trim();
  const password = document.getElementById("p").value;
  if (!username || !password) return showErr("err", "Username and password are required.");
  const btn = document.getElementById("btn");
  btn.disabled = true;
  try {
    await api("/api/admin/login", { method: "POST", body: JSON.stringify({ username, password }) });
    window.location.href = "/admin/dashboard";
  } catch (e) { showErr("err", e.message); btn.disabled = false; }
}

async function register() {
  hideAlert("err"); hideAlert("ok");
  const body = {
    student_id: document.getElementById("sid").value.trim(),
    name: document.getElementById("name").value.trim(),
    email: document.getElementById("email").value.trim(),
    password: document.getElementById("pw").value
  };
  if (!body.student_id) return showErr("err", "Student ID is required.");
  if (!body.name) return showErr("err", "Name is required.");
  if (!body.email) return showErr("err", "Email is required.");
  if (!body.password || body.password.length < 6) return showErr("err", "Password must be at least 6 characters.");
  const btn = document.getElementById("btn");
  btn.disabled = true;
  try {
    await api("/api/register", { method: "POST", body: JSON.stringify(body) });
    showOk("ok", "Account created! Redirecting to dashboard…");
    setTimeout(() => window.location.href = "/student/dashboard", 800);
  } catch (e) { showErr("err", e.message); btn.disabled = false; }
}

/* Enter key submits the form */
function bindEnter(formFn) {
  document.addEventListener("keydown", e => {
    if (e.key === "Enter" && e.target.tagName === "INPUT") { e.preventDefault(); formFn(); }
  });
}
