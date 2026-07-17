import React, { useState, useEffect } from "react";
import { 
  UserPlus, Edit2, Trash2, Shield, User, Phone, Briefcase, RefreshCw, X 
} from "lucide-react";
import API from "../services/api";

const Technicians = () => {
  const [technicians, setTechnicians] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [currentTech, setCurrentTech] = useState(null); // null means adding new
  const [formData, setFormData] = useState({
    name: "",
    skills: "",
    availability: "AVAILABLE",
    phone: "",
  });
  const [actionMsg, setActionMsg] = useState("");

  const fetchTechs = async () => {
    try {
      const res = await API.get("/technicians");
      setTechnicians(res.data);
    } catch (err) {
      console.error("Error fetching technicians", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTechs();
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
  };

  const handleAddOrUpdate = async (e) => {
    e.preventDefault();
    try {
      if (currentTech) {
        // Update
        await API.put(`/technicians/${currentTech.id}`, formData);
        setActionMsg("Technician successfully updated.");
      } else {
        // Add
        await API.post("/technicians", formData);
        setActionMsg("Technician successfully added.");
      }
      setModalOpen(false);
      fetchTechs();
      setTimeout(() => setActionMsg(""), 4000);
    } catch (err) {
      console.error("Failed to save technician", err);
      setActionMsg("Failed to save technician.");
    }
  };

  const handleEdit = (tech) => {
    setCurrentTech(tech);
    setFormData({
      name: tech.name,
      skills: tech.skills,
      availability: tech.availability,
      phone: tech.phone || "",
    });
    setModalOpen(true);
  };

  const handleDelete = async (techId) => {
    if (!window.confirm("Are you sure you want to remove this technician from the grid registry?")) return;
    try {
      await API.delete(`/technicians/${techId}`);
      setActionMsg("Technician profile deleted.");
      fetchTechs();
      setTimeout(() => setActionMsg(""), 4000);
    } catch (err) {
      console.error("Failed to delete technician", err);
    }
  };

  const openAddModal = () => {
    setCurrentTech(null);
    setFormData({
      name: "",
      skills: "",
      availability: "AVAILABLE",
      phone: "",
    });
    setModalOpen(true);
  };

  const getAvailStyle = (avail) => {
    return {
      AVAILABLE: "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20",
      ON_JOB: "bg-blue-500/10 text-blue-400 border border-blue-500/20",
      OFF_DUTY: "bg-slate-500/10 text-slate-400 border border-slate-500/20",
    }[avail?.toUpperCase() || "AVAILABLE"];
  };

  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="font-outfit text-3xl font-bold tracking-tight text-white">Technicians Registry</h1>
          <p className="text-slate-400 text-sm mt-1">Manage municipal utility field crew, skill specializations, and dispatch status.</p>
        </div>
        <button
          onClick={openAddModal}
          className="self-start bg-brandBlue hover:bg-blue-600 text-white text-xs font-bold px-4 py-2.5 rounded-xl transition-all shadow-lg shadow-brandBlue/15 flex items-center gap-1.5"
        >
          <UserPlus size={15} /> Add Technician
        </button>
      </div>

      {actionMsg && (
        <div className="p-3.5 bg-brandBlue/10 border border-brandBlue/20 rounded-xl text-xs text-brandBlue font-semibold">
          {actionMsg}
        </div>
      )}

      {/* Grid List */}
      <div className="glass-panel p-6">
        {loading ? (
          <div className="text-center py-10 text-slate-500">Loading technicians list...</div>
        ) : technicians.length === 0 ? (
          <div className="text-center py-12 text-slate-500">No technicians registered in grid registry.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-300">
              <thead>
                <tr className="border-b border-cardBorder text-slate-400 text-xs uppercase font-semibold">
                  <th className="py-3 px-4">Name</th>
                  <th className="py-3 px-4">Phone</th>
                  <th className="py-3 px-4">Skill Specialisation</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Active Jobs</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-cardBorder/50">
                {technicians.map(tech => (
                  <tr key={tech.id} className="hover:bg-slate-800/10 transition-all">
                    <td className="py-4 px-4 font-bold text-slate-200">{tech.name}</td>
                    <td className="py-4 px-4 text-xs font-medium text-slate-400">{tech.phone || "N/A"}</td>
                    <td className="py-4 px-4 text-xs font-semibold text-slate-300">{tech.skills}</td>
                    <td className="py-4 px-4">
                      <span className={`inline-block text-[10px] font-bold px-2 py-0.5 rounded-full uppercase ${getAvailStyle(tech.availability)}`}>
                        {tech.availability.replace("_", " ")}
                      </span>
                    </td>
                    <td className="py-4 px-4 text-xs font-bold text-brandBlue">{tech.currentJobs} Open Jobs</td>
                    <td className="py-4 px-4 text-right space-x-2 whitespace-nowrap">
                      <button
                        onClick={() => handleEdit(tech)}
                        className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl transition-all inline-flex border border-slate-700/60"
                      >
                        <Edit2 size={13} />
                      </button>
                      <button
                        onClick={() => handleDelete(tech.id)}
                        className="p-2 bg-red-500/5 hover:bg-red-500/10 text-red-400 rounded-xl transition-all inline-flex border border-red-500/15"
                      >
                        <Trash2 size={13} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* CRUD Form Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="glass-panel w-full max-w-md p-6 bg-cardBg shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between border-b border-cardBorder pb-3">
              <h3 className="font-outfit font-bold text-lg text-slate-100 flex items-center gap-1.5">
                <Briefcase size={18} className="text-brandBlue" />
                {currentTech ? "Edit Technician Details" : "Register New Technician"}
              </h3>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-white">
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleAddOrUpdate} className="space-y-3.5 text-xs text-slate-300">
              <div className="space-y-1">
                <label className="text-xs font-semibold text-slate-400 block" htmlFor="name">Full Name</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                    <User size={13} />
                  </span>
                  <input
                    id="name"
                    type="text"
                    required
                    placeholder="Enter technician name"
                    value={formData.name}
                    onChange={handleChange}
                    className="w-full bg-slate-900 border border-cardBorder rounded-xl py-2 pl-8 pr-3 text-slate-200 placeholder-slate-600 focus:outline-none"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-semibold text-slate-400 block" htmlFor="skills">Skills Specialisation</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                    <Briefcase size={13} />
                  </span>
                  <input
                    id="skills"
                    type="text"
                    required
                    placeholder="e.g. Transformer Specialist, Cable Repair"
                    value={formData.skills}
                    onChange={handleChange}
                    className="w-full bg-slate-900 border border-cardBorder rounded-xl py-2 pl-8 pr-3 text-slate-200 placeholder-slate-600 focus:outline-none"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-semibold text-slate-400 block" htmlFor="phone">Phone Number</label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                    <Phone size={13} />
                  </span>
                  <input
                    id="phone"
                    type="text"
                    required
                    placeholder="e.g. +91-9876543210"
                    value={formData.phone}
                    onChange={handleChange}
                    className="w-full bg-slate-900 border border-cardBorder rounded-xl py-2 pl-8 pr-3 text-slate-200 placeholder-slate-600 focus:outline-none"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-semibold text-slate-400 block" htmlFor="availability">Availability Status</label>
                <select
                  id="availability"
                  value={formData.availability}
                  onChange={handleChange}
                  className="w-full bg-slate-900 border border-cardBorder rounded-xl py-2 px-3 text-slate-300 focus:outline-none cursor-pointer"
                >
                  <option value="AVAILABLE">Available</option>
                  <option value="ON_JOB">On Job</option>
                  <option value="OFF_DUTY">Off Duty</option>
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold px-4 py-2 rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="bg-brandBlue hover:bg-blue-600 text-white font-bold px-4 py-2 rounded-xl"
                >
                  {currentTech ? "Save Changes" : "Register Technician"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Technicians;
