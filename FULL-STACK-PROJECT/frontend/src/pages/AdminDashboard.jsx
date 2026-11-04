import { useEffect, useState } from "react";
import api, { getErrorMessage, formatTime } from "../api.js";

// ADMIN page: add a doctor, see all doctors, see every appointment.
export default function AdminDashboard() {
  const [doctors, setDoctors] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    specialization: "",
    experienceYears: "",
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadAll() {
    try {
      const doctorResponse = await api.get("/doctors");
      const appointmentResponse = await api.get("/admin/appointments");
      setDoctors(doctorResponse.data);
      setAppointments(appointmentResponse.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  useEffect(() => {
    loadAll();
  }, []);

  // One function updates whichever input changed (uses the input's name attribute)
  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function addDoctor(event) {
    event.preventDefault();
    setMessage("");
    setError("");
    try {
      await api.post("/admin/doctors", {
        ...form,
        experienceYears: Number(form.experienceYears) || 0,
      });
      setMessage("Doctor added! They can now login with the email and password you typed.");
      setForm({ name: "", email: "", password: "", specialization: "", experienceYears: "" });
      loadAll();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <div>
      <h2>Admin Panel</h2>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}

      <div className="card">
        <h3>Add a new doctor</h3>
        <form onSubmit={addDoctor}>
          <input name="name" placeholder="Doctor name (Dr. ...)" value={form.name} onChange={handleChange} required />
          <input name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
          <input name="password" type="password" placeholder="Password (min 6)" value={form.password} onChange={handleChange} required />
          <input name="specialization" placeholder="Specialization (e.g. Neurology)" value={form.specialization} onChange={handleChange} required />
          <input name="experienceYears" type="number" min="0" placeholder="Years of experience" value={form.experienceYears} onChange={handleChange} />
          <button className="btn" type="submit">Add Doctor</button>
        </form>
      </div>

      <h3>All doctors ({doctors.length})</h3>
      <table>
        <thead>
          <tr><th>Name</th><th>Email</th><th>Specialization</th><th>Experience</th></tr>
        </thead>
        <tbody>
          {doctors.map((d) => (
            <tr key={d.id}>
              <td>{d.name}</td>
              <td>{d.email}</td>
              <td>{d.specialization}</td>
              <td>{d.experienceYears} yrs</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h3>All appointments ({appointments.length})</h3>
      {appointments.length === 0 && <p>No appointments yet.</p>}
      {appointments.length > 0 && (
        <table>
          <thead>
            <tr><th>Patient</th><th>Doctor</th><th>Date</th><th>Time</th><th>Status</th></tr>
          </thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{a.patientName}</td>
                <td>{a.doctorName} ({a.specialization})</td>
                <td>{a.date}</td>
                <td>{formatTime(a.startTime)} - {formatTime(a.endTime)}</td>
                <td><span className={"badge " + a.status}>{a.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
