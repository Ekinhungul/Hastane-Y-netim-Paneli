import React from 'react'

export default function Landing({ onStart }) {
  const start = () => {
    // pass empty config; user can set city or allow location later
    onStart({ city: '', location: null })
  }
  return (
    <div className="landing">
      <div className="landing-card">
        <h1>AI Travel Buddy</h1>
        <p>Keşfetmeye hazır mısın? Konumunu paylaş veya bir şehir gir — AI sana ilk önerileri başlatacak.</p>
        <div className="landing-actions">
          <button className="primary" onClick={start}>Hemen Başla</button>
          <button className="ghost" onClick={() => onStart({ city: 'İstanbul', location: null })}>Örnek İstanbul Turu</button>
        </div>
        <div className="landing-footer">© 2025 AI Travel Buddy</div>
      </div>
    </div>
  )
}
