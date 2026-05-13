import React, { useState, useRef, useEffect } from 'react'

export default function ChatUI({ messages, onSend }) {
  const [text, setText] = useState('')
  const boxRef = useRef()

  useEffect(() => {
    if (boxRef.current) boxRef.current.scrollTop = boxRef.current.scrollHeight
  }, [messages])

  const submit = (e) => {
    e.preventDefault()
    if (!text.trim()) return
    onSend(text.trim())
    setText('')
  }

  return (
    <div className="chatui">
      <div className="msgs" ref={boxRef}>
        {messages.map((m, i) => (
          <div key={i} className={m.role === 'user' ? 'msg user' : 'msg bot'}>
            {m.content}
          </div>
        ))}
      </div>
      <form onSubmit={submit} className="inputbar">
        <input value={text} onChange={e => setText(e.target.value)} placeholder="Ne yapmak istersin? (örn: Yemek yemek)" />
        <button type="submit">Gönder</button>
      </form>
    </div>
  )
}
