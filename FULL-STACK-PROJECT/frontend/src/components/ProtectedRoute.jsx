import { Navigate } from "react-router-dom";

// A page guard.
//  - Not logged in?          -> send to /login
//  - Logged in, wrong role?  -> send to "/" (which sends you to YOUR home page)
// Note: this only hides pages. The REAL protection is in the backend (AuthInterceptor).
export default function ProtectedRoute({ user, role, children }) {
  if (!user) return <Navigate to="/login" />;
  if (user.role !== role) return <Navigate to="/" />;
  return children;
}
