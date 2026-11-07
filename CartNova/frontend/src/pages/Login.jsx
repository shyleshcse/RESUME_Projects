import { useState } from 'react'
import { api } from '../api.js'

export default function Login({ onLogin }) {
  const [reg, setReg] = useState(false)
  const [f, setF] = useState({ name: '', email: '', password: '' })
  const [err, setErr] = useState('')
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })

  const submit = async () => {
    try {
      onLogin(await api(reg ? '/auth/register' : '/auth/login', { method: 'POST', body: f }))
    } catch (e) { setErr(e.message) }
  }

  return (
    <div className="box">
      <h2>{reg ? 'Create account' : 'Login'}</h2>
      {reg && <input placeholder="Name" value={f.name} onChange={set('name')} />}
      <input placeholder="Email" value={f.email} onChange={set('email')} />
      <input placeholder="Password" type="password" value={f.password} onChange={set('password')} />
      {err && <p className="err">{err}</p>}
      <button onClick={submit}>{reg ? 'Register' : 'Login'}</button>
      <p className="link" onClick={() => setReg(!reg)}>{reg ? 'Have an account? Login' : 'New here? Register'}</p>
      <small>Demo: user@cartnova.com / user123 &nbsp;|&nbsp; admin@cartnova.com / admin123</small>
    </div>
  )
}
