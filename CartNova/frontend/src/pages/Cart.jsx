import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api.js'

export default function Cart({ cart, setCart, user }) {
  const nav = useNavigate()
  const [msg, setMsg] = useState('')
  const total = cart.reduce((s, i) => s + i.price * i.quantity, 0)

  const checkout = async () => {
    if (!user) return nav('/login')
    try {
      await api('/orders', { method: 'POST', body: { items: cart.map((i) => ({ productId: i.id, quantity: i.quantity })) } })
      setCart([]); nav('/orders')
    } catch (e) { setMsg(e.message) }
  }

  if (!cart.length) return <p className="box">Your cart is empty.</p>
  return (
    <div className="box">
      <h2>Your Cart</h2>
      {cart.map((i) => (
        <div className="row" key={i.id}>
          <span>{i.name} × {i.quantity}</span>
          <b>₹{i.price * i.quantity}</b>
          <button onClick={() => setCart(cart.filter((c) => c.id !== i.id))}>✕</button>
        </div>
      ))}
      <h3>Total: ₹{total}</h3>
      {msg && <p className="err">{msg}</p>}
      <button onClick={checkout}>Checkout</button>
    </div>
  )
}
