import React from "react";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./routes/AppRoutes";

const getBasename = () => {
  if (import.meta.env.DEV) {
    return "/";
  }
  const path = window.location.pathname;
  if (path.toLowerCase().startsWith("/gridpulse")) {
    return "/GridPulse";
  }
  return "/";
};

function App() {
  return (
    <BrowserRouter basename={getBasename()}>
      <AppRoutes />
    </BrowserRouter>
  );
}

export default App;
