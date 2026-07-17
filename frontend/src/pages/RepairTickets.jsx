import React, { useState, useEffect } from "react";
import { 
  Wrench, ShieldAlert, Clock, Sparkles, User, FileText, CheckCircle2, AlertCircle
} from "lucide-react";
import API from "../services/api";
import authService from "../services/authService";

const RepairTickets = () => {
  const [tickets, setTickets] = useState([]);
  const [technicians, setTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [completeModalOpen, setCompleteModalOpen] = useState(false);
  const [repairNotes, setRepairNotes] = useState("");
  const [assignedTechId, setAssignedTechId] = useState("");
  const [actionMsg, setActionMsg] = useState("");
  const currentUser = authService.getCurrentUser();

  const fetchData = async () => {
    try {
      const [ticketsRes, techsRes] = await Promise.all([
        API.get("/tickets"),
        API.get("/technicians"),
      ]);
      setTickets(ticketsRes.data);
      setTechnicians(techsRes.data);
    } catch (err) {
      console.error("Error fetching tickets data:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleAssignTechnician = async () => {
    if (!selectedTicket || !assignedTechId) return;
    try {
      await API.put(`/tickets/${selectedTicket.id}`, {
        technicianId: parseInt(assignedTechId),
      });
      setActionMsg("Technician successfully assigned to ticket.");
      setAssignModalOpen(false);
      fetchData();
      setTimeout(() => setActionMsg(""), 4000);
    } catch (err) {
      console.error("Assign technician failed", err);
      setActionMsg("Failed to assign technician.");
    }
  };

  const handleUpdateStatus = async (ticketId, status) => {
    try {
      await API.put(`/tickets/${ticketId}`, { status });
      setActionMsg(`Ticket status updated to ${status}.`);
      fetchData();
      setTimeout(() => setActionMsg(""), 4000);
    } catch (err) {
      console.error("Failed to update status", err);
    }
  };

  const handleCompleteTicket = async () => {
    if (!selectedTicket || !repairNotes.trim()) return;
    try {
      await API.put(`/tickets/${selectedTicket.id}`, {
        status: "COMPLETED",
        repairNotes: repairNotes,
      });
      setActionMsg("Ticket successfully marked completed. Substation status restored.");
      setCompleteModalOpen(false);
      setRepairNotes("");
      fetchData();
      setTimeout(() => setActionMsg(""), 4000);
    } catch (err) {
      console.error("Failed to close ticket", err);
    }
  };

  const openAssignModal = (ticket) => {
    setSelectedTicket(ticket);
    setAssignedTechId(ticket.technicianId ? ticket.technicianId.toString() : "");
    setAssignModalOpen(true);
  };

  const openCompleteModal = (ticket) => {
    setSelectedTicket(ticket);
    setRepairNotes("");
    setCompleteModalOpen(true);
  };

  const getPriorityStyle = (prio) => {
    return {
      CRITICAL: "bg-red-500/10 text-red-400 border border-red-500/25",
      HIGH: "bg-orange-500/10 text-orange-400 border border-orange-500/25",
      MEDIUM: "bg-amber-500/10 text-amber-400 border border-amber-500/25",
      LOW: "bg-emerald-500/10 text-emerald-400 border border-emerald-500/25",
    }[prio?.toUpperCase() || "LOW"];
  };

  const getStatusStyle = (status) => {
    return {
      OPEN: "bg-red-500/15 text-red-400 border border-red-500/20",
      ASSIGNED: "bg-blue-500/15 text-blue-400 border border-blue-500/20",
      IN_PROGRESS: "bg-amber-500/15 text-amber-400 border border-amber-500/20 animate-pulse",
      COMPLETED: "bg-emerald-500/15 text-emerald-400 border border-emerald-500/20",
    }[status?.toUpperCase() || "OPEN"];
  };

  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-outfit text-3xl font-bold tracking-tight text-white">Repair Work Orders</h1>
          <p className="text-slate-400 text-sm mt-1">Track infrastructure repairs, technician jobs, and AI diagnostics.</p>
        </div>
        <button 
          onClick={fetchData}
          className="self-start bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold px-3.5 py-2.5 rounded-xl border border-slate-700/60 transition-all"
        >
          Refresh Board
        </button>
      </div>

      {actionMsg && (
        <div className="p-3.5 bg-brandBlue/10 border border-brandBlue/20 rounded-xl text-xs text-brandBlue font-semibold">
          {actionMsg}
        </div>
      )}

      {/* Tickets Table */}
      <div className="glass-panel p-6">
        {loading ? (
          <div className="text-center py-10 text-slate-500">Loading work orders...</div>
        ) : tickets.length === 0 ? (
          <div className="text-center py-12 text-slate-500">No active repair tickets opened.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-300">
              <thead>
                <tr className="border-b border-cardBorder text-slate-400 text-xs uppercase font-semibold">
                  <th className="py-3 px-4">Ticket ID</th>
                  <th className="py-3 px-4">Substation</th>
                  <th className="py-3 px-4">Probable Fault Diagnosis</th>
                  <th className="py-3 px-4">Priority</th>
                  <th className="py-3 px-4">Technician</th>
                  <th className="py-3 px-4">ETA</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-cardBorder/50">
                {tickets.map(ticket => (
                  <tr key={ticket.id} className="hover:bg-slate-800/10 transition-all">
                    <td className="py-4 px-4 font-semibold font-outfit text-slate-400">#TKT-{ticket.id}</td>
                    <td className="py-4 px-4 font-bold text-slate-200">{ticket.substationName}</td>
                    <td className="py-4 px-4">
                      <div className="space-y-0.5">
                        <p className="font-semibold text-slate-200 line-clamp-1">{ticket.probableFault}</p>
                        <button
                          onClick={() => setSelectedTicket(ticket)}
                          className="text-[10px] text-brandBlue hover:underline flex items-center gap-0.5 font-bold"
                        >
                          <Sparkles size={11} /> View AI Recommendation
                        </button>
                      </div>
                    </td>
                    <td className="py-4 px-4">
                      <span className={`inline-block text-[10px] font-extrabold px-2.5 py-0.5 rounded-full uppercase ${getPriorityStyle(ticket.priority)}`}>
                        {ticket.priority}
                      </span>
                    </td>
                    <td className="py-4 px-4 text-xs font-semibold text-slate-300">
                      {ticket.technicianName ? (
                        <span className="flex items-center gap-1.5"><User size={13} className="text-slate-400" /> {ticket.technicianName}</span>
                      ) : (
                        <span className="text-slate-500 italic">Unassigned</span>
                      )}
                    </td>
                    <td className="py-4 px-4 text-xs text-slate-400 font-medium">
                      {ticket.etaHours ? `${ticket.etaHours} hrs` : "N/A"}
                    </td>
                    <td className="py-4 px-4">
                      <span className={`inline-block text-[10px] font-bold px-2 py-0.5 rounded-full uppercase ${getStatusStyle(ticket.status)}`}>
                        {ticket.status}
                      </span>
                    </td>
                    <td className="py-4 px-4 text-right space-x-1.5 whitespace-nowrap">
                      {/* Admin Actions */}
                      {currentUser?.role === "ADMIN" && ticket.status !== "COMPLETED" && (
                        <button
                          onClick={() => openAssignModal(ticket)}
                          className="bg-brandBlue/10 hover:bg-brandBlue/25 text-brandBlue border border-brandBlue/20 text-xs font-bold px-2.5 py-1.5 rounded-xl transition-all"
                        >
                          {ticket.technicianId ? "Reassign" : "Assign Tech"}
                        </button>
                      )}

                      {/* Technician Actions */}
                      {currentUser?.role === "TECHNICIAN" && ticket.status === "ASSIGNED" && (
                        <button
                          onClick={() => handleUpdateStatus(ticket.id, "IN_PROGRESS")}
                          className="bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 border border-amber-500/20 text-xs font-bold px-2.5 py-1.5 rounded-xl transition-all"
                        >
                          Start Repair
                        </button>
                      )}

                      {ticket.status === "IN_PROGRESS" && (
                        <button
                          onClick={() => openCompleteModal(ticket)}
                          className="bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/20 text-xs font-bold px-2.5 py-1.5 rounded-xl transition-all"
                        >
                          Complete
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* AI Recommendation Drawer / Modal */}
      {selectedTicket && !assignModalOpen && !completeModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="glass-panel w-full max-w-lg p-6 bg-cardBg shadow-2xl border-brandBlue/20 space-y-5 animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-cardBorder pb-3">
              <h3 className="font-outfit font-bold text-lg text-slate-100 flex items-center gap-1.5">
                <Sparkles size={18} className="text-brandBlue" />
                AI Diagnosis Report
              </h3>
              <button 
                onClick={() => setSelectedTicket(null)}
                className="text-slate-400 hover:text-white"
              >
                Close
              </button>
            </div>

            <div className="space-y-4 text-sm text-slate-300">
              <div>
                <p className="text-slate-500 text-xs font-semibold">Substation</p>
                <p className="font-bold text-slate-200 mt-0.5">{selectedTicket.substationName}</p>
              </div>

              <div>
                <p className="text-slate-500 text-xs font-semibold">Probable Infrastructure Fault</p>
                <p className="font-bold text-slate-200 mt-0.5 text-base">{selectedTicket.probableFault}</p>
                <div className="flex items-center gap-2 mt-1">
                  <span className="text-[10px] text-slate-400">AI Confidence:</span>
                  <span className="text-xs font-bold text-emerald-400">{selectedTicket.confidenceScore}%</span>
                </div>
              </div>

              <div>
                <p className="text-slate-500 text-xs font-semibold">Recommended Field Action Plan</p>
                <div className="bg-slate-900/40 border border-cardBorder p-3 rounded-xl mt-1.5 text-slate-200 leading-relaxed text-xs">
                  {selectedTicket.recommendedRepair}
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 border-t border-cardBorder/60 pt-4">
                <div>
                  <p className="text-slate-500 text-xs font-semibold">Suggested Specialization</p>
                  <p className="text-slate-300 font-bold mt-0.5">
                    {selectedTicket.probableFault.toLowerCase().contains("transformer") ? "Transformer Specialist" : 
                     selectedTicket.probableFault.toLowerCase().contains("cable") ? "Cable Repair" : "Grid Automation"}
                  </p>
                </div>
                <div>
                  <p className="text-slate-500 text-xs font-semibold">Estimated Repair Time</p>
                  <p className="text-slate-300 font-bold mt-0.5">{selectedTicket.etaHours} Hours</p>
                </div>
              </div>
            </div>

            <div className="text-right pt-2">
              <button
                onClick={() => setSelectedTicket(null)}
                className="bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold px-4.5 py-2.5 rounded-xl transition-all"
              >
                Close Report
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Admin Assign Tech Modal */}
      {assignModalOpen && selectedTicket && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="glass-panel w-full max-w-md p-6 bg-cardBg shadow-2xl space-y-4">
            <h3 className="font-outfit font-bold text-lg text-slate-100 flex items-center gap-2">
              <User size={18} className="text-brandBlue" />
              Assign Field Technician
            </h3>
            
            <p className="text-xs text-slate-400 leading-relaxed">
              Dispatch a technician for the fault: <strong>{selectedTicket.probableFault}</strong>.
            </p>

            <div className="space-y-3">
              <label className="text-xs font-semibold text-slate-400 block" htmlFor="tech-select">Choose Available Technician:</label>
              <select
                id="tech-select"
                value={assignedTechId}
                onChange={(e) => setAssignedTechId(e.target.value)}
                className="w-full bg-slate-900 border border-cardBorder text-slate-300 rounded-xl px-3 py-2.5 text-xs focus:outline-none"
              >
                <option value="">-- Select Technician --</option>
                {technicians.map(t => (
                  <option key={t.id} value={t.id} disabled={t.availability === "OFF_DUTY"}>
                    {t.name} - {t.skills.split(",")[0]} ({t.availability} | Active: {t.currentJobs})
                  </option>
                ))}
              </select>
            </div>

            <div className="flex justify-end gap-2 pt-2 text-xs">
              <button
                onClick={() => setAssignModalOpen(false)}
                className="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold px-4 py-2.5 rounded-xl"
              >
                Cancel
              </button>
              <button
                onClick={handleAssignTechnician}
                disabled={!assignedTechId}
                className="bg-brandBlue hover:bg-blue-600 text-white font-bold px-4 py-2.5 rounded-xl disabled:opacity-50"
              >
                Dispatch Technician
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Technician Complete Ticket Modal */}
      {completeModalOpen && selectedTicket && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="glass-panel w-full max-w-md p-6 bg-cardBg shadow-2xl space-y-4">
            <h3 className="font-outfit font-bold text-lg text-slate-100 flex items-center gap-2">
              <CheckCircle2 size={18} className="text-emerald-400" />
              Complete Repair Ticket
            </h3>

            <div className="space-y-3 text-xs">
              <div>
                <label className="text-xs font-semibold text-slate-400 block mb-1.5" htmlFor="repair-notes">Repair Actions & Findings:</label>
                <textarea
                  id="repair-notes"
                  required
                  placeholder="Detail the repair actions performed (e.g. Swapped circuit breaker, re-torqued cable terminals, tested load values)."
                  value={repairNotes}
                  onChange={(e) => setRepairNotes(e.target.value)}
                  className="w-full bg-slate-900 border border-cardBorder text-slate-300 rounded-xl p-3 h-28 focus:outline-none focus:border-emerald-500"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2 text-xs">
              <button
                onClick={() => setCompleteModalOpen(false)}
                className="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold px-4 py-2.5 rounded-xl"
              >
                Cancel
              </button>
              <button
                onClick={handleCompleteTicket}
                disabled={!repairNotes.trim()}
                className="bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-bold px-4 py-2.5 rounded-xl disabled:opacity-50"
              >
                Resolve Ticket
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RepairTickets;
