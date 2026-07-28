import React from "react";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./routes/AppRoutes";

const getBasename = () => {
  return import.meta.env.DEV ? "/" : "/GridPulse";
};

function App() {
  return (
    <BrowserRouter basename={getBasename()}>
      <AppRoutes />
    </BrowserRouter>
  );
}

export default App;
