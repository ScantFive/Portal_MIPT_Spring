import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      }
    }
  },
  build: {
    outDir: path.resolve(__dirname, '../src/main/resources/static'),
    emptyOutDir: true,
  }
})
