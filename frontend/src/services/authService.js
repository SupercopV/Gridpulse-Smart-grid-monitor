import API from "./api";

const authService = {
  login: async (username, password) => {
    const response = await API.post("/auth/login", { username, password });
    if (response.data.token) {
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("user", JSON.stringify({
        userId: response.data.userId,
        username: response.data.username,
        fullName: response.data.fullName,
        role: response.data.role,
      }));
    }
    return response.data;
  },

  register: async (username, password, email, role, fullName) => {
    const response = await API.post("/auth/register", {
      username,
      password,
      email,
      role,
      fullName,
    });
    return response.data;
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  },

  getCurrentUser: () => {
    const userStr = localStorage.getItem("user");
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (e) {
        return null;
      }
    }
    return null;
  },

  isAuthenticated: () => {
    return !!localStorage.getItem("token");
  },

  getToken: () => {
    return localStorage.getItem("token");
  }
};

export default authService;
