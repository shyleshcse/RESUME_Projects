import { useEffect, useState } from 'react'
import { api } from '../api.js'

export default function Products({ addToCart }) {
  const [items, setItems] = useState([])
  const [cats, setCats] = useState([])
  const [q, setQ] = useState('')
  const [cat, setCat] = useState('')

  useEffect(() => { api('/products/categories').then(setCats).catch(() => {}) }, [])
  useEffect(() => {
    api(`/products?q=${encodeURIComponent(q)}&category=${encodeURIComponent(cat)}`).then(setItems).catch(() => {})
  }, [q, cat])

  return (
    <>
      <div className="bar">
        <input placeholder="Search products..." value={q} onChange={(e) => setQ(e.target.value)} />
        <select value={cat} onChange={(e) => setCat(e.target.value)}>
          <option value="">All categories</option>
          {cats.map((c) => <option key={c}>{c}</option>)}
        </select>
      </div>
      <div className="grid">
        {items.map((p) => (
          <div className="card" key={p.id}>
            <img src={p.imageUrl} alt={p.name} />
            <h3>{p.name}</h3>
            <p>{p.description}</p>
            <b>₹{p.price}</b> <small>{p.stock > 0 ? `${p.stock} in stock` : 'Out of stock'}</small>
            <button disabled={p.stock < 1} onClick={() => addToCart(p)}>Add to cart</button>
          </div>
        ))}
      </div>
    </>
  )
}
