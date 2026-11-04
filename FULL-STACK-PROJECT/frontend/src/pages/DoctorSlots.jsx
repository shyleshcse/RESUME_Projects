import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api, { getErrorMessage, formatTime } from "../api.js";

// PATIENT page 2: see free slots of ONE doctor and book one.
export default function DoctorSlots() {
  const { id } = useParams(); // the "5" in the URL /doctors/5
  const [doctor, setDoctor] = useState(null);
  const [slots, setSlots] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadSlots() {
    try {
      const response = await api.get("/doctors/" + id + "/slots");
      setSlots(response.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  useEffect(() => {
    async function loadPage() {
      try {
        const doctorResponse = await api.get("/doctors/" + id);
        setDoctor(doctorResponse.data);
      } catch (err) {
        setError(getErrorMessage(err));
      }
      loadSlots();
    }
    loadPage();
  }, [id]);

  async function book(slotId) {
    setMessage("");
    setError("");
    try {
      // POST /api/appointments   body: { slotId }
      await api.post("/appointments", { slotId });
      setMessage("Appointment booked! See it in 'My Appointments'.");
    } catch (err) {
      // Example: 409 "Sorry, this slot was just booked by someone else"
      setError(getErrorMessage(err));
    }
    loadSlots(); // refresh so the booked slot disappears
  }

  return (
    <div>
      <Link to="/doctors">← Back to doctors</Link>

      {doctor && (
        <h2>
          {doctor.name} <small>({doctor.specialization})</small>
        </h2>
      )}

      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}

      {slots.length === 0 && <p>No free slots available right now.</p>}

      <table>
        <thead>
          <tr><th>Date</th><th>Time</th><th></th></tr>
        </thead>
        <tbody>
          {slots.map((slot) => (
            <tr key={slot.id}>
              <td>{slot.date}</td>
              <td>{formatTime(slot.startTime)} - {formatTime(slot.endTime)}</td>
              <td><button className="btn small" onClick={() => book(slot.id)}>Book</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
