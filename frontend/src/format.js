export function reference(id) {
  return "C" + String(id).padStart(3, "0");
}

export function shortDate(value) {
  if (!value) {
    return "-";
  }
  return String(value).slice(0, 10);
}

export function statusClass(status) {
  if (status === "Pending") return "badge pending";
  if (status === "In Progress") return "badge progress";
  if (status === "Resolved") return "badge resolved";
  return "badge rejected";
}
