import { useCallback, useEffect, useMemo, useState } from "react";
import {
  CircleMarker,
  MapContainer,
  Popup,
  TileLayer,
  useMap,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import "./index.css";

const API_URL = "http://localhost:8081/api/drones";

const INDIA_CENTER = [22.9734, 78.6569];

function normalizeStatus(status) {
  return String(status || "UNKNOWN").toUpperCase();
}

function batteryClass(value) {
  const battery = Number(value ?? 0);
  if (battery <= 20) return "critical";
  if (battery <= 40) return "warning";
  return "healthy";
}

function droneCoordinates(drone) {
  const latitude = Number(drone.latitude);
  const longitude = Number(drone.longitude);

  if (
      !Number.isFinite(latitude) ||
      !Number.isFinite(longitude) ||
      latitude < 6 ||
      latitude > 37.5 ||
      longitude < 67 ||
      longitude > 98
  ) {
    return null;
  }

  return [latitude, longitude];
}

function FlyToDrone({ position }) {
  const map = useMap();

  useEffect(() => {
    if (position) {
      map.flyTo(position, 12, { duration: 1 });
    }
  }, [map, position]);

  return null;
}

function MetricCard({ label, value, detail, icon, accent }) {
  return (
      <article className={`metric-card ${accent || ""}`}>
        <div className="metric-top">
          <span>{label}</span>
          <span className="metric-icon">{icon}</span>
        </div>
        <div className="metric-value">{value}</div>
        <div className="metric-detail">{detail}</div>
      </article>
  );
}

function App() {
  const [drones, setDrones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [lastUpdated, setLastUpdated] = useState(null);
  const [selectedDrone, setSelectedDrone] = useState(null);
  const [search, setSearch] = useState("");

  const fetchDrones = useCallback(async () => {
    try {
      const response = await fetch(API_URL);

      if (!response.ok) {
        throw new Error(`Backend returned HTTP ${response.status}`);
      }

      const data = await response.json();

      if (!Array.isArray(data)) {
        throw new Error("Expected an array of drones from the API.");
      }

      setDrones(data);
      setError("");
      setLastUpdated(new Date());
    } catch (err) {
      setError(
          `${err.message}. Check that Spring Boot is running on port 8081.`
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDrones();

    const timer = window.setInterval(fetchDrones, 5000);
    return () => window.clearInterval(timer);
  }, [fetchDrones]);

  const stats = useMemo(() => {
    const active = drones.filter((drone) =>
        ["ACTIVE", "IN_PROGRESS", "IN_FLIGHT", "EXECUTING"].includes(
            normalizeStatus(drone.status)
        )
    ).length;

    const idle = drones.filter((drone) =>
        ["IDLE", "AVAILABLE", "READY"].includes(
            normalizeStatus(drone.status)
        )
    ).length;

    const lowBattery = drones.filter(
        (drone) => Number(drone.batteryLevel ?? 0) <= 20
    ).length;

    return {
      total: drones.length,
      active,
      idle,
      lowBattery,
    };
  }, [drones]);

  const filteredDrones = useMemo(() => {
    const query = search.trim().toLowerCase();

    return drones.filter((drone) =>
        String(drone.name || drone.id || "")
            .toLowerCase()
            .includes(query)
    );
  }, [drones, search]);

  const mappedDrones = useMemo(
      () =>
          filteredDrones
              .map((drone) => ({
                drone,
                position: droneCoordinates(drone),
              }))
              .filter((item) => item.position),
      [filteredDrones]
  );

  const selectedPosition = selectedDrone
      ? droneCoordinates(selectedDrone)
      : null;

  function selectDrone(drone) {
    setSelectedDrone(drone);
  }

  return (
      <div className="dashboard">
        <aside className="sidebar">
          <a className="brand" href="#overview" aria-label="AeroSaga home">
            <span className="brand-symbol">A</span>
            <span>
            <strong>AEROSAGA</strong>
            <small>MISSION CONTROL</small>
          </span>
          </a>

          <div className="nav-heading">OPERATIONS</div>

          <nav className="navigation" aria-label="Main navigation">
            <a className="nav-item active" href="#overview">
              <span>◈</span> Overview
            </a>
            <a className="nav-item" href="#fleet">
              <span>⌁</span> Drone Fleet
            </a>
            <a className="nav-item" href="#map-panel">
              <span>◎</span> Live Map
            </a>
            <a className="nav-item" href="#alerts">
              <span>△</span> System Alerts
            </a>
          </nav>

          <div className="sidebar-bottom">
            <div className="connection-card">
              <span className={`connection-dot ${error ? "offline" : ""}`} />
              <div>
                <strong>{error ? "Backend Offline" : "System Monitor"}</strong>
                <small>{error ? "Check connection" : "Monitoring enabled"}</small>
              </div>
            </div>

            <div className="sidebar-version">
              AEROSAGA CONTROL SYSTEM
              <br />
              Development Environment
            </div>
          </div>
        </aside>

        <main className="main-content" id="overview">
          <header className="topbar">
            <div>
              <div className="eyebrow">FLEET OPERATIONS / OVERVIEW</div>
              <h1>Mission Control</h1>
              <p className="page-subtitle">
                Monitor your autonomous fleet and mission activity.
              </p>
            </div>

            <div className="topbar-actions">
              <div className={`system-pill ${error ? "offline-pill" : ""}`}>
                <span className="connection-dot" />
                {error ? "DISCONNECTED" : "SYSTEM ONLINE"}
              </div>

              <button
                  className="refresh-button"
                  type="button"
                  onClick={fetchDrones}
              >
                ↻ <span>Refresh</span>
              </button>
            </div>
          </header>

          {error && (
              <div className="error-banner" role="alert">
                <strong>API connection issue</strong>
                <span>{error}</span>
                <button type="button" onClick={fetchDrones}>
                  Retry
                </button>
              </div>
          )}

          <section className="metrics-grid" aria-label="Fleet summary">
            <MetricCard
                label="Total Fleet"
                value={loading ? "—" : stats.total}
                detail="Registered drones"
                icon="⌁"
            />
            <MetricCard
                label="Active Drones"
                value={loading ? "—" : stats.active}
                detail="Currently on mission"
                icon="↗"
                accent="metric-cyan"
            />
            <MetricCard
                label="Standby"
                value={loading ? "—" : stats.idle}
                detail="Available for dispatch"
                icon="◷"
                accent="metric-blue"
            />
            <MetricCard
                label="Low Battery"
                value={loading ? "—" : stats.lowBattery}
                detail="Battery at or below 20%"
                icon="ϟ"
                accent="metric-red"
            />
          </section>

          <section className="map-section" id="map-panel">
            <div className="section-header">
              <div>
                <div className="eyebrow">GEOSPATIAL MONITORING</div>
                <h2>Live Fleet Map</h2>
                <p>Drone positions reported by the backend API.</p>
              </div>

              <span className="map-live-badge">
              <span className="connection-dot" />
              AUTO REFRESH · 5 SEC
            </span>
            </div>

            <div className="map-wrapper">
              <MapContainer
                  center={INDIA_CENTER}
                  zoom={5}
                  scrollWheelZoom
                  className="fleet-map"
              >
                <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />

                <FlyToDrone position={selectedPosition} />

                {mappedDrones.map(({ drone, position }) => {
                  const battery = Number(drone.batteryLevel ?? 0);
                  const status = normalizeStatus(drone.status);
                  const color =
                      battery <= 20
                          ? "#fb5364"
                          : ["ACTIVE", "IN_PROGRESS", "IN_FLIGHT", "EXECUTING"].includes(
                              status
                          )
                              ? "#2ce4c6"
                              : "#63a5ff";

                  return (
                      <CircleMarker
                          key={drone.id}
                          center={position}
                          radius={selectedDrone?.id === drone.id ? 10 : 7}
                          pathOptions={{
                            color,
                            fillColor: color,
                            fillOpacity: 0.9,
                            weight: 2,
                          }}
                          eventHandlers={{
                            click: () => selectDrone(drone),
                          }}
                      >
                        <Popup>
                          <strong>{drone.name || `Drone ${drone.id}`}</strong>
                          <br />
                          Status: {status}
                          <br />
                          Battery: {battery}%
                          <br />
                          GPS: {drone.gpsStatus || "Unknown"}
                        </Popup>
                      </CircleMarker>
                  );
                })}
              </MapContainer>

              <div className="map-legend">
                <strong>MAP LEGEND</strong>
                <span><i className="legend-dot green" /> Active</span>
                <span><i className="legend-dot blue" /> Standby / Other</span>
                <span><i className="legend-dot red" /> Low battery</span>
              </div>

              <div className="map-counter">
                <span className="connection-dot" />
                {mappedDrones.length} DRONES MAPPED
              </div>
            </div>

            {mappedDrones.length === 0 && (
                <p className="map-note">
                  {loading
                      ? "Loading drone locations…"
                      : error
                          ? "Drone locations will appear after the backend reconnects."
                          : "No drones have valid coordinates in the configured India map bounds."}
                </p>
            )}
          </section>

          <section className="fleet-section" id="fleet">
            <div className="section-header fleet-heading">
              <div>
                <div className="eyebrow">ASSET MANAGEMENT</div>
                <h2>Drone Fleet</h2>
                <p>Select a drone to highlight its location on the map.</p>
              </div>

              <label className="search-box">
                <span aria-hidden="true">⌕</span>
                <input
                    type="search"
                    placeholder="Search drones..."
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    aria-label="Search drones"
                />
              </label>
            </div>

            <div className="table-wrapper">
              <table>
                <thead>
                <tr>
                  <th>DRONE</th>
                  <th>STATUS</th>
                  <th>BATTERY</th>
                  <th>GPS</th>
                  <th>LOCATION</th>
                  <th>ACTION</th>
                </tr>
                </thead>

                <tbody>
                {filteredDrones.map((drone) => {
                  const battery = Number(drone.batteryLevel ?? 0);
                  const position = droneCoordinates(drone);
                  const status = normalizeStatus(drone.status);

                  return (
                      <tr key={drone.id}>
                        <td>
                          <div className="drone-name">
                            <span className="drone-avatar">✦</span>
                            <span>
                            <strong>{drone.name || `Drone ${drone.id}`}</strong>
                            <small>ID: {drone.id}</small>
                          </span>
                          </div>
                        </td>

                        <td>
                        <span
                            className={`status-badge ${
                                ["ACTIVE", "IN_PROGRESS", "IN_FLIGHT", "EXECUTING"].includes(
                                    status
                                )
                                    ? "status-active"
                                    : ["IDLE", "AVAILABLE", "READY"].includes(status)
                                        ? "status-idle"
                                        : "status-other"
                            }`}
                        >
                          {status.replaceAll("_", " ")}
                        </span>
                        </td>

                        <td>
                          <div className="battery-cell">
                            <div className="battery-track">
                              <div
                                  className={`battery-fill ${batteryClass(battery)}`}
                                  style={{
                                    width: `${Math.max(0, Math.min(100, battery))}%`,
                                  }}
                              />
                            </div>
                            <span className={batteryClass(battery)}>
                            {battery}%
                          </span>
                          </div>
                        </td>

                        <td>
                        <span className="gps-value">
                          <i
                              className={`gps-dot ${
                                  String(drone.gpsStatus || "")
                                      .toLowerCase()
                                      .includes("connect")
                                      ? ""
                                      : "gps-off"
                              }`}
                          />
                          {drone.gpsStatus || "Unknown"}
                        </span>
                        </td>

                        <td className="coordinates">
                          {position
                              ? `${position[0].toFixed(4)}, ${position[1].toFixed(4)}`
                              : "No coordinates"}
                        </td>

                        <td>
                          <button
                              className="locate-button"
                              type="button"
                              disabled={!position}
                              onClick={() => {
                                selectDrone(drone);
                                document
                                    .getElementById("map-panel")
                                    ?.scrollIntoView({ behavior: "smooth" });
                              }}
                          >
                            Locate ↗
                          </button>
                        </td>
                      </tr>
                  );
                })}

                {!loading && filteredDrones.length === 0 && (
                    <tr>
                      <td colSpan="6" className="empty-state">
                        {error
                            ? "Waiting for the backend connection."
                            : "No drones match your search."}
                      </td>
                    </tr>
                )}

                {loading && (
                    <tr>
                      <td colSpan="6" className="empty-state">
                        Loading fleet data…
                      </td>
                    </tr>
                )}
                </tbody>
              </table>
            </div>

            <div className="table-footer">
            <span>
              Showing {filteredDrones.length} of {drones.length} registered drones
            </span>
              <span>
              Last updated:{" "}
                {lastUpdated
                    ? lastUpdated.toLocaleTimeString()
                    : "Waiting for data"}
            </span>
            </div>
          </section>

          <footer className="dashboard-footer">
            <span>AEROSAGA · AUTONOMOUS MISSION CONTROL</span>
            <span>Monitoring interface · Development build</span>
          </footer>
        </main>
      </div>
  );
}

export default App;