import axios from "axios";

// One axios object used by every page to talk to the backend.
// baseURL "/api" + vite proxy  =>  http://localhost:8080/api/...
const api = axios.create({ baseURL: "/api" });

// BEFORE every request: attach the login token (the "wristband") if we have one.
api.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem("user") || "null");
  if (user && user.token) {
    config.headers.Authorization = "Bearer " + user.token;
  }
  return config;
});

// AFTER every response: if the token is expired/invalid (401), log the user out.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthCall = error.config && error.config.url.startsWith("/auth/");
    if (error.response && error.response.status === 401 && !isAuthCall) {
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

// Gets the readable error text our backend sends: { "error": "..." }
export function getErrorMessage(error) {
  if (error.response && error.response.data && error.response.data.error) {
    return error.response.data.error;
  }
  return "Cannot reach the server. Is the backend running on port 8080?";
}

export default api;

// The backend sends times like "10:00:00". We only want to show "10:00".
export function formatTime(time) {
  return time ? time.slice(0, 5) : "";
}
