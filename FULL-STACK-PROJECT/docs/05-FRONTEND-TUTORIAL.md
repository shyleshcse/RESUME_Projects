# 05 - Frontend Tutorial (React)

Open `frontend/src/` next to this file.

## Folder map

```
frontend/
├── index.html            Has one empty <div id="root">. React fills it.
├── vite.config.js        Dev server settings + proxy to the backend
├── package.json          Library list (like pom.xml for React)
└── src/
    ├── main.jsx          Starts React
    ├── App.jsx           The "map": which URL shows which page
    ├── api.js            Talks to the backend (adds the token automatically)
    ├── styles.css        All the colours and layout
    ├── components/
    │   ├── Navbar.jsx        Top bar
    │   └── ProtectedRoute.jsx   Blocks pages you should not see
    └── pages/
        ├── Login.jsx
        ├── Register.jsx
        ├── Doctors.jsx           patient: search doctors
        ├── DoctorSlots.jsx       patient: see slots + book
        ├── MyAppointments.jsx    patient: history + cancel
        ├── DoctorDashboard.jsx   doctor
        └── AdminDashboard.jsx    admin
```

## What is Vite?
A tool that runs your React app in the browser while you develop. `npm run dev` starts it on port 5173. Edit a file, save, and the browser updates instantly.

## 3 React ideas you need (with examples)

### 1. Component = a reusable piece of screen
A function that returns HTML-like code (called **JSX**).
```jsx
function Hello() {
  return <h1>Hello</h1>;
}
```
**LEGO example:** each page and the navbar is one LEGO brick. `App.jsx` snaps them together.

### 2. State (`useState`) = a variable that redraws the screen when it changes
```jsx
const [email, setEmail] = useState("");
```
- `email` = current value
- `setEmail("x")` = change it **and** redraw
**Whiteboard example:** state is what is written on the whiteboard. When you rewrite it (`setEmail`), everyone looking at the board sees the new text immediately.

A normal variable (`let email = ""`) would change but the screen would NOT update. That is why we use `useState`.

### 3. Effect (`useEffect`) = "do this when the page opens"
```jsx
useEffect(() => {
  loadDoctors("");
}, []);
```
The empty `[]` means "run once, when the page first appears". We use it to fetch data from the backend.

## main.jsx - the start
```jsx
ReactDOM.createRoot(document.getElementById("root")).render(
  <BrowserRouter><App /></BrowserRouter>
);
```
Puts the `App` into the `<div id="root">`. `BrowserRouter` enables different URLs.

## App.jsx - the map

- Holds `user` state: `{ token, userId, name, email, role }` or `null` when logged out.
- Saves the user in `localStorage` (browser storage) so refreshing the page does not log you out.
- Lists the routes:

| URL | Page | Who |
|-----|------|-----|
| `/login` | Login | everyone |
| `/register` | Register | everyone |
| `/doctors` | Doctors | PATIENT |
| `/doctors/3` | DoctorSlots | PATIENT |
| `/my-appointments` | MyAppointments | PATIENT |
| `/doctor` | DoctorDashboard | DOCTOR |
| `/admin` | AdminDashboard | ADMIN |

`homePath(user)` decides where each role lands after login.

## api.js - the messenger

```jsx
const api = axios.create({ baseURL: "/api" });
```
**axios** is a library that sends HTTP requests. Because of `baseURL`, `api.get("/doctors")` calls `/api/doctors`, and Vite's proxy forwards it to `http://localhost:8080/api/doctors`.

Two **interceptors** (automatic helpers):

1. **Before every request:** read the token from localStorage and add `Authorization: Bearer <token>`. So no page needs to think about tokens.
2. **After every response:** if the server says 401 (token expired), clear the login and go to `/login`.

`getErrorMessage(err)` pulls `{ "error": "..." }` text out of a failed response.

## ProtectedRoute.jsx - page guard
```jsx
if (!user) return <Navigate to="/login" />;
if (user.role !== role) return <Navigate to="/" />;
return children;
```
**Important:** this is only for a nicer screen. A clever user can still try to call APIs directly. The **real** protection is the backend `AuthInterceptor`. Never trust the frontend for security.

## The pages

### Login.jsx
```jsx
const response = await api.post("/auth/login", { email, password });
onLogin(response.data);              // save the user in App state + localStorage
navigate(homePath(response.data));   // go to this role's home page
```
- `async/await` = "wait for the server's answer before the next line".
- `try { ... } catch (err) { setError(...) }` = show the error text if the server says no.
- `event.preventDefault()` = stop the browser's default "reload the page" on form submit.

### Register.jsx
Same pattern, calls `/auth/register`.

### Doctors.jsx
- On open: `GET /api/doctors` -> `setDoctors(response.data)`
- Search box: `GET /api/doctors?specialization=card` (axios builds the `?...` from `params`)
- `doctors.map((doctor) => <div>...</div>)` draws one card per doctor. `key={doctor.id}` helps React track each card.

### DoctorSlots.jsx
- `useParams()` reads the doctor id from the URL (`/doctors/3` -> `id = "3"`).
- Loads the doctor and his **free** slots.
- **Book** button -> `POST /api/appointments { slotId }`. On success shows a green message. On failure (for example 409 slot taken) shows the red message from the server. Either way it reloads the slot list.

### MyAppointments.jsx
- `GET /api/appointments/my` -> table.
- **Cancel** button only shows when `status === "BOOKED"`.
- Coloured badge: CSS class `badge BOOKED / COMPLETED / CANCELLED`.

### DoctorDashboard.jsx
- Form with date, start time, end time -> `POST /api/doctor/slots`
- Table of patients who booked me -> `GET /api/doctor/appointments`, with **Mark Completed**.
- Table of all my slots -> `GET /api/doctor/slots`.

### AdminDashboard.jsx
- Form to add a doctor -> `POST /api/admin/doctors`
- `handleChange` updates one form field using the input's `name`:
  ```jsx
  setForm({ ...form, [event.target.name]: event.target.value });
  ```
  (`...form` copies the old values, `[name]: value` overwrites only the changed one.)
- All doctors and all appointments tables.

### Navbar.jsx
Shows different links depending on `user.role`. Logout clears the user.

## The login flow from the React side

```
Login form -> api.post("/auth/login")
           -> backend returns { token, role, ... }
           -> onLogin() saves it in localStorage + App state
           -> navigate("/doctors")  (or /doctor or /admin)
           -> every later api call automatically carries the token
```

## Handy browser tools
- Press **F12 -> Network tab**: click Book and see the real request and response.
- **F12 -> Application tab -> Local Storage**: see the saved `user` with your token.
- **F12 -> Console tab**: see JavaScript errors.

Next: [06-WORKFLOWS.md](06-WORKFLOWS.md)
