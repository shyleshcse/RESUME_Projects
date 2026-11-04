import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// The React app runs on http://localhost:5173
// Any request starting with /api is forwarded to the Spring Boot backend on port 8080.
// This is why in the React code we can simply write api.get("/doctors").
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
});
