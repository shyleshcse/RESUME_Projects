import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api, { getErrorMessage } from "../api.js";

export default function Login({ onLogin, homePath }) {
  // useState = a variable that makes the page redraw when it changes
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  async function handleSubmit(event) {
    event.preventDefault(); // stop the browser from reloading the page
    setError("");
    try {
      // POST /api/auth/login  ->  { token, userId, name, email, role }
      const response = await api.post("/auth/login", { email, password });
      onLogin(response.data);                 // save the user (and token)
      navigate(homePath(response.data));      // go to the correct home page
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <div className="card form-card">
      <h2>Login</h2>
      {error && <p className="error">{error}</p>}

      <form onSubmit={handleSubmit}>
        <label>Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

        <button className="btn" type="submit">Login</button>
      </form>

      <p>New patient? <Link to="/register">Create an account</Link></p>

      <div className="hint">
        <b>Demo logins</b><br />
        Patient: patient@test.com / patient123<br />
        Doctor: priya@hospital.com / doctor123<br />
        Admin: admin@hospital.com / admin123
      </div>
    </div>
  );
}
