import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

const root = document.getElementById('root')!

try {
  createRoot(root).render(
    <StrictMode>
      <App />
    </StrictMode>,
  )
} catch (err) {
  console.error('[main] Fatal render error:', err)
  root.innerHTML = `
    <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;
                height:100vh;font-family:system-ui,sans-serif;color:#374151;gap:12px">
      <span style="font-size:2.5rem">⚠️</span>
      <h2 style="margin:0">Application failed to start</h2>
      <p style="margin:0;color:#6b7280;font-size:.9rem">
        ${import.meta.env.DEV && err instanceof Error ? err.message : 'Please refresh or contact support.'}
      </p>
      <button onclick="location.reload()"
        style="margin-top:8px;padding:8px 20px;background:#5046e5;color:#fff;
               border:none;border-radius:6px;cursor:pointer;font-size:.9rem">
        Reload
      </button>
    </div>`
}
