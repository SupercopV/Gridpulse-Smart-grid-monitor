import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import authService from "../services/authService";
import DashboardLayout from "../layouts/DashboardLayout";
import Login from "../pages/Login";
import Register from "../pages/Register";
import Dashboard from "../pages/Dashboard";
import LiveMonitor from "../pages/LiveMonitor";
import GridHeatmap from "../pages/GridHeatmap";
import AlertsPage from "../pages/AlertsPage";
import RepairTickets from "../pages/RepairTickets";
import Technicians from "../pages/Technicians";
import Customers from "../pages/Customers";
import Reports from "../pages/Reports";

// Helper components for guarding routes
const ProtectedRoute = ({ children }) => {
  const isAuth = authService.isAuthenticated();
  return isAuth ? children : <Navigate to="/login" replace />;
};

const RoleRoute = ({ children, allowedRoles }) => {
  const user = authService.getCurrentUser();
  if (!user) return <Navigate to="/login" replace />;
  
  const hasAccess = allowedRoles.includes(user.role);
  return hasAccess ? children : <Navigate to="/" replace />;
};

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Auth Routes */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Protected Dashboard Routes */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="monitoring" element={<LiveMonitor />} />
        <Route path="heatmap" element={<GridHeatmap />} />
        
        {/* Alerts Page - accessible by Admin and Operator */}
        <Route 
          path="alerts" 
          element={
            <RoleRoute allowedRoles={["ADMIN", "OPERATOR"]}>
              <AlertsPage />
            </RoleRoute>
          } 
        />

        {/* Repair Tickets - accessible by all but different actions (Admin manages, Operator requests, Tech completes) */}
        <Route path="tickets" element={<RepairTickets />} />

        {/* Technicians CRUD - Admin only */}
        <Route 
          path="technicians" 
          element={
            <RoleRoute allowedRoles={["ADMIN"]}>
              <Technicians />
            </RoleRoute>
          } 
        />

        {/* Customers CRUD - Admin / Operator */}
        <Route 
          path="customers" 
          element={
            <RoleRoute allowedRoles={["ADMIN", "OPERATOR"]}>
              <Customers />
            </RoleRoute>
          } 
        />

        {/* Reports - Admin / Operator */}
        <Route 
          path="reports" 
          element={
            <RoleRoute allowedRoles={["ADMIN", "OPERATOR"]}>
              <Reports />
            </RoleRoute>
          } 
        />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
export { ProtectedRoute, RoleRoute };
