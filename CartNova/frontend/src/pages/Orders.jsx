import { useEffect, useState } from 'react'
import { api } from '../api.js'

export default function Orders({ user }) {
  const [orders, setOrders] = useState([])
  useEffect(() => { if (user) api('/orders/my').then(setOrders).catch(() => {}) }, [user])

  if (!user) return <p className="box">Please login to see your orders.</p>
  return (
    <div className="box">
      <h2>My Orders</h2>
      {orders.map((o) => (
        <div className="order" key={o.id}>
          <b>Order #{o.id}</b> <span className="tag">{o.status}</span> <b>₹{o.total}</b>
          <ul>{o.items.map((i, n) => <li key={n}>{i.productName} × {i.quantity}</li>)}</ul>
        </div>
      ))}
      {!orders.length && <p>No orders yet.</p>}
    </div>
  )
}
