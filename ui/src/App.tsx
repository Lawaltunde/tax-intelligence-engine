import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { ErrorBoundary }    from './components/ErrorBoundary';
import Layout               from './components/Layout';
import DashboardPage        from './pages/DashboardPage';
import DocumentUploadPage   from './pages/DocumentUploadPage';
import CraImportPage        from './pages/CraImportPage';
import ReconciliationPage   from './pages/ReconciliationPage';
import OptimizationPage     from './pages/OptimizationPage';
import RiskAssessmentPage   from './pages/RiskAssessmentPage';
import ProvincialPolicyPage from './pages/ProvincialPolicyPage';
import ReviewPage           from './pages/ReviewPage';
import NotFoundPage         from './pages/NotFoundPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,       // 30 s — data is fresh for 30 s after fetching
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,                // never auto-retry mutations (user must resubmit)
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ErrorBoundary>
          <Routes>
            <Route element={<Layout />}>
              <Route index           element={<DashboardPage />} />
              <Route path="upload"     element={<DocumentUploadPage />} />
              <Route path="cra"        element={<CraImportPage />} />
              <Route path="reconcile"  element={<ReconciliationPage />} />
              <Route path="optimize"   element={<OptimizationPage />} />
              <Route path="risk"       element={<RiskAssessmentPage />} />
              <Route path="provincial" element={<ProvincialPolicyPage />} />
              <Route path="review"     element={<ReviewPage />} />
              <Route path="*"          element={<NotFoundPage />} />
            </Route>
          </Routes>
        </ErrorBoundary>
      </BrowserRouter>
      <Toaster
        position="bottom-right"
        toastOptions={{
          duration: 4000,
          style: {
            fontFamily: 'Inter, system-ui, sans-serif',
            fontSize: '0.875rem',
            borderRadius: '8px',
            boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
          },
          success: {
            iconTheme: { primary: '#10b981', secondary: '#fff' },
          },
          error: {
            iconTheme: { primary: '#ef4444', secondary: '#fff' },
            duration: 6000,
          },
        }}
      />
    </QueryClientProvider>
  );
}
