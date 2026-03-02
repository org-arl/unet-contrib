import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { viteSingleFile } from 'vite-plugin-singlefile'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools(), viteSingleFile(), tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  envPrefix: ['VITE_', 'FJAGE_'],
  server: {
    port: 3000,
    open: '/USBL.html',
  },
  build: {
    rollupOptions: {
      input: {
        app: './USBL.html',
      },
    },
  },
})
