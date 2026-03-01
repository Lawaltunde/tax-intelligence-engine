import { Component, type ErrorInfo, type ReactNode } from 'react';

interface Props  { children: ReactNode; fallback?: ReactNode; }
interface State  { error: Error | null; }

/**
 * Class-based error boundary — catches render-time exceptions in any child
 * subtree and renders a friendly fallback instead of a blank/broken screen.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('[ErrorBoundary]', error, info.componentStack);
  }

  handleReset = () => this.setState({ error: null });

  render() {
    if (this.state.error) {
      if (this.props.fallback) return this.props.fallback;
      return (
        <div className="error-boundary" role="alert">
          <span className="error-boundary-icon" aria-hidden="true">⚠️</span>
          <h2>Something went wrong</h2>
          <p className="error-boundary-msg">
            {import.meta.env.DEV
              ? this.state.error.message
              : 'An unexpected error occurred. Please try again.'}
          </p>
          <button className="btn-primary" onClick={this.handleReset}>
            Try again
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
