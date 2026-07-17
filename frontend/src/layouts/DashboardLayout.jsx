import React, { useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { 
  LayoutDashboard, Activity, Map, AlertTriangle, 
  Wrench, Users, UserCheck, BarChart3, LogOut, Menu, X, BatteryCharging
} from "lucide-react";
import authService from "../services/authService";

const DashboardLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const user = authService.getCurrentUser();

  const handleLogout = () => {
    authService.logout();
    navigate("/login");
  };

  const navItems = [
    { name: "Dashboard", path: "/", icon: LayoutDashboard, roles: ["ADMIN", "OPERATOR", "TECHNICIAN"] },
    { name: "Live Monitoring", path: "/monitoring", icon: Activity, roles: ["ADMIN", "OPERATOR", "TECHNICIAN"] },
    { name: "Grid Heatmap", path: "/heatmap", icon: Map, roles: ["ADMIN", "OPERATOR", "TECHNICIAN"] },
    { name: "Active Alerts", path: "/alerts", icon: AlertTriangle, roles: ["ADMIN", "OPERATOR"] },
    { name: "Repair Tickets", path: "/tickets", icon: Wrench, roles: ["ADMIN", "OPERATOR", "TECHNICIAN"] },
    { name: "Technicians", path: "/technicians", icon: UserCheck, roles: ["ADMIN"] },
    { name: "Customers", path: "/customers", icon: Users, roles: ["ADMIN", "OPERATOR"] },
    { name: "Reports", path: "/reports", icon: BarChart3, roles: ["ADMIN", "OPERATOR"] },
  ];

  const filteredNavItems = navItems.filter(item => item.roles.includes(user?.role));

  const roleColors = {
    ADMIN: "bg-red-500/10 text-red-400 border border-red-500/20",
    OPERATOR: "bg-blue-500/10 text-blue-400 border border-blue-500/20",
    TECHNICIAN: "bg-green-500/10 text-green-400 border border-green-500/20",
  };

  return (
    <div className="min-h-screen bg-darkBg text-slate-100 flex">
      {/* Sidebar for Desktop */}
      <aside className={`fixed inset-y-0 left-0 z-50 w-64 bg-cardBg/90 backdrop-blur-md border-r border-cardBorder/60 p-5 flex flex-col transform transition-transform duration-300 md:translate-x-0 md:static md:h-screen ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <div className="bg-brandBlue/10 p-2 rounded-xl border border-brandBlue/20 text-brandBlue animate-pulse">
              <BatteryCharging size={24} />
            </div>
            <span className="font-outfit text-xl font-bold tracking-wide bg-gradient-to-r from-blue-400 via-indigo-400 to-emerald-400 bg-clip-text text-transparent">
              GridPulse
            </span>
          </div>
          <button className="md:hidden text-slate-400 hover:text-white" onClick={() => setSidebarOpen(false)}>
            <X size={20} />
          </button>
        </div>

        <nav className="flex-1 space-y-1.5 overflow-y-auto pr-1">
          {filteredNavItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.name}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                className={`flex items-center gap-3.5 px-4 py-3 rounded-xl transition-all duration-200 ${
                  isActive 
                    ? "bg-brandBlue text-white font-medium shadow-md shadow-brandBlue/10" 
                    : "text-slate-400 hover:bg-slate-800/40 hover:text-slate-100"
                }`}
              >
                <Icon size={18} />
                <span className="text-sm">{item.name}</span>
              </Link>
            );
          })}
        </nav>

        <div className="mt-auto border-t border-cardBorder/60 pt-4 flex flex-col gap-3">
          <div className="flex items-center gap-3 px-2">
            <div className="w-10 h-10 rounded-full bg-slate-800 border border-slate-700/80 flex items-center justify-center font-bold text-slate-300 font-outfit uppercase">
              {user?.fullName?.charAt(0) || "U"}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold truncate text-slate-200">{user?.fullName}</p>
              <span className={`inline-block text-[10px] px-2 py-0.5 mt-0.5 rounded-full font-bold uppercase ${roleColors[user?.role] || "bg-slate-800 text-slate-400"}`}>
                {user?.role}
              </span>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2.5 px-4 py-3 rounded-xl border border-red-500/20 bg-red-500/5 hover:bg-red-500/10 text-red-400 text-sm font-semibold transition-all duration-200"
          >
            <LogOut size={16} />
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 h-screen overflow-hidden">
        {/* Header for Mobile */}
        <header className="bg-cardBg/50 backdrop-blur-md border-b border-cardBorder/40 px-6 py-4 flex items-center justify-between md:justify-end">
          <button className="md:hidden text-slate-400 hover:text-white" onClick={() => setSidebarOpen(true)}>
            <Menu size={24} />
          </button>
          
          <div className="flex items-center gap-3">
            <div className="text-right hidden md:block">
              <p className="text-xs text-slate-400">Current Grid Monitor</p>
              <p className="text-sm font-semibold text-brandBlue font-outfit">Active Status: Monitoring</p>
            </div>
          </div>
        </header>

        {/* Scrollable Page Outlet */}
        <main className="flex-1 overflow-y-auto p-6 md:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout;
