import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const backendProxy = {
  target: 'http://localhost:5000',
  changeOrigin: true,
  xfwd: true,
}

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    strictPort: true,
    proxy: {
      '/api': backendProxy,
      '/oauth2': backendProxy,
      '/login': backendProxy,
      '/logout': backendProxy,
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test-setup.js',
  },
})
