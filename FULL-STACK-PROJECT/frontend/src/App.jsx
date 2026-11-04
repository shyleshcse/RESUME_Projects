import { useState } from "react";
import { Routes, Route, Navigate } from "react-router-dom";

import Navbar from "./components/Navbar.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Doctors from "./pages/Doctors.jsx";
import DoctorSlots from "./pages/DoctorSlots.jsx";
import MyAppointments from "./pages/MyAppointments.jsx";
import DoctorDashboard from "./pages/DoctorDashboard.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";

// After login, each role starts on its own page.
function homePath(user) {
  if (!user) return "/login";
  if (user.role === "ADMIN") return "/admin";
  if (user.role === "DOCTOR") return "/doctor";
  return "/doctors"; // PATIENT
}

export default function App() {
  // "user" = who is logged in: { token, userId, name, email, role } or null.
  // We also keep it in localStorage so a page refresh does not log you out.
  const [user, setUser] = useState(JSON.parse(localStorage.getItem("user") || "null"));

  function handleLogin(loggedInUser) {
    localStorage.setItem("user", JSON.stringify(loggedInUser));
    setUser(loggedInUser);
  }

  function handleLogout() {
    localStorage.removeItem("user");
    setUser(null);
  }

  return (
    <>
      <Navbar user={user} onLogout={handleLogout} />
      <div className="container">
        <Routes>
          <Route path="/" element={<Navigate to={homePath(user)} />} />
          <Route path="/login" element={<Login onLogin={handleLogin} homePath={homePath} />} />
          <Route path="/register" element={<Register onLogin={handleLogin} homePath={homePath} />} />

          {/* PATIENT pages */}
          <Route path="/doctors" element={<ProtectedRoute user={user} role="PATIENT"><Doctors /></ProtectedRoute>} />
          <Route path="/doctors/:id" element={<ProtectedRoute user={user} role="PATIENT"><DoctorSlots /></ProtectedRoute>} />
          <Route path="/my-appointments" element={<ProtectedRoute user={user} role="PATIENT"><MyAppointments /></ProtectedRoute>} />

          {/* DOCTOR page */}
          <Route path="/doctor" element={<ProtectedRoute user={user} role="DOCTOR"><DoctorDashboard /></ProtectedRoute>} />

          {/* ADMIN page */}
          <Route path="/admin" element={<ProtectedRoute user={user} role="ADMIN"><AdminDashboard /></ProtectedRoute>} />

          {/* Anything else */}
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </div>
    </>
  );
}
