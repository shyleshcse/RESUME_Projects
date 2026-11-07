import { useState } from 'react'
import { Link, Route, Routes, useNavigate } from 'react-router-dom'
import Products from './pages/Products.jsx'
import Cart from './pages/Cart.jsx'
import Orders from './pages/Orders.jsx'
import Login from './pages/Login.jsx'
import Admin from './pages/Admin.jsx'

export default function App() {
  const nav = useNavigate()
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('user') || 'null'))
  const [cart, setCart] = useState([])

  const login = (data) => {
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data))
    setUser(data)
    nav(data.role === 'ADMIN' ? '/admin' : '/')
  }
  const logout = () => { localStorage.clear(); setUser(null); setCart([]); nav('/') }

  const addToCart = (p) =>
    setCart((c) => {
      const f = c.find((i) => i.id === p.id)
      return f ? c.map((i) => (i.id === p.id ? { ...i, quantity: i.quantity + 1 } : i)) : [...c, { ...p, quantity: 1 }]
    })

  return (
    <>
      <header className="nav">
        <Link to="/" className="logo">🛍️ CartNova</Link>
        <nav>
          <Link to="/cart">Cart ({cart.reduce((s, i) => s + i.quantity, 0)})</Link>
          {user && <Link to="/orders">My Orders</Link>}
          {user?.role === 'ADMIN' && <Link to="/admin">Admin</Link>}
          {user ? <button onClick={logout}>Logout ({user.name})</button> : <Link to="/login">Login</Link>}
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Products addToCart={addToCart} />} />
          <Route path="/cart" element={<Cart cart={cart} setCart={setCart} user={user} />} />
          <Route path="/orders" element={<Orders user={user} />} />
          <Route path="/login" element={<Login onLogin={login} />} />
          <Route path="/admin" element={<Admin user={user} />} />
        </Routes>
      </main>
    </>
  )
}
