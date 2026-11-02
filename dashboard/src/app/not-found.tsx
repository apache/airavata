import Link from "next/link";

export default function NotFound() {
  return (
    <div className="stack">
      <h1>Page not found</h1>
      <p className="muted">
        <Link href="/">Back to the overview</Link>
      </p>
    </div>
  );
}
