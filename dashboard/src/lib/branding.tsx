"use client";

import { createContext, useContext, useEffect, useState } from "react";

// Branding: the name shown beside the logo and the logo image itself.
//
// Two layers, the second overriding the first:
//   - Build time: NEXT_PUBLIC_AIRAVATA_BRAND_NAME and NEXT_PUBLIC_AIRAVATA_BRAND_LOGO_URL.
//   - Deployment time: a branding.json beside the exported pages (dashboard/out/branding.json
//     for a stock build), read by the browser on load, so a deployment can be rebranded
//     without rebuilding. Fields: {"name": "...", "logoUrl": "..."}; either may be omitted.
//
// A logo URL that is absolute (http:, https:, data:) or starts with "/" is used as is; any
// other value is a file inside the dashboard, e.g. "logo.svg" is served at /dashboard/logo.svg.
// With no logo configured the Airavata mark is shown.

export interface Brand {
  name: string;
  logoUrl: string;
}

const BASE_PATH = "/dashboard";

export const DEFAULT_BRAND: Brand = {
  name: process.env.NEXT_PUBLIC_AIRAVATA_BRAND_NAME || "Airavata",
  logoUrl: resolveLogo(process.env.NEXT_PUBLIC_AIRAVATA_BRAND_LOGO_URL ?? ""),
};

function resolveLogo(url: string): string {
  const u = url.trim();
  if (!u || /^(https?:|data:|\/)/i.test(u)) return u;
  return `${BASE_PATH}/${u}`;
}

const BrandContext = createContext<Brand>(DEFAULT_BRAND);

export function BrandProvider({ children }: { children: React.ReactNode }) {
  const [brand, setBrand] = useState<Brand>(DEFAULT_BRAND);

  useEffect(() => {
    let cancelled = false;
    // A missing file answers with the dashboard's HTML 404 page, so anything that is not
    // JSON is ignored, as is a file that fails to parse.
    fetch(`${BASE_PATH}/branding.json`, { cache: "no-cache" })
      .then((r) => (r.ok && r.headers.get("content-type")?.includes("json") ? r.json() : null))
      .then((cfg: Partial<Brand> | null) => {
        if (cancelled || !cfg || typeof cfg !== "object") return;
        const name = typeof cfg.name === "string" && cfg.name.trim() ? cfg.name.trim() : undefined;
        const logoUrl = typeof cfg.logoUrl === "string" ? resolveLogo(cfg.logoUrl) : undefined;
        setBrand((b) => ({ name: name ?? b.name, logoUrl: logoUrl ?? b.logoUrl }));
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    document.title = `${brand.name} Dashboard`;
  }, [brand.name]);

  return <BrandContext.Provider value={brand}>{children}</BrandContext.Provider>;
}

export const useBrand = () => useContext(BrandContext);
