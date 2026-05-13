import React from 'react'
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// fix icon paths for leaflet's default icon
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
})

export default function MapView({ center, places }) {
  const defaultCenter = center ? [center.lat, center.lng] : [39.925533, 32.866287] // Ankara as fallback
  return (
    <div className="mapwrap">
      <MapContainer center={defaultCenter} zoom={13} style={{ height: '60vh', width: '100%' }} key={JSON.stringify(defaultCenter)}>
        <TileLayer
          attribution='&copy; OpenStreetMap contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {center && <Circle center={[center.lat, center.lng]} radius={200} />}
        {places?.map(p => {
          const pos = p.lat && p.lng ? [p.lat, p.lng] : null
          return pos ? (
            <Marker key={p.place_id} position={pos}>
              <Popup>
                <strong>{p.name}</strong><br/>{p.address || ''}<br/>{p.rating ? `⭐ ${p.rating}` : ''}
              </Popup>
            </Marker>
          ) : null
        })}
      </MapContainer>
    </div>
  )
}
