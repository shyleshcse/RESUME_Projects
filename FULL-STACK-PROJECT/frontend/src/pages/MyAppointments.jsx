import { useEffect, useState } from "react";
import api, { getErrorMessage, formatTime } from "../api.js";

// PATIENT page 3: appointment history + cancel button.
export default function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState("");

  async function load() {
    try {
      const response = await api.get("/appointments/my");
      setAppointments(response.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function cancel(id) {
    setError("");
    try {
      await api.put("/appointments/" + id + "/cancel");
      load(); // reload the list
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <div>
      <h2>My Appointments</h2>
      {error && <p className="error">{error}</p>}
      {appointments.length === 0 && <p>You have no appointments yet.</p>}

      {appointments.length > 0 && (
        <table>
          <thead>
            <tr><th>Doctor</th><th>Specialization</th><th>Date</th><th>Time</th><th>Status</th><th></th></tr>
          </thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{a.doctorName}</td>
                <td>{a.specialization}</td>
                <td>{a.date}</td>
                <td>{formatTime(a.startTime)} - {formatTime(a.endTime)}</td>
                <td><span className={"badge " + a.status}>{a.status}</span></td>
                <td>
                  {a.status === "BOOKED" && (
                    <button className="btn small danger" onClick={() => cancel(a.id)}>Cancel</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
