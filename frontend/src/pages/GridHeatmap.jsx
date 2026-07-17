import React, { useState, useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import API from "../services/api";
import StatusIndicator from "../components/StatusIndicator";
import { 
  AlertTriangle, Wrench, Sparkles, Clock, Hammer, X, ChevronRight, Activity, Thermometer
} from "lucide-react";

// Create custom glowing markers for Map
const createMarkerIcon = (status) => {
  const dotColor = {
    HEALTHY: "bg-emerald-500 glow-green",
    WARNING: "bg-amber-500 glow-amber",
    FAULT: "bg-red-500 glow-red animate-pulse",
  }[status?.toUpperCase() || "HEALTHY"];

  const innerColor = {
    HEALTHY: "bg-emerald-400",
    WARNING: "bg-amber-400",
    FAULT: "bg-red-400",
  }[status?.toUpperCase() || "HEALTHY"];

  return L.divIcon({
    html: `
      <div class="relative w-6 h-6 flex items-center justify-center">
        <div class="absolute w-5 h-5 rounded-full ${dotColor} opacity-50"></div>
        <div class="w-2.5 h-2.5 rounded-full ${innerColor} border border-slate-900"></div>
      </div>
    `,
    className: "custom-map-marker",
    iconSize: [24, 24],
    iconAnchor: [12, 12],
  });
};

const GridHeatmap = () => {
  const [substations, setSubstations] = useState([]);
  const [activeAlerts, setActiveAlerts] = useState([]);
  const [activeTickets, setActiveTickets] = useState([]);
  const [selectedSub, setSelectedSub] = useState(null);
  const [subHistory, setSubHistory] = useState([]);
  const [subTelemetry, setSubTelemetry] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const fetchMapData = async () => {
    try {
      const [subsRes, alertsRes, ticketsRes] = await Promise.all([
        API.get("/substations"),
        API.get("/alerts"),
        API.get("/tickets"),
      ]);

      setSubstations(subsRes.data);
      setActiveAlerts(alertsRes.data.filter(a => a.status === "ACTIVE"));
      setActiveTickets(ticketsRes.data.filter(t => t.status !== "COMPLETED"));

      // Refresh currently selected substation details
      if (selectedSub) {
        const updatedSub = subsRes.data.find(s => s.id === selectedSub.id);
        if (updatedSub) setSelectedSub(updatedSub);
      }
    } catch (err) {
      console.error("Error fetching map coordinates data:", err);
    }
  };

  const selectSubstation = async (sub) => {
    setSelectedSub(sub);
    setDrawerOpen(true);

    try {
      // 1. Fetch recent telemetry
      const teleRes = await API.get("/telemetry/live");
      const subTele = teleRes.data.find(t => t.substationId === sub.id);
      setSubTelemetry(subTele || { voltage: 230, current: 10, temperature: 35, frequency: 50, power: 2.3 });

      // 2. Fetch history
      const histRes = await API.get(`/repair-history/substation/${sub.id}`);
      setSubHistory(histRes.data);
    } catch (err) {
      console.error("Error fetching selected substation details", err);
    }
  };

  useEffect(() => {
    fetchMapData();
    const interval = setInterval(fetchMapData, 5000);
    return () => clearInterval(interval);
  }, [selectedSub]);

  // Find alert and ticket for selected substation
  const currentAlert = selectedSub ? activeAlerts.find(a => a.substationId === selectedSub.id) : null;
  const currentTicket = selectedSub ? activeTickets.find(t => t.substationId === selectedSub.id) : null;

  return (
    <div className="h-[calc(100vh-140px)] flex flex-col md:flex-row gap-6 relative overflow-hidden">
      {/* Left Map View */}
      <div className="flex-1 glass-panel p-4 h-full relative z-10">
        <div className="absolute top-6 left-6 z-[1000] glass-panel bg-cardBg/90 px-4 py-2 border border-cardBorder text-xs text-slate-300 font-semibold flex items-center gap-3">
          <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-emerald-500 glow-green" /> Healthy</span>
          <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-amber-500 glow-amber" /> Warning</span>
          <span className="flex items-center gap-1.5"><span className="w-2.5 h-2.5 rounded-full bg-red-500 glow-red animate-ping" /> Fault / Alert</span>
        </div>

        {substations.length > 0 && (
          <MapContainer 
            center={[12.9716, 77.5946]} // Bangalore Coordinates
            zoom={13} 
            scrollWheelZoom={true}
            className="w-full h-full"
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {substations.map(sub => (
              <Marker 
                key={sub.id} 
                position={[sub.latitude, sub.longitude]}
                icon={createMarkerIcon(sub.status)}
                eventHandlers={{
                  click: () => selectSubstation(sub)
                }}
              >
                <Popup>
                  <div className="text-slate-100">
                    <p className="font-bold font-outfit text-sm">{sub.name}</p>
                    <p className="text-[10px] text-slate-400 mt-0.5">{sub.location}</p>
                    <button 
                      onClick={() => selectSubstation(sub)}
                      className="mt-2 text-xs text-brandBlue font-bold hover:underline flex items-center gap-0.5"
                    >
                      Open Diagnostics Drawer <ChevronRight size={12} />
                    </button>
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        )}
      </div>

      {/* Right Drawer panel (Sliding out overlay on mobile, fixed side card on desktop) */}
      <div className={`fixed inset-y-0 right-0 z-[1001] md:static w-full md:w-96 bg-cardBg/95 md:bg-cardBg/60 backdrop-blur-md border-l md:border border-cardBorder/60 md:rounded-2xl p-6 flex flex-col transition-transform duration-300 transform h-full ${
        drawerOpen ? "translate-x-0" : "translate-x-full md:translate-x-0 md:hidden"
      }`}>
        {selectedSub ? (
          <div className="flex-1 flex flex-col min-h-0 space-y-5">
            <div className="flex items-center justify-between border-b border-cardBorder pb-3">
              <div>
                <h3 className="font-outfit font-bold text-slate-200 text-lg">{selectedSub.name}</h3>
                <span className="text-[10px] text-slate-400 font-semibold">{selectedSub.location}</span>
              </div>
              <button 
                onClick={() => setDrawerOpen(false)}
                className="text-slate-400 hover:text-white p-1 hover:bg-slate-800 rounded-lg"
              >
                <X size={18} />
              </button>
            </div>

            {/* Scrollable details */}
            <div className="flex-1 overflow-y-auto pr-1 space-y-5 text-sm text-slate-300">
              
              {/* Status */}
              <div className="flex items-center justify-between bg-slate-900/40 p-3 rounded-xl border border-cardBorder/40">
                <span className="text-xs font-semibold text-slate-400">Current Health</span>
                <StatusIndicator status={selectedSub.status} />
              </div>

              {/* Telemetry metrics */}
              {subTelemetry && (
                <div className="space-y-2.5">
                  <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                    <Activity size={14} className="text-brandBlue" />
                    Live Electrical Feed
                  </h4>
                  <div className="grid grid-cols-2 gap-2 text-center text-xs">
                    <div className="bg-slate-900/30 p-2.5 rounded-lg border border-slate-800/40">
                      <p className="text-slate-500">Voltage</p>
                      <p className="font-bold text-sm text-slate-200 mt-0.5">{subTelemetry.voltage} V</p>
                    </div>
                    <div className="bg-slate-900/30 p-2.5 rounded-lg border border-slate-800/40">
                      <p className="text-slate-500">Current</p>
                      <p className="font-bold text-sm text-slate-200 mt-0.5">{subTelemetry.current} A</p>
                    </div>
                    <div className="bg-slate-900/30 p-2.5 rounded-lg border border-slate-800/40">
                      <p className="text-slate-500">Core Temp</p>
                      <p className="font-bold text-sm text-slate-200 mt-0.5">{subTelemetry.temperature} °C</p>
                    </div>
                    <div className="bg-slate-900/30 p-2.5 rounded-lg border border-slate-800/40">
                      <p className="text-slate-500">Power Load</p>
                      <p className="font-bold text-sm text-slate-200 mt-0.5">{subTelemetry.power} kW</p>
                    </div>
                  </div>
                </div>
              )}

              {/* Active Anomaly Alerts */}
              {currentAlert && (
                <div className="p-3.5 bg-red-500/5 border border-red-500/15 rounded-xl space-y-2 text-xs">
                  <h4 className="font-bold text-red-400 flex items-center gap-1">
                    <AlertTriangle size={14} /> Alert Raised
                  </h4>
                  <p className="text-slate-300 font-semibold">{currentAlert.message}</p>
                  <p className="text-[10px] text-slate-500">{new Date(currentAlert.timestamp).toLocaleString()}</p>
                </div>
              )}

              {/* AI Diagnoses & Tickets */}
              {currentTicket && (
                <div className="p-4 bg-brandBlue/5 border border-brandBlue/15 rounded-xl space-y-3">
                  <h4 className="text-xs font-bold text-brandBlue flex items-center gap-1.5">
                    <Sparkles size={14} className="text-brandBlue" />
                    AI Fault Diagnosis
                  </h4>
                  
                  <div className="space-y-2 text-xs">
                    <div>
                      <p className="text-slate-500">Probable Fault</p>
                      <p className="font-bold text-slate-200 text-sm mt-0.5">{currentTicket.probableFault}</p>
                      <div className="flex items-center gap-1 mt-0.5">
                        <span className="text-[10px] text-slate-400">Confidence:</span>
                        <span className="text-[10px] font-bold text-emerald-400">{currentTicket.confidenceScore}%</span>
                      </div>
                    </div>

                    <div>
                      <p className="text-slate-500">Recommended Repair</p>
                      <p className="text-slate-300 mt-0.5 leading-relaxed">{currentTicket.recommendedRepair}</p>
                    </div>

                    <div className="grid grid-cols-2 gap-2 pt-1 border-t border-cardBorder/40">
                      <div>
                        <p className="text-slate-500">Priority</p>
                        <span className="inline-block text-[10px] font-extrabold text-red-400 bg-red-500/10 px-2 py-0.5 rounded uppercase mt-0.5">
                          {currentTicket.priority}
                        </span>
                      </div>
                      <div>
                        <p className="text-slate-500">Assigned Tech</p>
                        <p className="text-slate-300 font-bold mt-0.5 truncate">{currentTicket.technicianName || "Searching..."}</p>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Completed Repairs History */}
              <div className="space-y-2.5">
                <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                  <Hammer size={14} className="text-slate-400" />
                  Recent Repair Logs
                </h4>
                {subHistory.length === 0 ? (
                  <p className="text-xs text-slate-500 italic">No prior repair history recorded.</p>
                ) : (
                  <div className="space-y-2 max-h-[160px] overflow-y-auto pr-1">
                    {subHistory.map(h => (
                      <div key={h.id} className="p-2.5 bg-slate-900/30 rounded-xl border border-cardBorder/40 text-xs">
                        <div className="flex items-center justify-between text-[10px] text-slate-500 mb-1">
                          <span className="flex items-center gap-1"><Clock size={10} /> {new Date(h.completedAt).toLocaleDateString()}</span>
                          <span className="font-semibold">{h.technicianName}</span>
                        </div>
                        <p className="font-semibold text-slate-200">{h.faultResolved}</p>
                        <p className="text-slate-400 mt-0.5 line-clamp-2">{h.notes}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>

            </div>
          </div>
        ) : (
          <div className="h-full flex flex-col justify-center items-center text-slate-500 text-center text-sm">
            <Map size={36} className="text-slate-600 mb-2" />
            Click any substation marker on the map to display diagnostics, active alerts, and AI fault recommendations.
          </div>
        )}
      </div>
    </div>
  );
};

export default GridHeatmap;
