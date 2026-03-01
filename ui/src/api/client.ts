import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
});

// Convert HTTP error responses into plain Error objects with user-readable messages
// so every page's onError handler gets a sensible err.message.
api.interceptors.response.use(
  response => response,
  (error: unknown) => {
    // axios wraps HTTP errors — check for the response shape directly
    if (
      error != null &&
      typeof error === 'object' &&
      'response' in error &&
      error.response != null
    ) {
      const response = error.response as {
        status: number;
        data?: { message?: string; error?: string };
      };
      const status  = response.status;
      const body    = response.data ?? {};
      const serverMsg =
        (typeof body === 'object' && (body.message ?? body.error)) || null;

      const fallback: Record<number, string> = {
        400: 'Invalid request — please check your inputs.',
        401: 'You are not authorised to perform this action.',
        403: 'Access denied.',
        404: 'The requested resource was not found.',
        413: 'File is too large to upload.',
        415: 'Unsupported file type — only PDF tax slips are accepted.',
        422: 'The server could not process the submitted data.',
        500: 'An internal server error occurred. Please try again.',
        503: 'The service is temporarily unavailable.',
      };

      const message = serverMsg || fallback[status] || `Server error (${status}).`;
      return Promise.reject(new Error(message));
    }

    // Request left the browser but no response arrived (server down, CORS, etc.)
    if (
      error != null &&
      typeof error === 'object' &&
      'request' in error
    ) {
      return Promise.reject(
        new Error('Cannot reach the server. Please check your connection or try again shortly.')
      );
    }

    return Promise.reject(error);
  }
);

export default api;
