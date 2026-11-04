import { Link, useNavigate } from "react-router-dom";

// The blue bar at the top. What it shows depends on who is logged in.
export default function Navbar({ user, onLogout }) {
  const navigate = useNavigate();

  function logout() {
    onLogout();
    navigate("/login");
  }

  return (
    <nav className="navbar">
      <span className="brand">🏥 City Hospital</span>

      <div className="nav-links">
        {!user && (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}

        {user && user.role === "PATIENT" && (
          <>
            <Link to="/doctors">Find Doctors</Link>
            <Link to="/my-appointments">My Appointments</Link>
          </>
        )}
        {user && user.role === "DOCTOR" && <Link to="/doctor">My Dashboard</Link>}
        {user && user.role === "ADMIN" && <Link to="/admin">Admin Panel</Link>}

        {user && (
          <>
            <span className="who">
              {user.name} ({user.role})
            </span>
            <button className="btn small" onClick={logout}>Logout</button>
          </>
        )}
      </div>
    </nav>
  );
}
