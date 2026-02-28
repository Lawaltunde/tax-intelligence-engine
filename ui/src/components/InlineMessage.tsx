import type { ReactNode } from 'react';

// SVG icons — aria-hidden so screen readers skip them; the role on the
// container + visually-hidden prefix text carry the semantic weight instead.

function WarningIcon() {
  return (
    <svg
      aria-hidden="true"
      focusable="false"
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      style={{ flexShrink: 0, marginTop: '1px' }}
    >
      <path
        d="M8 1.333 1.333 13.333h13.334L8 1.333Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinejoin="round"
        fill="none"
      />
      <path d="M8 6v3.333" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <circle cx="8" cy="11.333" r="0.667" fill="currentColor" />
    </svg>
  );
}

function CheckIcon() {
  return (
    <svg
      aria-hidden="true"
      focusable="false"
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      style={{ flexShrink: 0 }}
    >
      <path
        d="M2.667 8.667 6 12l7.333-8"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

interface InlineMessageProps {
  variant: 'error' | 'success';
  children: ReactNode;
  /** Additional class names forwarded to the container */
  className?: string;
}

/**
 * Accessible inline status message.
 *
 * - `role="alert"` (error) / `role="status"` (success) announces to AT
 * - SVG icon is `aria-hidden` — purely decorative
 * - A visually-hidden prefix ("Error:" / "Success:") gives screen-reader
 *   users context before the message text is read
 */
export default function InlineMessage({ variant, children, className }: InlineMessageProps) {
  const isError = variant === 'error';

  return (
    <p
      className={[variant, className].filter(Boolean).join(' ')}
      role={isError ? 'alert' : 'status'}
    >
      {isError ? <WarningIcon /> : <CheckIcon />}
      {/* Visually-hidden prefix for screen readers */}
      <span className="sr-only">{isError ? 'Error:' : 'Success:'}</span>
      {children}
    </p>
  );
}
