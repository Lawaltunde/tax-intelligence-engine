import { useNavigate } from 'react-router-dom';

export default function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <div className="not-found">
      <div className="not-found-code" aria-hidden="true">404</div>
      <h1 className="not-found-title">Page not found</h1>
      <p className="not-found-desc">
        The page you're looking for doesn't exist or has been moved.
      </p>
      <button className="btn-primary" onClick={() => navigate('/')}>
        ← Back to Dashboard
      </button>
    </div>
  );
}
