import { useEffect, useState } from "react";
import api, { getErrorMessage, formatTime } from "../api.js";

// DOCTOR page: add slots, see my slots, see my patients and mark visits as completed.
export default function DoctorDashboard() {
  const [slots, setSlots] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [date, setDate] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadAll() {
    try {
      const slotResponse = await api.get("/doctor/slots");
      const appointmentResponse = await api.get("/doctor/appointments");
      setSlots(slotResponse.data);
      setAppointments(appointmentResponse.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  useEffect(() => {
    loadAll();
  }, []);

  async function addSlot(event) {
    event.preventDefault();
    setMessage("");
    setError("");
    try {
      await api.post("/doctor/slots", { date, startTime, endTime });
      setMessage("Slot added!");
      loadAll();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function complete(id) {
    setError("");
    try {
      await api.put("/doctor/appointments/" + id + "/complete");
      loadAll();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <div>
      <h2>Doctor Dashboard</h2>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}

      <div className="card">
        <h3>Add a new slot</h3>
        <form className="row" onSubmit={addSlot}>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
          <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} required />
          <input type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} required />
          <button className="btn" type="submit">Add Slot</button>
        </form>
      </div>

      <h3>Patients who booked me</h3>
      {appointments.length === 0 && <p>No appointments yet.</p>}
      {appointments.length > 0 && (
        <table>
          <thead>
            <tr><th>Patient</th><th>Date</th><th>Time</th><th>Status</th><th></th></tr>
          </thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{a.patientName}</td>
                <td>{a.date}</td>
                <td>{formatTime(a.startTime)} - {formatTime(a.endTime)}</td>
                <td><span className={"badge " + a.status}>{a.status}</span></td>
                <td>
                  {a.status === "BOOKED" && (
                    <button className="btn small" onClick={() => complete(a.id)}>Mark Completed</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <h3>My slots</h3>
      {slots.length === 0 && <p>You have not added any slots.</p>}
      {slots.length > 0 && (
        <table>
          <thead>
            <tr><th>Date</th><th>Time</th><th>Free / Booked</th></tr>
          </thead>
          <tbody>
            {slots.map((s) => (
              <tr key={s.id}>
                <td>{s.date}</td>
                <td>{formatTime(s.startTime)} - {formatTime(s.endTime)}</td>
                <td>{s.booked ? "Booked" : "Free"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
