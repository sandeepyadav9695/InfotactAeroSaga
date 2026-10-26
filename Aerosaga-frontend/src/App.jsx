
import { useCallback, useEffect, useState } from 'react';
import {
  Activity,
  Battery,
  BatteryWarning,
  CircleAlert,
  Clock,
  MapPin,
  Plane,
  RefreshCw,
  RotateCcw,
  ShieldCheck,
  Square,
  XCircle,
} from 'lucide-react';
import './App.css';

const API = '/api';

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(body || `Request failed (${response.status})`);
  }

  if (response.status === 204) return null;

  const contentType = response.headers.get('content-type') || '';
  return contentType.includes('application/json')
      ? response.json()
      : null;
}

function batteryColor(level) {
  if (level <= 10) return 'critical';
  if (level <= 25) return 'warning';
  return 'healthy';
}

function statusClass(status = '') {
  return status.toLowerCase().replaceAll('_', '-');
}

function StatCard({ title, value, icon: Icon, detail }) {
  return (
      <article className="stat-card">
        <div className="stat-heading">
          <span>{title}</span>
          <Icon size={20} />
        </div>
        <div className="stat-value">{value ?? 0}</div>
        <div className="stat-detail">{detail}</div>
      </article>
  );
}

function BatteryBar({ level = 0 }) {
  const value = Math.max(0, Math.min(100, Number(level) || 0));
  const color = batteryColor(value);

  return (
      <div className="battery-row">
        <Battery size={17} />
        <div className="battery-track">
          <div
              className={`battery-fill ${color}`}
              style={{ width: `${value}%` }}
          />
        </div>
        <strong>{value.toFixed(1)}%</strong>
      </div>
  );
}

export default function App() {
  const [dashboard, setDashboard] = useState(null);
  const [drones, setDrones] = useState([]);
  const [missions, setMissions] = useState([]);
  const [selectedDrone, setSelectedDrone] = useState('');
  const [missionName, setMissionName] = useState('Delivery Mission');
  const [latitude, setLatitude] = useState('19.0760');
  const [longitude, setLongitude] = useState('72.8777');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [lastUpdated, setLastUpdated] = useState(null);

  const refresh = useCallback(async () => {
    try {
      const results = await Promise.all([
        apiRequest('/dashboard'),
        apiRequest('/drones'),
        apiRequest('/missions'),
      ]);

      setDashboard(results[0]);
      setDrones(Array.isArray(results[1]) ? results[1] : []);
      setMissions(Array.isArray(results[2]) ? results[2] : []);
      setError('');
      setLastUpdated(new Date());
    } catch (e) {
      setError(
          `Could not load backend data. Check Spring Boot on port 8081. ${e.message}`
      );
    }
  }, []);

  useEffect(() => {
    refresh();
    const timer = window.setInterval(refresh, 5000);
    return () => window.clearInterval(timer);
  }, [refresh]);

  async function runAction(action, missionId, body) {
    setBusy(true);
    setError('');
    setMessage('');

    try {
      await apiRequest(`/missions/${missionId}/${action}`, {
        method: 'POST',
        body: body ? JSON.stringify(body) : undefined,
      });
      setMessage(`${action.replace('-', ' ')} request submitted.`);
      await refresh();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }

  async function createMission(event) {
    event.preventDefault();
    setBusy(true);
    setError('');
    setMessage('');

    try {
      const created = await apiRequest('/missions', {
        method: 'POST',
        body: JSON.stringify({
          name: missionName,
          droneId: Number(selectedDrone),
          waypoints: [
            {
              latitude: Number(latitude),
              longitude: Number(longitude),
              altitude: 30,
            },
          ],
        }),
      });

      setMessage(`Mission ${created?.id ?? ''} created successfully.`);
      await refresh();
    } catch (e) {
      setError(
          `${e.message}. Confirm the request fields match your CreateMissionRequest DTO.`
      );
    } finally {
      setBusy(false);
    }
  }

  const stats = dashboard || {};
  const droneRows = stats.drones || drones;
  const missionRows = stats.missions || missions;
  const alerts = stats.alerts || [];

  return (
      <main className="app-shell">
        <header className="topbar">
          <div className="brand">
            <div className="brand-icon"><Plane size={25} /></div>
            <div>
              <h1>AeroSaga</h1>
              <p>Autonomous Drone Mission Control</p>
            </div>
          </div>
          <div className="topbar-right">
            <span className="live-dot" />
            <span>Monitoring</span>
            <button className="icon-button" onClick={refresh} title="Refresh">
              <RefreshCw size={17} />
            </button>
          </div>
        </header>

        <section className="welcome">
          <div>
            <p className="eyebrow">FLEET OVERVIEW</p>
            <h2>Mission Control Center</h2>
            <p className="muted">
              Monitor drone health, mission execution, and battery status.
            </p>
          </div>
          <div className="updated">
            <Clock size={15} />
            {lastUpdated
                ? `Updated ${lastUpdated.toLocaleTimeString()}`
                : 'Waiting for data'}
          </div>
        </section>

        {error && <div className="notice error"><CircleAlert size={18} />{error}</div>}
        {message && <div className="notice success"><ShieldCheck size={18} />{message}</div>}

        <section className="stats-grid">
          <StatCard title="Total Drones" value={stats.totalDrones ?? drones.length}
                    icon={Plane} detail="Registered fleet" />
          <StatCard title="Idle Drones" value={stats.idleDrones}
                    icon={ShieldCheck} detail="Available for missions" />
          <StatCard title="Active Missions" value={stats.activeMission ?? stats.activeMissions}
                    icon={Activity} detail="Currently in progress" />
          <StatCard title="Low Battery" value={stats.lowBatteryDrone ?? stats.lowBatteryDrones}
                    icon={BatteryWarning} detail="At or below return threshold" />
        </section>

        <section className="content-grid">
          <article className="panel">
            <div className="panel-header">
              <div>
                <h3>Drone Fleet</h3>
                <p>Current status and battery levels</p>
              </div>
              <span className="count-pill">{droneRows.length} drones</span>
            </div>

            {droneRows.length === 0 ? (
                <div className="empty-state">No drones found. Register a drone through the API.</div>
            ) : (
                <div className="table-wrap">
                  <table>
                    <thead>
                    <tr><th>Drone</th><th>Status</th><th>Battery</th><th>GPS</th></tr>
                    </thead>
                    <tbody>
                    {droneRows.map((d) => {
                      const id = d.id;
                      const name = d.name ?? d.droneName ?? `Drone ${id}`;
                      const level = d.batteryPercent ?? d.batteryLevel ?? 0;

                      return (
                          <tr key={id}>
                            <td>
                              <div className="drone-name">
                                <span className="drone-avatar"><Plane size={16} /></span>
                                <div><strong>{name}</strong><small>ID: {id}</small></div>
                              </div>
                            </td>
                            <td><span className={`status-pill ${statusClass(d.status)}`}>{d.status ?? 'UNKNOWN'}</span></td>
                            <td className="battery-cell"><BatteryBar level={level} /></td>
                            <td>{d.gpsStatus ?? 'UNKNOWN'}</td>
                          </tr>
                      );
                    })}
                    </tbody>
                  </table>
                </div>
            )}
          </article>

          <article className="panel alerts-panel">
            <div className="panel-header">
              <div><h3>Battery Alerts</h3><p>Threshold monitoring</p></div>
              <BatteryWarning size={20} />
            </div>

            {alerts.length === 0 ? (
                <div className="empty-state">
                  <ShieldCheck size={28} />
                  <strong>No active battery alerts</strong>
                  <span>Alerts appear at or below the configured threshold.</span>
                </div>
            ) : alerts.map((a, i) => (
                <div className={`alert-item ${String(a.severity).toLowerCase()}`} key={`${a.droneId}-${i}`}>
                  <CircleAlert size={18} />
                  <div>
                    <strong>{a.droneName} Â· {a.batteryPercent}%</strong>
                    <p>{a.message}</p>
                  </div>
                </div>
            ))}

            <div className="threshold-legend">
              <span><i className="legend-dot warning-dot" /> Warning â‰¤ 25%</span>
              <span><i className="legend-dot critical-dot" /> Critical â‰¤ 10%</span>
            </div>
          </article>
        </section>

        <section className="content-grid lower-grid">
          <article className="panel">
            <div className="panel-header">
              <div><h3>Mission Operations</h3><p>Create a simulated delivery mission</p></div>
            </div>
            <form className="mission-form" onSubmit={createMission}>
              <label>
                Mission name
                <input value={missionName} onChange={e => setMissionName(e.target.value)} required />
              </label>
              <label>
                Available drone
                <select value={selectedDrone} onChange={e => setSelectedDrone(e.target.value)} required>
                  <option value="">Select a drone</option>
                  {drones.filter(d => (d.status ?? 'IDLE') === 'IDLE')
                      .map(d => (
                          <option key={d.id} value={d.id}>
                            {d.name ?? d.droneName ?? `Drone ${d.id}`} â€” {d.batteryLevel ?? 0}%
                          </option>
                      ))}
                </select>
              </label>
              <div className="coordinate-grid">
                <label>Latitude
                  <input type="number" step="any" min="-90" max="90"
                         value={latitude} onChange={e => setLatitude(e.target.value)} required />
                </label>
                <label>Longitude
                  <input type="number" step="any" min="-180" max="180"
                         value={longitude} onChange={e => setLongitude(e.target.value)} required />
                </label>
              </div>
              <button className="primary-button" type="submit" disabled={busy || !selectedDrone}>
                <Plane size={17} /> Create Mission
              </button>
            </form>
          </article>

          <article className="panel">
            <div className="panel-header">
              <div><h3>Mission Activity</h3><p>Start and manage Temporal workflows</p></div>
              <span className="count-pill">{missionRows.length} missions</span>
            </div>

            {missionRows.length === 0 ? (
                <div className="empty-state">No missions created yet.</div>
            ) : (
                <div className="mission-list">
                  {missionRows.map((m) => (
                      <div className="mission-item" key={m.id}>
                        <div className="mission-info">
                          <span className="mission-icon"><MapPin size={17} /></span>
                          <div>
                            <strong>{m.name ?? m.missionName ?? `Mission ${m.id}`}</strong>
                            <small>Mission #{m.id} Â· Drone {m.droneId ?? 'â€”'}</small>
                            <span className={`status-pill ${statusClass(m.status)}`}>{m.status}</span>
                          </div>
                        </div>
                        <div className="mission-actions">
                          <button disabled={busy} title="Start workflow"
                                  onClick={() => runAction('start', m.id, { timeScale: 60 })}>
                            <Plane size={15} /> Start
                          </button>
                          <button disabled={busy} title="Request return home"
                                  onClick={() => runAction('return-home', m.id)}>
                            <RotateCcw size={15} />
                          </button>
                          <button disabled={busy} title="Request cancellation"
                                  onClick={() => runAction('cancel', m.id)}>
                            <XCircle size={15} />
                          </button>
                        </div>
                      </div>
                  ))}
                </div>
            )}
          </article>
        </section>

        <footer>
          <span><Activity size={15} /> AeroSaga Mission Control</span>
          <span>Simulation only Â· Not connected to physical drones</span>
        </footer>
      </main>
  );
}

