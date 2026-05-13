const API_BASE = 'http://localhost:8080/api'
const LS_KEY = 'ekinhosp_local_db_v2'

class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

function createEmptyDb() {
  return {
    patients: [],
    doctors: [],
    branches: ['Kardiyoloji', 'Dahiliye', 'Nöroloji', 'Ortopedi'],
    reports: [],
    appointments: []
  }
}

function readDb() {
  try {
    const raw = localStorage.getItem(LS_KEY)
    if (!raw) return createEmptyDb()
    const parsed = JSON.parse(raw)
    return {
      ...createEmptyDb(),
      ...parsed
    }
  } catch (e) {
    return createEmptyDb()
  }
}

function writeDb(db) {
  localStorage.setItem(LS_KEY, JSON.stringify(db))
}

async function tryApi(path, options = {}) {
  let response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      ...options
    })
  } catch {
    throw new Error('NETWORK_ERROR')
  }

  if (!response.ok) {
    const text = await response.text()
    throw new ApiError(text || `HTTP ${response.status}`, response.status)
  }

  if (response.status === 204) return null
  return response.json()
}

export async function getPatients() {
  try {
    return await tryApi('/patients')
  } catch {
    return readDb().patients
  }
}

export async function addPatient(payload) {
  const tcNo = String(payload.tcNo || '').trim()
  if (!/^\d{11}$/.test(tcNo)) {
    throw new Error('TC No 11 haneli rakam olmalıdır.')
  }

  try {
    return await tryApi('/patients', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  } catch (error) {
    if (error instanceof ApiError) {
      throw new Error(error.message || 'Hasta kaydı backend tarafından reddedildi.')
    }
    const db = readDb()
    const normalizedTc = tcNo
    const duplicate = db.patients.find((p) => p.tcNo === normalizedTc)
    if (duplicate) throw new Error('Bu TC No ile hasta zaten kayıtlı.')

    const newPatient = {
      id: Date.now(),
      tcNo: normalizedTc,
      firstName: String(payload.firstName || '').trim(),
      lastName: String(payload.lastName || '').trim(),
      phone: String(payload.phone || '').trim()
    }

    db.patients.unshift(newPatient)
    writeDb(db)
    return newPatient
  }
}

export async function getDoctors() {
  try {
    return await tryApi('/doctors')
  } catch {
    return readDb().doctors
  }
}

export async function addDoctor(payload) {
  try {
    return await tryApi('/doctors', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  } catch (error) {
    if (error instanceof ApiError) {
      throw new Error(error.message || 'Doktor kaydı backend tarafından reddedildi.')
    }
    const db = readDb()
    const name = String(payload.name || '').trim()
    const branchName = String(payload.branchName || '').trim()
    if (!name) throw new Error('Doktor adı boş olamaz.')
    if (!branchName) throw new Error('Branş seçiniz.')

    const duplicate = db.doctors.find(
      (d) => d.name.toLowerCase() === name.toLowerCase() && d.branchName.toLowerCase() === branchName.toLowerCase()
    )
    if (duplicate) throw new Error('Aynı isim ve branşta doktor zaten kayıtlı.')

    const tcNo = String(payload.tcNo || '').trim()
    const newDoctor = { id: Date.now(), name, branchName, tcNo }
    db.doctors.unshift(newDoctor)
    if (!db.branches.includes(branchName)) db.branches.push(branchName)
    writeDb(db)
    return newDoctor
  }
}

export async function getBranches() {
  try {
    return await tryApi('/branches')
  } catch {
    return readDb().branches.map((name, idx) => ({ id: idx + 1, name }))
  }
}

export async function addBranch(name) {
  const payload = { name: String(name || '').trim() }
  try {
    return await tryApi('/branches', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  } catch (error) {
    if (error instanceof ApiError) {
      throw new Error(error.message || 'Branş kaydı backend tarafından reddedildi.')
    }
    if (!payload.name) throw new Error('Branş adı boş olamaz.')
    const db = readDb()
    if (db.branches.some((b) => b.toLowerCase() === payload.name.toLowerCase())) {
      throw new Error('Bu branş zaten kayıtlı.')
    }
    db.branches.push(payload.name)
    writeDb(db)
    return { id: db.branches.length, name: payload.name }
  }
}

export async function getReportsByPatient(patientId) {
  try {
    return await tryApi(`/patients/${patientId}/reports`)
  } catch {
    const db = readDb()
    return db.reports.filter((r) => Number(r.patientId) === Number(patientId))
  }
}

export async function addReport(payload) {
  const db = readDb()
  const newReport = {
    id: Date.now(),
    patientId: Number(payload.patientId),
    doctorId: payload.doctorId || null,
    title: String(payload.title || '').trim(),
    fileName: String(payload.fileName || '').trim(),
    fileType: String(payload.fileType || '').trim(),
    fileData: payload.fileData || null,
    createdAt: new Date().toISOString()
  }

  if (!newReport.patientId || !newReport.title) {
    throw new Error('Hasta ve rapor başlığı zorunludur.')
  }
  if (!newReport.fileData) {
    throw new Error('Lütfen bir dosya seçiniz (PDF veya resim).')
  }

  db.reports.unshift(newReport)
  writeDb(db)
  return newReport
}

export async function getAppointments() {
  try {
    return await tryApi('/appointments')
  } catch {
    return readDb().appointments
  }
}

export async function addAppointment(payload) {
  try {
    return await tryApi('/appointments', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  } catch (error) {
    if (error instanceof ApiError) {
      throw new Error(error.message || 'Randevu kaydı backend tarafından reddedildi.')
    }
    const db = readDb()
    const patientId = Number(payload.patientId)
    const doctorId = Number(payload.doctorId)
    const appointmentTime = String(payload.appointmentTime || '').trim()

    if (!patientId || !doctorId || !appointmentTime) {
      throw new Error('Hasta, doktor ve tarih alanı zorunludur.')
    }

    const duplicate = db.appointments.find(
      (a) => Number(a.doctorId) === doctorId && String(a.appointmentTime) === appointmentTime && a.status !== 'IPTAL'
    )
    if (duplicate) {
      throw new Error('Bu doktor için aynı saatte başka randevu var.')
    }

    const row = {
      id: Date.now(),
      patientId,
      doctorId,
      appointmentTime,
      status: 'AKTIF'
    }
    db.appointments.unshift(row)
    writeDb(db)
    return row
  }
}

export async function cancelAppointment(id) {
  try {
    return await tryApi(`/appointments/${id}/cancel`, { method: 'PUT' })
  } catch (error) {
    if (error instanceof ApiError) throw new Error(error.message)
    const db = readDb()
    const apt = db.appointments.find((a) => Number(a.id) === Number(id))
    if (!apt) throw new Error('Randevu bulunamadi.')
    apt.status = 'IPTAL'
    writeDb(db)
    return apt
  }
}

export async function completeAppointment(id, diagnosis, notes) {
  try {
    return await tryApi(`/appointments/${id}/complete`, {
      method: 'PUT',
      body: JSON.stringify({ diagnosis, notes })
    })
  } catch (error) {
    if (error instanceof ApiError) throw new Error(error.message)
    const db = readDb()
    const apt = db.appointments.find((a) => Number(a.id) === Number(id))
    if (!apt) throw new Error('Randevu bulunamadi.')
    apt.status = 'TAMAMLANDI'
    apt.diagnosis = diagnosis
    apt.notes = notes || ''
    writeDb(db)
    return apt
  }
}
