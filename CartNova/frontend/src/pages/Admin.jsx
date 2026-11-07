import { useEffect, useState } from 'react'
import { api } from '../api.js'

const STATUSES = ['PLACED', 'PACKED', 'SHIPPED', 'DELIVERED', 'CANCELLED']

export default function Admin({ user }) {
  const [products, setProducts] = useState([])
  const [orders, setOrders] = useState([])
  const [np, setNp] = useState({ name: '', price: '', category: '', stock: '', imageUrl: '', description: '' })

  const load = () => {
    api('/products').then(setProducts)
    api('/admin/orders').then(setOrders)
  }
  useEffect(() => { if (user?.role === 'ADMIN') load() }, [user])

  if (user?.role !== 'ADMIN') return <p className="box">Admin access only.</p>

  const addProduct = async () => {
    await api('/admin/products', { method: 'POST', body: { ...np, price: +np.price, stock: +np.stock } })
    setNp({ name: '', price: '', category: '', stock: '', imageUrl: '', description: '' }); load()
  }
  const setStock = async (id, stock) => { await api(`/admin/products/${id}/stock?stock=${stock}`, { method: 'PATCH' }); load() }
  const del = async (id) => { await api(`/admin/products/${id}`, { method: 'DELETE' }); load() }
  const setStatus = async (id, s) => { await api(`/admin/orders/${id}/status?status=${s}`, { method: 'PATCH' }); load() }

  return (
    <div className="box wide">
      <h2>Admin Panel</h2>
      <h3>Add product</h3>
      <div className="bar">
        {Object.keys(np).map((k) => (
          <input key={k} placeholder={k} value={np[k]} onChange={(e) => setNp({ ...np, [k]: e.target.value })} />
        ))}
        <button onClick={addProduct}>Add</button>
      </div>
      <h3>Products & stock</h3>
      {products.map((p) => (
        <div className="row" key={p.id}>
          <span>{p.name} (₹{p.price})</span>
          <input type="number" defaultValue={p.stock} onBlur={(e) => setStock(p.id, e.target.value)} style={{ width: 80 }} />
          <button onClick={() => del(p.id)}>Delete</button>
        </div>
      ))}
      <h3>Orders</h3>
      {orders.map((o) => (
        <div className="row" key={o.id}>
          <span>#{o.id} · {o.userEmail} · ₹{o.total}</span>
          <select value={o.status} onChange={(e) => setStatus(o.id, e.target.value)}>
            {STATUSES.map((s) => <option key={s}>{s}</option>)}
          </select>
        </div>
      ))}
    </div>
  )
}
