export function reference(id) {
  return "C" + String(id).padStart(3, "0");
}

export function shortDate(value) {
  if (!value) {
    return "-";
  }
  return String(value).slice(0, 10);
}

export function dateTime(value) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value).replace("T", " ").slice(0, 16);
  }
  return date.toLocaleString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function statusClass(status) {
  if (status === "Pending") return "badge pending";
  if (status === "In Progress") return "badge progress";
  if (status === "Resolved") return "badge resolved";
  return "badge rejected";
}
