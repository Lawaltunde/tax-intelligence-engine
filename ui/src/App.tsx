import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
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
            </Route>
          </Routes>
        </ErrorBoundary>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
