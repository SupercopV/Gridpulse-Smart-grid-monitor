import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { BatteryCharging, Lock, User, AlertCircle } from "lucide-react";
import authService from "../services/authService";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await authService.login(username, password);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.message || "Invalid credentials. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-darkBg flex flex-col justify-center items-center px-4 relative overflow-hidden">
      {/* Decorative Blur Orbs */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-brandBlue/5 rounded-full filter blur-[100px] pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-emerald-500/5 rounded-full filter blur-[100px] pointer-events-none" />

      <div className="w-full max-w-md space-y-8 z-10">
        <div className="text-center">
          <div className="inline-flex bg-brandBlue/10 p-3.5 rounded-2xl border border-brandBlue/20 text-brandBlue mb-4 shadow-lg shadow-brandBlue/10 animate-bounce">
            <BatteryCharging size={36} />
          </div>
          <h1 className="font-outfit text-3xl font-extrabold tracking-tight bg-gradient-to-r from-blue-400 via-indigo-400 to-emerald-400 bg-clip-text text-transparent">
            GridPulse Portal
          </h1>
          <p className="text-slate-400 text-sm mt-2">Smart Municipal Grid Monitoring Console</p>
        </div>

        <div className="glass-panel p-8 shadow-2xl border-cardBorder/80">
          <h2 className="text-xl font-bold font-outfit text-slate-200 mb-6 text-center">Login to Console</h2>
          
          {error && (
            <div className="flex items-center gap-2.5 p-3.5 bg-red-500/10 border border-red-500/20 text-red-400 rounded-xl text-xs mb-5">
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-4">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-slate-400" htmlFor="username">Username</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                  <User size={16} />
                </span>
                <input
                  id="username"
                  type="text"
                  required
                  placeholder="Enter username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="w-full bg-slate-950/60 border border-cardBorder rounded-xl py-3 pl-10 pr-4 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-brandBlue focus:ring-1 focus:ring-brandBlue/30 transition-all"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-slate-400" htmlFor="password">Password</label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                  <Lock size={16} />
                </span>
                <input
                  id="password"
                  type="password"
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full bg-slate-950/60 border border-cardBorder rounded-xl py-3 pl-10 pr-4 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-brandBlue focus:ring-1 focus:ring-brandBlue/30 transition-all"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-brandBlue hover:bg-blue-600 text-white font-semibold py-3 px-4 rounded-xl shadow-lg shadow-brandBlue/20 transition-all flex items-center justify-center gap-2 mt-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? "Logging in..." : "Login"}
            </button>
          </form>

          <p className="text-center text-xs text-slate-400 mt-6">
            Don't have an account?{" "}
            <Link to="/register" className="text-brandBlue hover:underline font-semibold">
              Register here
            </Link>
          </p>
        </div>
        
        {/* Quick Demo Info */}
        <div className="glass-panel p-4 text-xs text-slate-400/90 border-dashed border-cardBorder flex flex-col gap-1 shadow-md">
          <p className="font-bold text-slate-300 font-outfit text-center mb-1">Demo Credentials Available:</p>
          <div className="grid grid-cols-3 gap-2 text-center">
            <div>
              <p className="font-semibold text-red-400">Admin</p>
              <p>admin / admin123</p>
            </div>
            <div>
              <p className="font-semibold text-blue-400">Operator</p>
              <p>operator / operator123</p>
            </div>
            <div>
              <p className="font-semibold text-green-400">Technician</p>
              <p>technician / technician123</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
