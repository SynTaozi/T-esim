import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import fs from 'fs'

/** 让 /page-style-previews 走 public 静态 HTML，避免被 Vue SPA 回退到首页 */
function pageStylePreviewsPlugin() {
  const previewsRoot = path.resolve(__dirname, 'public/page-style-previews')
  return {
    name: 'page-style-previews-static',
    configureServer(server: any) {
      server.middlewares.use((req: any, res: any, next: any) => {
        const raw = req.url?.split('?')[0] || ''
        if (!raw.startsWith('/page-style-previews')) {
          return next()
        }
        let rel = raw.replace(/^\/page-style-previews\/?/, '')
        if (!rel || rel.endsWith('/')) {
          rel = `${rel}index.html`
        }
        const filePath = path.resolve(previewsRoot, rel)
        if (!filePath.startsWith(previewsRoot) || !fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
          return next()
        }
        res.setHeader('Content-Type', 'text/html; charset=utf-8')
        fs.createReadStream(filePath).pipe(res)
      })
    }
  }
}

export default defineConfig({
  plugins: [vue(), pageStylePreviewsPlugin()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
