// Icons for the navigation rail, drawn inline so the dashboard carries no icon
// dependency and every glyph follows the text colour (stroke="currentColor").

// The Apache Airavata mark: a blue and a coral block, three cells each, wrapped around
// an empty centre. Every cell is split along one diagonal into two facets. Colours are
// those of the project's published logo.
type Facets = [x: number, y: number, diagonal: "\\" | "/", a: string, b: string];

// For "\" (top-left to bottom-right) a is the upper-right facet and b the lower-left;
// for "/" (top-right to bottom-left) a is the upper-left facet and b the lower-right.
const MARK: Facets[] = [
  [0, 1, "\\", "#8fd5e5", "#5dcbee"],
  [0, 2, "/", "#42c3f1", "#2bb8df"],
  [1, 2, "\\", "#1ca2b8", "#1aadcc"],
  [1, 0, "\\", "#f7b9b0", "#f4c4c0"],
  [2, 0, "/", "#f8ab9d", "#f58b7d"],
  [2, 1, "\\", "#f37b72", "#ef6a67"],
];

export function AiravataMark({ size = 28 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 3 3" aria-hidden="true" shapeRendering="crispEdges">
      {MARK.map(([x, y, d, a, b]) => {
        const tl = `${x},${y}`;
        const tr = `${x + 1},${y}`;
        const bl = `${x},${y + 1}`;
        const br = `${x + 1},${y + 1}`;
        const [first, second] = d === "\\" ? [[tl, tr, br], [tl, bl, br]] : [[tl, tr, bl], [tr, br, bl]];
        return (
          <g key={`${x}${y}`}>
            <polygon points={first.join(" ")} fill={a} />
            <polygon points={second.join(" ")} fill={b} />
          </g>
        );
      })}
    </svg>
  );
}

export type IconName =
  | "overview"
  | "key"
  | "server"
  | "database"
  | "app"
  | "activity"
  | "file"
  | "users"
  | "notebook"
  | "shield"
  | "user"
  | "bot"
  | "logout";

// 24×24 line drawings, 1.75 stroke.
const PATHS: Record<IconName, React.ReactNode> = {
  overview: (
    <>
      <rect x="3" y="3" width="7" height="9" rx="1.5" />
      <rect x="14" y="3" width="7" height="5" rx="1.5" />
      <rect x="14" y="12" width="7" height="9" rx="1.5" />
      <rect x="3" y="16" width="7" height="5" rx="1.5" />
    </>
  ),
  key: (
    <>
      <circle cx="7.5" cy="15.5" r="4.5" />
      <path d="M10.7 12.3 20 3M16 7l3 3M14 9l2 2" />
    </>
  ),
  server: (
    <>
      <rect x="3" y="3" width="18" height="7" rx="2" />
      <rect x="3" y="14" width="18" height="7" rx="2" />
      <path d="M7 6.5h.01M7 17.5h.01M11 6.5h6M11 17.5h6" />
    </>
  ),
  database: (
    <>
      <ellipse cx="12" cy="5" rx="8" ry="3" />
      <path d="M4 5v14c0 1.7 3.6 3 8 3s8-1.3 8-3V5" />
      <path d="M4 12c0 1.7 3.6 3 8 3s8-1.3 8-3" />
    </>
  ),
  app: (
    <>
      <path d="M12 2 3 7v10l9 5 9-5V7z" />
      <path d="M3 7l9 5 9-5M12 12v10" />
    </>
  ),
  activity: <path d="M22 12h-4l-3 9L9 3l-3 9H2" />,
  file: (
    <>
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <path d="M14 2v6h6M8 13h8M8 17h5" />
    </>
  ),
  users: (
    <>
      <circle cx="9" cy="8" r="4" />
      <path d="M2 21v-1a6 6 0 0 1 6-6h2a6 6 0 0 1 6 6v1" />
      <path d="M16 3.1a4 4 0 0 1 0 7.8M22 21v-1a6 6 0 0 0-4-5.7" />
    </>
  ),
  notebook: (
    <>
      <rect x="4" y="2" width="16" height="20" rx="2" />
      <path d="M8 2v20M12 7h4M12 11h4" />
    </>
  ),
  shield: (
    <>
      <path d="M12 2 4 5v6c0 5 3.4 9.3 8 11 4.6-1.7 8-6 8-11V5z" />
      <path d="m9 12 2 2 4-4" />
    </>
  ),
  user: (
    <>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1" />
    </>
  ),
  bot: (
    <>
      <rect x="4" y="8" width="16" height="12" rx="2" />
      <path d="M12 8V4M8 4h8M9 13v2M15 13v2M2 14h2M20 14h2" />
    </>
  ),
  logout: <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" />,
};

export function Icon({ name, size = 20 }: { name: IconName; size?: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.75}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {PATHS[name]}
    </svg>
  );
}
