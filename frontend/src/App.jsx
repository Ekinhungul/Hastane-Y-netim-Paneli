import React, { useEffect, useMemo, useState } from 'react'
import {
  addAppointment, addBranch, addDoctor, addPatient, addReport,
  cancelAppointment, completeAppointment,
  getAppointments, getBranches, getDoctors, getPatients, getReportsByPatient
} from './api.js'

export default function App() {
  const [screen, setScreen] = useState('landing')
  const [role, setRole] = useState(null)
  const [currentUser, setCurrentUser] = useState(null)
  const [tcInput, setTcInput] = useState('')
  const [loginError, setLoginError] = useState('')

  const [patients, setPatients] = useState([])
  const [doctors, setDoctors] = useState([])
  const [branches, setBranches] = useState([])
  const [appointments, setAppointments] = useState([])
  const [notice, setNotice] = useState({ type: '', text: '' })

  useEffect(() => { loadAll() }, [])

  async function loadAll() {
    const [p, d, b, a] = await Promise.all([getPatients(), getDoctors(), getBranches(), getAppointments()])
    setPatients(Array.isArray(p) ? p : [])
    setDoctors(Array.isArray(d) ? d : [])
    setBranches(Array.isArray(b) ? b : [])
    setAppointments(Array.isArray(a) ? a : [])
  }

  function flash(type, text) {
    setNotice({ type, text })
    setTimeout(() => setNotice({ type: '', text: '' }), 3500)
  }

  function logout() {
    setScreen('landing')
    setRole(null)
    setCurrentUser(null)
    setTcInput('')
    setLoginError('')
  }

  function goToLogin(r) {
    setRole(r)
    setTcInput('')
    setLoginError('')
    setScreen('login')
  }

  function handleLogin(e) {
    e.preventDefault()
    const tc = tcInput.trim()
    if (!/^\d{11}$/.test(tc)) { setLoginError('TC No 11 haneli rakam olmalidir.'); return }

    if (role === 'doctor') {
      const doc = doctors.find((d) => d.tcNo === tc)
      if (!doc) { setLoginError('Bu TC ile kayitli doktor bulunamadi. Kayitli doktor TC\'si giriniz.'); return }
      setCurrentUser(doc)
      setScreen('doctor-panel')
    } else {
      const pat = patients.find((p) => p.tcNo === tc)
      if (!pat) { setLoginError('Bu TC ile kayitli hasta bulunamadi. Kayitli hasta TC\'si giriniz.'); return }
      setCurrentUser(pat)
      setScreen('patient-panel')
    }
    setLoginError('')
  }

  if (screen === 'landing') return <LandingPage onSelect={goToLogin} onRegister={(r) => { setRole(r); setScreen('register') }} />
  if (screen === 'register') return <RegisterPage role={role} branches={branches} flash={flash} notice={notice} loadAll={loadAll} onBack={() => setScreen('landing')} onDone={() => setScreen('landing')} />
  if (screen === 'login') return <LoginPage role={role} tc={tcInput} setTc={setTcInput} error={loginError} onSubmit={handleLogin} onBack={() => setScreen('landing')} />
  if (screen === 'doctor-panel') return <DoctorPanel user={currentUser} patients={patients} doctors={doctors} branches={branches} appointments={appointments} notice={notice} flash={flash} loadAll={loadAll} onLogout={logout} />
  if (screen === 'patient-panel') return <PatientPanel user={currentUser} doctors={doctors} branches={branches} appointments={appointments} notice={notice} flash={flash} loadAll={loadAll} onLogout={logout} />
  return null
}

function LandingPage({ onSelect, onRegister }) {
  return (
    <div className="landing">
      <div className="landing-bg" />
      <div className="landing-content">
        <div className="landing-header">
          <span className="logo-icon">+</span>
          <span className="logo-text">EkinMedical</span>
        </div>
        <h1 className="landing-title">YONETIM PANELI</h1>
        <p className="landing-sub">Hastane yonetim sisteminize hos geldiniz. Devam etmek icin giris tipinizi seciniz.</p>

        <div className="role-cards">
          <button className="role-card doctor-role" onClick={() => onSelect('doctor')}>
            <div className="role-icon">&#9764;</div>
            <h2>Doktor Girisi</h2>
            <p>Hastalarinizi yonetin, rapor yukleyin, randevulari kontrol edin.</p>
            <span className="role-arrow">&rarr;</span>
          </button>
          <button className="role-card patient-role" onClick={() => onSelect('patient')}>
            <div className="role-icon">&#9829;</div>
            <h2>Hasta Girisi</h2>
            <p>Randevu alin, raporlarinizi goruntuleyin, saglik durumunuzu takip edin.</p>
            <span className="role-arrow">&rarr;</span>
          </button>
        </div>

        <div className="register-section">
          <p className="register-label">Hesabiniz yok mu? Yeni kayit olusturun:</p>
          <div className="register-buttons">
            <button className="btn-register doctor-reg" onClick={() => onRegister('doctor')}>Yeni Doktor Kaydi</button>
            <button className="btn-register patient-reg" onClick={() => onRegister('patient')}>Yeni Hasta Kaydi</button>
          </div>
        </div>

        <p className="landing-footer">EkinMedical &copy; 2026 &mdash; Guvenli Saglik Yonetimi</p>
      </div>
    </div>
  )
}

function LoginPage({ role, tc, setTc, error, onSubmit, onBack }) {
  const isDoctor = role === 'doctor'
  return (
    <div className={`login-page ${isDoctor ? 'login-doctor' : 'login-patient'}`}>
      <div className="login-card">
        <button className="back-link" onClick={onBack}>&larr; Geri Don</button>
        <div className="login-icon">{isDoctor ? '\u2694' : '\u2665'}</div>
        <h1>{isDoctor ? 'Doktor Girisi' : 'Hasta Girisi'}</h1>
        <p className="login-desc">{isDoctor ? 'TC Kimlik Numaraniz ile doktor panelinize erisin.' : 'TC Kimlik Numaraniz ile hasta panelinize erisin.'}</p>
        <form onSubmit={onSubmit} className="login-form">
          <input value={tc} maxLength={11} onChange={(e) => setTc(e.target.value.replace(/\D/g, ''))} placeholder="TC Kimlik No (11 hane)" autoFocus />
          {error && <div className="login-error">{error}</div>}
          <button type="submit">{isDoctor ? 'Doktor Paneline Giris' : 'Hasta Paneline Giris'}</button>
        </form>
      </div>
    </div>
  )
}

function RegisterPage({ role, branches, flash, notice, loadAll, onBack, onDone }) {
  const isDoctor = role === 'doctor'
  const [form, setForm] = useState(
    isDoctor
      ? { tcNo: '', name: '', branchName: '' }
      : { tcNo: '', firstName: '', lastName: '', phone: '' }
  )
  const [success, setSuccess] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!/^\d{11}$/.test(form.tcNo)) { flash('error', 'TC No 11 haneli rakam olmalidir.'); return }

    if (isDoctor) {
      if (!form.name.trim()) { flash('error', 'Doktor adi zorunludur.'); return }
      if (!form.branchName) { flash('error', 'Brans seciniz.'); return }
      try {
        await addDoctor(form)
        await loadAll()
        setSuccess(true)
      } catch (error) { flash('error', error.message) }
    } else {
      if (!form.firstName.trim() || !form.lastName.trim()) { flash('error', 'Ad ve soyad zorunludur.'); return }
      try {
        await addPatient(form)
        await loadAll()
        setSuccess(true)
      } catch (error) { flash('error', error.message) }
    }
  }

  return (
    <div className={`login-page ${isDoctor ? 'login-doctor' : 'login-patient'}`}>
      <div className="register-card">
        <button className="back-link" onClick={onBack}>&larr; Ana Sayfaya Don</button>
        {notice.text && <div className={notice.type === 'success' ? 'notice success' : 'notice error'}>{notice.text}</div>}

        {success ? (
          <div className="register-success">
            <div className="success-icon">&#10003;</div>
            <h2>Kayit Basarili!</h2>
            <p>{isDoctor ? 'Doktor kaydınız oluşturuldu.' : 'Hasta kaydınız oluşturuldu.'} Artik TC numaraniz ile giris yapabilirsiniz.</p>
            <button className="btn-register-go" onClick={onDone}>Giris Sayfasina Don</button>
          </div>
        ) : (
          <>
            <div className="login-icon">{isDoctor ? '\u2694' : '\u2665'}</div>
            <h1>{isDoctor ? 'Yeni Doktor Kaydi' : 'Yeni Hasta Kaydi'}</h1>
            <p className="login-desc">{isDoctor ? 'Sisteme doktor olarak kayit olun.' : 'Sisteme hasta olarak kayit olun.'}</p>

            <form className="login-form" onSubmit={handleSubmit}>
              <input value={form.tcNo} maxLength={11} onChange={(e) => setForm((s) => ({ ...s, tcNo: e.target.value.replace(/\D/g, '') }))} placeholder="TC Kimlik No (11 hane)" autoFocus />

              {isDoctor ? (
                <>
                  <input value={form.name} onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))} placeholder="Ad Soyad" />
                  <select value={form.branchName} onChange={(e) => setForm((s) => ({ ...s, branchName: e.target.value }))} className="register-select">
                    <option value="">Brans seciniz</option>
                    {branches.map((b) => <option key={b.id || b.name} value={b.name}>{b.name}</option>)}
                  </select>
                </>
              ) : (
                <>
                  <input value={form.firstName} onChange={(e) => setForm((s) => ({ ...s, firstName: e.target.value }))} placeholder="Ad" />
                  <input value={form.lastName} onChange={(e) => setForm((s) => ({ ...s, lastName: e.target.value }))} placeholder="Soyad" />
                  <input value={form.phone} onChange={(e) => setForm((s) => ({ ...s, phone: e.target.value.replace(/[^0-9+ ()-]/g, '') }))} placeholder="Telefon (opsiyonel)" />
                </>
              )}

              <button type="submit">Kayit Ol</button>
            </form>
          </>
        )}
      </div>
    </div>
  )
}

function DoctorPanel({ user, patients, doctors, branches, appointments, notice, flash, loadAll, onLogout }) {
  const [tab, setTab] = useState('dashboard')
  const [patientSearch, setPatientSearch] = useState('')
  const [selectedPatient, setSelectedPatient] = useState(null)
  const [patientReports, setPatientReports] = useState([])
  const [reportForm, setReportForm] = useState({ patientId: '', title: '' })
  const [reportFile, setReportFile] = useState(null)
  const [completeForm, setCompleteForm] = useState({ id: null, diagnosis: '', notes: '' })

  const [branchName, setBranchName] = useState('')
  const [doctorForm, setDoctorForm] = useState({ name: '', branchName: '', tcNo: '' })
  const [patientForm, setPatientForm] = useState({ tcNo: '', firstName: '', lastName: '', phone: '' })

  const myAppointments = useMemo(() => appointments.filter((a) => Number(a.doctorId) === Number(user.id)), [appointments, user])
  const todayAppts = useMemo(() => {
    const today = new Date().toISOString().slice(0, 10)
    return myAppointments.filter((a) => String(a.appointmentTime || '').startsWith(today))
  }, [myAppointments])

  const filteredPatients = useMemo(() => {
    if (!patientSearch.trim()) return patients
    const q = patientSearch.toLowerCase()
    return patients.filter((p) => (p.tcNo || '').includes(q) || `${p.firstName} ${p.lastName}`.toLowerCase().includes(q))
  }, [patients, patientSearch])

  function findName(list, id, field = 'name') {
    const item = list.find((x) => Number(x.id) === Number(id))
    if (!item) return `#${id}`
    return field === 'fullName' ? `${item.firstName} ${item.lastName}` : (item[field] || `#${id}`)
  }

  async function viewPatient(p) {
    setSelectedPatient(p)
    const r = await getReportsByPatient(p.id)
    setPatientReports(Array.isArray(r) ? r : [])
    setTab('patient-detail')
  }

  function handleFileSelect(e) {
    const file = e.target.files[0]
    if (!file) { setReportFile(null); return }
    const allowed = ['application/pdf', 'image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowed.includes(file.type)) { flash('error', 'Sadece PDF ve resim dosyalari yuklenebilir.'); e.target.value = ''; return }
    if (file.size > 5 * 1024 * 1024) { flash('error', 'Dosya boyutu en fazla 5 MB olabilir.'); e.target.value = ''; return }
    setReportFile(file)
  }

  async function handleReportCreate(e) {
    e.preventDefault()
    if (!reportForm.patientId || !reportForm.title.trim()) { flash('error', 'Hasta ve rapor basligi zorunludur.'); return }
    if (!reportFile) { flash('error', 'Lutfen bir dosya seciniz (PDF veya resim).'); return }
    try {
      const fileData = await fileToBase64(reportFile)
      await addReport({ ...reportForm, doctorId: user.id, fileName: reportFile.name, fileType: reportFile.type, fileData })
      setReportForm({ patientId: '', title: '' })
      setReportFile(null)
      flash('success', 'Rapor yuklendi.')
      if (selectedPatient) { setPatientReports(await getReportsByPatient(selectedPatient.id)) }
    } catch (error) { flash('error', error.message) }
  }

  async function handleCancelAppt(id) {
    try { await cancelAppointment(id); await loadAll(); flash('success', 'Randevu iptal edildi.') }
    catch (error) { flash('error', error.message) }
  }

  async function handleCompleteAppt(e) {
    e.preventDefault()
    if (!completeForm.diagnosis.trim()) { flash('error', 'Teshis zorunludur.'); return }
    try {
      await completeAppointment(completeForm.id, completeForm.diagnosis, completeForm.notes)
      setCompleteForm({ id: null, diagnosis: '', notes: '' })
      await loadAll()
      flash('success', 'Muayene tamamlandi.')
    } catch (error) { flash('error', error.message) }
  }

  async function handlePatientCreate(e) {
    e.preventDefault()
    if (!/^\d{11}$/.test(patientForm.tcNo)) { flash('error', 'TC No 11 haneli olmalidir.'); return }
    if (!patientForm.firstName.trim() || !patientForm.lastName.trim()) { flash('error', 'Ad ve soyad zorunludur.'); return }
    try { await addPatient(patientForm); setPatientForm({ tcNo: '', firstName: '', lastName: '', phone: '' }); await loadAll(); flash('success', 'Hasta kaydedildi.') }
    catch (error) { flash('error', error.message) }
  }

  async function handleBranchCreate(e) {
    e.preventDefault()
    if (!branchName.trim()) { flash('error', 'Brans adi bos olamaz.'); return }
    try { await addBranch(branchName); setBranchName(''); await loadAll(); flash('success', 'Brans eklendi.') }
    catch (error) { flash('error', error.message) }
  }

  async function handleDoctorCreate(e) {
    e.preventDefault()
    if (!doctorForm.name.trim()) { flash('error', 'Doktor adi zorunludur.'); return }
    if (!doctorForm.branchName) { flash('error', 'Brans seciniz.'); return }
    if (!doctorForm.tcNo || !/^\d{11}$/.test(doctorForm.tcNo)) { flash('error', 'Doktor TC No 11 haneli olmalidir.'); return }
    try { await addDoctor(doctorForm); setDoctorForm({ name: '', branchName: '', tcNo: '' }); await loadAll(); flash('success', 'Doktor kaydedildi.') }
    catch (error) { flash('error', error.message) }
  }

  const docTabs = [
    { id: 'dashboard', label: 'Dashboard' },
    { id: 'patients', label: 'Hastalar' },
    { id: 'appointments', label: 'Randevularim' },
    { id: 'reports', label: 'Rapor Yukle' },
    { id: 'management', label: 'Yonetim' },
  ]

  return (
    <div className="panel-layout">
      <aside className="panel-sidebar doctor-sidebar">
        <div>
          <p className="brand-kicker">EkinMedical</p>
          <h2 className="side-title">Doktor Paneli</h2>
          <div className="user-badge">
            <div className="user-avatar">{(user.name || '?')[0].toUpperCase()}</div>
            <div><p className="user-name">{user.name}</p><p className="user-role">{user.branchName || user.branch || 'Doktor'}</p></div>
          </div>
        </div>
        <nav className="side-nav">
          {docTabs.map((t) => (
            <button key={t.id} className={tab === t.id || (tab === 'patient-detail' && t.id === 'patients') ? 'tab active' : 'tab'} onClick={() => { setTab(t.id); setSelectedPatient(null) }}>{t.label}</button>
          ))}
        </nav>
        <button className="btn-ghost" onClick={onLogout}>Cikis Yap</button>
      </aside>

      <main className="panel-content fade-in" key={tab}>
        {notice.text && <div className={notice.type === 'success' ? 'notice success' : 'notice error'}>{notice.text}</div>}

        {tab === 'dashboard' && (
          <section>
            <h1 className="page-title">Hos Geldiniz, {user.name}</h1>
            <div className="card-grid four">
              <StatCard title="Toplam Hastam" value={patients.length} color="#2563eb" />
              <StatCard title="Bugunun Randevulari" value={todayAppts.length} color="#dc2626" />
              <StatCard title="Toplam Randevum" value={myAppointments.length} color="#059669" />
              <StatCard title="Brans Sayisi" value={branches.length} color="#7c3aed" />
            </div>
            <div className="dash-grid">
              <div className="card">
                <h3>Bugunun Randevulari</h3>
                {todayAppts.length === 0 ? <p className="muted">Bugun randevunuz yok.</p> : (
                  <ul className="mini-list">{todayAppts.map((a) => (
                    <li key={a.id}>
                      <span className="mini-name">{findName(patients, a.patientId, 'fullName')}</span>
                      <span className="mini-meta">{String(a.appointmentTime).slice(11, 16)} <span className={'status-badge ' + (a.status || 'AKTIF')}>{a.status || 'AKTIF'}</span></span>
                    </li>
                  ))}</ul>
                )}
              </div>
              <div className="card">
                <h3>Son Hastalar</h3>
                {patients.slice(0, 5).length === 0 ? <p className="muted">Hasta yok.</p> : (
                  <ul className="mini-list">{patients.slice(0, 5).map((p) => (
                    <li key={p.id}><span className="mini-name">{p.firstName} {p.lastName}</span><span className="mini-meta">TC: {p.tcNo}</span></li>
                  ))}</ul>
                )}
              </div>
            </div>
            <div className="quick-actions">
              <button className="btn-action" onClick={() => setTab('patients')}>Hastalari Gor</button>
              <button className="btn-action" onClick={() => setTab('reports')}>Rapor Yukle</button>
              <button className="btn-action" onClick={() => setTab('appointments')}>Randevularim</button>
            </div>
          </section>
        )}

        {tab === 'patients' && (
          <section>
            <h1 className="page-title">Hasta Listesi</h1>
            <div className="layout-two">
              <div className="card">
                <h2>Yeni Hasta Kaydi</h2>
                <form className="form" onSubmit={handlePatientCreate}>
                  <input value={patientForm.tcNo} maxLength={11} onChange={(e) => setPatientForm((s) => ({ ...s, tcNo: e.target.value.replace(/\D/g, '') }))} placeholder="TC No (11 hane)" />
                  <input value={patientForm.firstName} onChange={(e) => setPatientForm((s) => ({ ...s, firstName: e.target.value }))} placeholder="Ad" />
                  <input value={patientForm.lastName} onChange={(e) => setPatientForm((s) => ({ ...s, lastName: e.target.value }))} placeholder="Soyad" />
                  <input value={patientForm.phone} onChange={(e) => setPatientForm((s) => ({ ...s, phone: e.target.value.replace(/[^0-9+ ()-]/g, '') }))} placeholder="Telefon" />
                  <button type="submit">Hasta Kaydet</button>
                </form>
              </div>
              <div className="card">
                <h2>Kayitli Hastalar</h2>
                <input value={patientSearch} onChange={(e) => setPatientSearch(e.target.value)} placeholder="TC veya isim ile ara..." className="search-input" />
                <div className="table-wrap">
                  <table>
                    <thead><tr><th>ID</th><th>TC No</th><th>Ad Soyad</th><th>Telefon</th><th>Islem</th></tr></thead>
                    <tbody>
                      {filteredPatients.length === 0 ? <tr><td colSpan={5} className="empty-cell">Hasta bulunamadi.</td></tr> :
                        filteredPatients.map((p) => (
                          <tr key={p.id}>
                            <td>{p.id}</td><td>{p.tcNo}</td><td>{p.firstName} {p.lastName}</td><td>{p.phone || '-'}</td>
                            <td><button className="btn-sm" onClick={() => viewPatient(p)}>Detay</button></td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </section>
        )}

        {tab === 'patient-detail' && selectedPatient && (
          <section>
            <button className="btn-back" onClick={() => setTab('patients')}>&larr; Hasta Listesi</button>
            <h1 className="page-title">{selectedPatient.firstName} {selectedPatient.lastName}</h1>
            <div className="detail-header">
              <div className="detail-field"><span>TC No</span><strong>{selectedPatient.tcNo}</strong></div>
              <div className="detail-field"><span>Telefon</span><strong>{selectedPatient.phone || '-'}</strong></div>
              <div className="detail-field"><span>Hasta ID</span><strong>{selectedPatient.id}</strong></div>
            </div>
            <div className="dash-grid">
              <div className="card">
                <h3>Randevulari</h3>
                {(() => {
                  const pa = appointments.filter((a) => Number(a.patientId) === Number(selectedPatient.id))
                  if (!pa.length) return <p className="muted">Randevu yok.</p>
                  return <ul className="mini-list">{pa.map((a) => (
                    <li key={a.id}><span className="mini-name">{findName(doctors, a.doctorId)}</span><span className="mini-meta">{a.appointmentTime} <span className={'status-badge ' + (a.status || 'AKTIF')}>{a.status || 'AKTIF'}</span></span></li>
                  ))}</ul>
                })()}
              </div>
              <div className="card">
                <h3>Raporlari</h3>
                {patientReports.length === 0 ? <p className="muted">Rapor yok.</p> : (
                  <div className="report-grid compact">{patientReports.map((r) => (
                    <div key={r.id} className="report-card">
                      <div className="report-preview small">
                        {r.fileData && r.fileType && r.fileType.startsWith('image/') ? <img src={r.fileData} alt={r.title} /> : <div className="report-pdf-icon">PDF</div>}
                      </div>
                      <div className="report-info">
                        <p className="report-title">{r.title || '-'}</p>
                        <p className="report-meta">{r.fileName || '-'}</p>
                      </div>
                      {r.fileData && <button className="btn-sm" onClick={() => { const w = window.open(''); w.document.write(r.fileType && r.fileType.startsWith('image/') ? `<img src="${r.fileData}" style="max-width:100%"/>` : `<iframe src="${r.fileData}" style="width:100%;height:100vh;border:none"></iframe>`); }}>Ac</button>}
                    </div>
                  ))}</div>
                )}
              </div>
            </div>
            <div className="card" style={{ marginTop: 16 }}>
              <h3>Bu Hastaya Rapor Yukle</h3>
              <form className="form" onSubmit={(e) => { reportForm.patientId = selectedPatient.id; handleReportCreate(e) }}>
                <input value={reportForm.title} onChange={(e) => setReportForm((s) => ({ ...s, title: e.target.value }))} placeholder="Rapor basligi" />
                <div className="file-upload">
                  <label className="file-label">
                    <span>{reportFile ? reportFile.name : 'PDF veya Resim Sec (max 5MB)'}</span>
                    <input type="file" accept=".pdf,.jpg,.jpeg,.png,.gif,.webp" onChange={handleFileSelect} />
                  </label>
                </div>
                <button type="submit">Rapor Yukle</button>
              </form>
            </div>
          </section>
        )}

        {tab === 'appointments' && (
          <section>
            <h1 className="page-title">Randevularim</h1>
            {completeForm.id && (
              <div className="card" style={{ marginBottom: 16 }}>
                <h3>Muayene Tamamla</h3>
                <form className="form" onSubmit={handleCompleteAppt}>
                  <input value={completeForm.diagnosis} onChange={(e) => setCompleteForm((s) => ({ ...s, diagnosis: e.target.value }))} placeholder="Teshis *" />
                  <input value={completeForm.notes} onChange={(e) => setCompleteForm((s) => ({ ...s, notes: e.target.value }))} placeholder="Notlar (opsiyonel)" />
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button type="submit">Tamamla</button>
                    <button type="button" className="btn-cancel" onClick={() => setCompleteForm({ id: null, diagnosis: '', notes: '' })}>Vazgec</button>
                  </div>
                </form>
              </div>
            )}
            <div className="card">
              <div className="table-wrap">
                <table>
                  <thead><tr><th>ID</th><th>Hasta</th><th>Tarih</th><th>Durum</th><th>Islem</th></tr></thead>
                  <tbody>
                    {myAppointments.length === 0 ? <tr><td colSpan={5} className="empty-cell">Randevunuz yok.</td></tr> :
                      myAppointments.map((a) => (
                        <tr key={a.id}>
                          <td>{a.id}</td>
                          <td>{findName(patients, a.patientId, 'fullName')}</td>
                          <td>{a.appointmentTime}</td>
                          <td><span className={'status-badge ' + (a.status || 'AKTIF')}>{a.status || 'AKTIF'}</span></td>
                          <td>
                            {(a.status === 'AKTIF' || !a.status) && (
                              <>
                                <button className="btn-sm green" onClick={() => setCompleteForm({ id: a.id, diagnosis: '', notes: '' })}>Tamamla</button>
                                <button className="btn-sm red" onClick={() => handleCancelAppt(a.id)}>Iptal</button>
                              </>
                            )}
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}

        {tab === 'reports' && (
          <section>
            <h1 className="page-title">Rapor Yukle</h1>
            <div className="layout-two">
              <div className="card">
                <h2>Yeni Rapor</h2>
                <form className="form" onSubmit={handleReportCreate}>
                  <select value={reportForm.patientId} onChange={(e) => setReportForm((s) => ({ ...s, patientId: e.target.value }))}>
                    <option value="">Hasta seciniz</option>
                    {patients.map((p) => <option key={p.id} value={p.id}>{p.firstName} {p.lastName}</option>)}
                  </select>
                  <input value={reportForm.title} onChange={(e) => setReportForm((s) => ({ ...s, title: e.target.value }))} placeholder="Rapor basligi" />
                  <div className="file-upload">
                    <label className="file-label">
                      <span>{reportFile ? reportFile.name : 'PDF veya Resim Sec (max 5MB)'}</span>
                      <input type="file" accept=".pdf,.jpg,.jpeg,.png,.gif,.webp" onChange={handleFileSelect} />
                    </label>
                  </div>
                  <button type="submit">Rapor Kaydet</button>
                </form>
              </div>
              <div className="card">
                <h2>Bilgi</h2>
                <p className="muted">Desteklenen formatlar: <strong>PDF, JPG, PNG, GIF, WEBP</strong></p>
                <p className="muted">Maksimum dosya boyutu: <strong>5 MB</strong></p>
                <p className="muted" style={{ marginTop: 8 }}>Yuklenen dosyalar tarayicida kalici olarak saklanir.</p>
              </div>
            </div>
          </section>
        )}

        {tab === 'management' && (
          <section>
            <h1 className="page-title">Sistem Yonetimi</h1>
            <div className="dash-grid">
              <div className="card">
                <h3>Brans Ekle</h3>
                <form className="form compact" onSubmit={handleBranchCreate}>
                  <input value={branchName} onChange={(e) => setBranchName(e.target.value)} placeholder="Brans adi" />
                  <button type="submit">Ekle</button>
                </form>
                {branches.length > 0 && <div className="branch-chips">{branches.map((b) => <span key={b.id || b.name} className="chip">{b.name}</span>)}</div>}
              </div>
              <div className="card">
                <h3>Doktor Ekle</h3>
                <form className="form" onSubmit={handleDoctorCreate}>
                  <input value={doctorForm.name} onChange={(e) => setDoctorForm((s) => ({ ...s, name: e.target.value }))} placeholder="Doktor adi" />
                  <input value={doctorForm.tcNo} maxLength={11} onChange={(e) => setDoctorForm((s) => ({ ...s, tcNo: e.target.value.replace(/\D/g, '') }))} placeholder="Doktor TC No (11 hane)" />
                  <select value={doctorForm.branchName} onChange={(e) => setDoctorForm((s) => ({ ...s, branchName: e.target.value }))}>
                    <option value="">Brans seciniz</option>
                    {branches.map((b) => <option key={b.id || b.name} value={b.name}>{b.name}</option>)}
                  </select>
                  <button type="submit">Doktor Kaydet</button>
                </form>
              </div>
              <div className="card">
                <h3>Doktor Listesi</h3>
                <div className="doctor-cards">
                  {doctors.length === 0 ? <p className="muted">Doktor yok.</p> : doctors.map((d) => (
                    <div key={d.id} className="doctor-card">
                      <div className="doc-avatar">{(d.name || '?')[0].toUpperCase()}</div>
                      <div><p className="doc-name">{d.name}</p><p className="doc-branch">{d.branchName || d.branch || '-'}</p></div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </section>
        )}
      </main>
    </div>
  )
}

function PatientPanel({ user, doctors, branches, appointments, notice, flash, loadAll, onLogout }) {
  const [tab, setTab] = useState('dashboard')
  const [myReports, setMyReports] = useState([])
  const [apptDoctor, setApptDoctor] = useState('')
  const [apptDate, setApptDate] = useState('')
  const [apptTime, setApptTime] = useState('')

  useEffect(() => {
    getReportsByPatient(user.id).then((r) => setMyReports(Array.isArray(r) ? r : []))
  }, [user.id])

  const myAppointments = useMemo(() => appointments.filter((a) => Number(a.patientId) === Number(user.id)), [appointments, user])
  const activeAppts = useMemo(() => myAppointments.filter((a) => a.status === 'AKTIF' || !a.status), [myAppointments])

  function findDocName(id) {
    const d = doctors.find((x) => Number(x.id) === Number(id))
    return d ? d.name : `#${id}`
  }

  async function handleNewAppointment(e) {
    e.preventDefault()
    if (!apptDoctor || !apptDate || !apptTime) { flash('error', 'Doktor, tarih ve saat secimi zorunludur.'); return }
    const appointmentTime = `${apptDate}T${apptTime}`
    const now = new Date().toISOString().slice(0, 16)
    if (appointmentTime < now) { flash('error', 'Gecmis tarihe randevu alinamaz.'); return }
    try {
      await addAppointment({ patientId: user.id, doctorId: apptDoctor, appointmentTime })
      setApptDoctor(''); setApptDate(''); setApptTime('')
      await loadAll()
      flash('success', 'Randevunuz olusturuldu.')
    } catch (error) { flash('error', error.message) }
  }

  async function handleCancelAppt(id) {
    try { await cancelAppointment(id); await loadAll(); flash('success', 'Randevu iptal edildi.') }
    catch (error) { flash('error', error.message) }
  }

  const patTabs = [
    { id: 'dashboard', label: 'Dashboard' },
    { id: 'appointments', label: 'Randevularim' },
    { id: 'new-appointment', label: 'Randevu Al' },
    { id: 'reports', label: 'Raporlarim' },
  ]

  return (
    <div className="panel-layout">
      <aside className="panel-sidebar patient-sidebar">
        <div>
          <p className="brand-kicker">EkinMedical</p>
          <h2 className="side-title">Hasta Paneli</h2>
          <div className="user-badge">
            <div className="user-avatar patient-av">{(user.firstName || '?')[0].toUpperCase()}</div>
            <div><p className="user-name">{user.firstName} {user.lastName}</p><p className="user-role">Hasta</p></div>
          </div>
        </div>
        <nav className="side-nav">
          {patTabs.map((t) => (
            <button key={t.id} className={tab === t.id ? 'tab active' : 'tab'} onClick={() => setTab(t.id)}>{t.label}</button>
          ))}
        </nav>
        <button className="btn-ghost" onClick={onLogout}>Cikis Yap</button>
      </aside>

      <main className="panel-content fade-in" key={tab}>
        {notice.text && <div className={notice.type === 'success' ? 'notice success' : 'notice error'}>{notice.text}</div>}

        {tab === 'dashboard' && (
          <section>
            <h1 className="page-title">Hos Geldiniz, {user.firstName}</h1>
            <div className="card-grid three">
              <StatCard title="Aktif Randevularim" value={activeAppts.length} color="#2563eb" />
              <StatCard title="Toplam Randevum" value={myAppointments.length} color="#059669" />
              <StatCard title="Raporlarim" value={myReports.length} color="#7c3aed" />
            </div>
            <div className="dash-grid">
              <div className="card">
                <h3>Yaklasan Randevularim</h3>
                {activeAppts.length === 0 ? <p className="muted">Aktif randevunuz yok.</p> : (
                  <ul className="mini-list">{activeAppts.slice(0, 5).map((a) => (
                    <li key={a.id}><span className="mini-name">{findDocName(a.doctorId)}</span><span className="mini-meta">{a.appointmentTime}</span></li>
                  ))}</ul>
                )}
              </div>
              <div className="card">
                <h3>Son Raporlarim</h3>
                {myReports.length === 0 ? <p className="muted">Raporunuz yok.</p> : (
                  <ul className="mini-list">{myReports.slice(0, 5).map((r) => (
                    <li key={r.id}><span className="mini-name">{r.title || r.fileName || '-'}</span><span className="mini-meta">{r.createdAt || '-'}</span></li>
                  ))}</ul>
                )}
              </div>
            </div>
            <div className="quick-actions">
              <button className="btn-action" onClick={() => setTab('new-appointment')}>Randevu Al</button>
              <button className="btn-action secondary" onClick={() => setTab('reports')}>Raporlarimi Gor</button>
            </div>
          </section>
        )}

        {tab === 'appointments' && (
          <section>
            <h1 className="page-title">Randevularim</h1>
            <div className="card">
              <div className="table-wrap">
                <table>
                  <thead><tr><th>ID</th><th>Doktor</th><th>Tarih</th><th>Durum</th><th>Islem</th></tr></thead>
                  <tbody>
                    {myAppointments.length === 0 ? <tr><td colSpan={5} className="empty-cell">Randevunuz yok.</td></tr> :
                      myAppointments.map((a) => (
                        <tr key={a.id}>
                          <td>{a.id}</td>
                          <td>{findDocName(a.doctorId)}</td>
                          <td>{a.appointmentTime}</td>
                          <td><span className={'status-badge ' + (a.status || 'AKTIF')}>{a.status || 'AKTIF'}</span></td>
                          <td>{(a.status === 'AKTIF' || !a.status) && <button className="btn-sm red" onClick={() => handleCancelAppt(a.id)}>Iptal Et</button>}</td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}

        {tab === 'new-appointment' && (
          <section>
            <h1 className="page-title">Randevu Al</h1>
            <div className="layout-two">
              <div className="card">
                <h2>Yeni Randevu</h2>
                <form className="form" onSubmit={handleNewAppointment}>
                  <select value={apptDoctor} onChange={(e) => setApptDoctor(e.target.value)}>
                    <option value="">Doktor seciniz</option>
                    {doctors.map((d) => <option key={d.id} value={d.id}>{d.name} - {d.branchName || d.branch || ''}</option>)}
                  </select>
                  <input type="date" value={apptDate} onChange={(e) => setApptDate(e.target.value)} min={new Date().toISOString().slice(0, 10)} />
                  <select value={apptTime} onChange={(e) => setApptTime(e.target.value)}>
                    <option value="">Saat seciniz</option>
                    <TimeSlots />
                  </select>
                  <p className="form-hint">Randevular 08:30 - 17:00 arasi, 15 dakika aralikla alinabilir.</p>
                  <button type="submit">Randevu Olustur</button>
                </form>
              </div>
              <div className="card">
                <h2>Mevcut Doktorlar</h2>
                <div className="doctor-cards">
                  {doctors.length === 0 ? <p className="muted">Doktor yok.</p> : doctors.map((d) => (
                    <div key={d.id} className="doctor-card">
                      <div className="doc-avatar">{(d.name || '?')[0].toUpperCase()}</div>
                      <div><p className="doc-name">{d.name}</p><p className="doc-branch">{d.branchName || d.branch || '-'}</p></div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </section>
        )}

        {tab === 'reports' && (
          <section>
            <h1 className="page-title">Raporlarim</h1>
            {myReports.length === 0 ? <div className="card"><p className="muted">Henuz raporunuz bulunmuyor.</p></div> : (
              <div className="report-grid">
                {myReports.map((r) => (
                  <div key={r.id} className="report-card">
                    <div className="report-preview">
                      {r.fileData && r.fileType && r.fileType.startsWith('image/') ? (
                        <img src={r.fileData} alt={r.title} />
                      ) : (
                        <div className="report-pdf-icon">PDF</div>
                      )}
                    </div>
                    <div className="report-info">
                      <p className="report-title">{r.title || '-'}</p>
                      <p className="report-meta">{r.fileName || '-'}</p>
                      <p className="report-meta">{r.createdAt ? new Date(r.createdAt).toLocaleDateString('tr-TR') : '-'}</p>
                    </div>
                    {r.fileData && (
                      <button className="btn-sm" onClick={() => { const w = window.open(''); w.document.write(r.fileType && r.fileType.startsWith('image/') ? `<img src="${r.fileData}" style="max-width:100%"/>` : `<iframe src="${r.fileData}" style="width:100%;height:100vh;border:none"></iframe>`); }}>Goruntule</button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  )
}

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

function TimeSlots() {
  const slots = []
  for (let h = 8; h <= 16; h++) {
    for (let m = 0; m < 60; m += 15) {
      if (h === 8 && m < 30) continue
      if (h === 16 && m > 45) continue
      if (h >= 17) continue
      const hh = String(h).padStart(2, '0')
      const mm = String(m).padStart(2, '0')
      slots.push(`${hh}:${mm}`)
    }
  }
  return slots.map((s) => <option key={s} value={s}>{s}</option>)
}

function StatCard({ title, value, color }) {
  return (
    <article className="stat-card" style={{ borderLeft: `4px solid ${color || '#2563eb'}` }}>
      <p>{title}</p>
      <h3>{value}</h3>
    </article>
  )
}
