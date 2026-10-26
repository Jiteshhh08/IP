const BASE = import.meta.env.VITE_API_BASE || "";

export async function api(path, options = {}) {
  const { headers, ...rest } = options;
  const response = await fetch(BASE + path, {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(headers || {}) },
    ...rest,
  });
  let data = null;
  try {
    data = await response.json();
  } catch {
    data = null;
  }
  if (!response.ok) {
    const error = new Error((data && data.error) || `Request failed (${response.status})`);
    error.status = response.status;
    throw error;
  }
  return data;
}
