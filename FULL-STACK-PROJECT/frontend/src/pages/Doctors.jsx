import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { getErrorMessage } from "../api.js";

// PATIENT page 1: search doctors by specialization.
export default function Doctors() {
  const [doctors, setDoctors] = useState([]);
  const [search, setSearch] = useState("");
  const [error, setError] = useState("");

  // Ask the backend for doctors. If "text" is empty we get all of them.
  async function loadDoctors(text) {
    try {
      const response = await api.get("/doctors", { params: { specialization: text } });
      setDoctors(response.data);
      setError("");
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  // useEffect with [] = run ONCE when the page opens
  useEffect(() => {
    loadDoctors("");
  }, []);

  function handleSearch(event) {
    event.preventDefault();
    loadDoctors(search);
  }

  return (
    <div>
      <h2>Find a Doctor</h2>

      <form className="row" onSubmit={handleSearch}>
        <input
          placeholder="Search by specialization (e.g. cardio, skin, child)"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button className="btn" type="submit">Search</button>
      </form>

      {error && <p className="error">{error}</p>}
      {doctors.length === 0 && !error && <p>No doctors found.</p>}

      <div className="grid">
        {doctors.map((doctor) => (
          <div className="card" key={doctor.id}>
            <h3>{doctor.name}</h3>
            <p><b>{doctor.specialization}</b></p>
            <p>{doctor.experienceYears} years experience</p>
            <Link className="btn" to={"/doctors/" + doctor.id}>View Slots</Link>
          </div>
        ))}
      </div>
    </div>
  );
}
