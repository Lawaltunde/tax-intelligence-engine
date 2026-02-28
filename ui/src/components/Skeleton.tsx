/**
 * Skeleton — animated placeholder shown while data is loading.
 *
 * Usage:
 *   <Skeleton />                  — single line, full width
 *   <Skeleton width="60%" />      — shorter line
 *   <Skeleton height={120} />     — taller block (e.g. card placeholder)
 *   <Skeleton count={4} gap={10}/>— stacked lines
 */
interface SkeletonProps {
  width?:  string | number;
  height?: string | number;
  count?:  number;
  gap?:    number;
  style?:  React.CSSProperties;
}

export function Skeleton({ width = '100%', height = 16, count = 1, gap = 8, style }: SkeletonProps) {
  const items = Array.from({ length: count });
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap }}>
      {items.map((_, i) => (
        <span
          key={i}
          className="skeleton"
          style={{ width, height, display: 'block', ...style }}
          aria-hidden="true"
        />
      ))}
    </div>
  );
}

/** Pre-built skeleton that matches a result card's typical height. */
export function CardSkeleton() {
  return (
    <div className="card" aria-busy="true" aria-label="Loading result…">
      <Skeleton width="40%" height={20} />
      <div style={{ marginTop: 16 }}>
        <Skeleton count={5} gap={10} />
      </div>
      <div style={{ marginTop: 16 }}>
        <Skeleton width="30%" height={14} />
        <Skeleton count={3} gap={10} style={{ marginTop: 8 }} />
      </div>
    </div>
  );
}
