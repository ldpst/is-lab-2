import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  base: './',
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080/is-lab-1',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (response) => {
            const cookies = response.headers['set-cookie']
            if (cookies) {
              response.headers['set-cookie'] = cookies.map((cookie) =>
                cookie.replace('Path=/is-lab-1', 'Path=/'),
              )
            }
          })
        },
      },
      '/updates': {
        target: 'ws://localhost:8080/is-lab-1',
        ws: true,
      },
    },
  },
})
