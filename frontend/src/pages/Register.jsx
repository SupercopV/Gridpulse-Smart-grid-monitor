import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { BatteryCharging, Lock, User, Mail, Shield, AlertCircle, FileText } from "lucide-react";
import authService from "../services/authService";

const Register = () => {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    fullName: "",
    role: "OPERATOR",
    password: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    try {
      await authService.register(
        formData.username,
        formData.password,
        formData.email,
        formData.role,
        formData.fullName
      );
      setSuccess("Registration successful! Redirecting to login...");
      setTimeout(() => {
        navigate("/login");
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed. Try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-darkBg flex flex-col justify-center items-center px-4 relative overflow-hidden">
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-brandBlue/5 rounded-full filter blur-[100px] pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-emerald-500/5 rounded-full filter blur-[100px] pointer-events-none" />

      <div className="w-full max-w-md space-y-6 z-10">
        <div className="text-center">
          <div className="inline-flex bg-brandBlue/10 p-3.5 rounded-2xl border border-brandBlue/20 text-brandBlue mb-4 shadow-lg shadow-brandBlue/10">
            <BatteryCharging size={36} />
          </div>
          <h1 className="font-outfit text-3xl font-extrabold tracking-tight bg-gradient-to-r from-blue-400 via-indigo-400 to-emerald-400 bg-clip-text text-transparent">
            GridPulse System
          </h1>
          <p className="text-slate-400 text-sm mt-1">Register a New Infrastructure Staff Account</p>
        </div>

        <div className="glass-panel p-8 shadow-2xl border-cardBorder/80">
          <h2 className="text-xl font-bold font-outfit text-slate-200 mb-5 text-center">Staff Account Registration</h2>

          {error && (
            <div className="flex items-center gap-2.5 p-3.5 bg-red-500/10 border border-red-500/20 text-red-400 rounded-xl text-xs mb-4">
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="flex items-center gap-2.5 p-3.5 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-xl text-xs mb-4 animate-pulse">
              <AlertCircle size={16} />
              <span>{success}</span>
            </div>
          )}

          <form onSubmit={handleRegister} className="space-y-4">
            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-400" htmlFor="fullName">Full Name</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                  <FileText size={16} />
                </span>
                <input
                  id="fullName"
                  type="text"
                  required
                  placeholder="Enter full name"
                  value={formData.fullName}
                  onChange={handleChange}
                  className="w-full bg-slate-950/60 border border-cardBorder rounded-xl py-2.5 pl-10 pr-4 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-brandBlue focus:ring-1 focus:ring-brandBlue/30 transition-all"
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-400" htmlFor="username">Username</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                  <User size={16} />
                </span>
                <input
                  id="username"
                  type="text"
                  required
                  placeholder="Create username"
                  value={formData.username}
                  onChange={handleChange}
                  className="w-full bg-slate-950/60 border border-cardBorder rounded-xl py-2.5 pl-10 pr-4 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-brandBlue focus:ring-1 focus:ring-brandBlue/30 transition-all"
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-400" htmlFor="email">Email Address</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                  <Mail size={16} />
                </span>
                <input
                  id="email"
                  type="email"
                  required
                  placeholder="e.g. employee@citygrid.org"
                  value={formData.email}
                  onChange={handleChange}
                  className="w-full bg-slate-950/60 border border-cardBorder rounded-xl py-2.5 pl-10 pr-4 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-brandBlue focus:ring-1 focus:ring-brandBlue/30 transition-all"
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-400" htmlFor="role">Security Role</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                  <Shield size={16} />
                </span>
                <select
                  id="role"
                  value={formData.role}
                  onChange={handleChange}
                  className="w-full bg-slate-950/60 border border-cardBorder rounded-xl py-2.5 pl-10 pr-4 text-sm text-slate-300 focus:outline-none focus:border-brandBlue focus:ring-1 focus:ring-brandBlue/30 transition-all appearance-none cursor-pointer"
                >
                  <option className="bg-slate-900 text-slate-100" value="OPERATOR">Grid Operator</option>
                  <option className="bg-slate-900 text-slate-100" value="ADMIN">System Administrator</option>
                  <option className="bg-slate-900 text-slate-100" value="TECHNICIAN">Field Technician</option>
                </select>
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-semibold text-slate-400" htmlFor="password">Security Password</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                  <Lock size={16} />
                </span>
                <input
                  id="password"
                  type="password"
                  required
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  className="w-full bg-slate-950/60 border border-cardBorder rounded-xl py-2.5 pl-10 pr-4 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-brandBlue focus:ring-1 focus:ring-brandBlue/30 transition-all"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-brandBlue hover:bg-blue-600 text-white font-semibold py-3 px-4 rounded-xl shadow-lg shadow-brandBlue/20 transition-all flex items-center justify-center gap-2 mt-4 disabled:opacity-50"
            >
              {loading ? "Registering account..." : "Register Staff Account"}
            </button>
          </form>

          <p className="text-center text-xs text-slate-400 mt-5">
            Already have an account?{" "}
            <Link to="/login" className="text-brandBlue hover:underline font-semibold">
              Login here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
