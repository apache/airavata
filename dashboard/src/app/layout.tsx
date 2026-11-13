import type { Metadata } from "next";
import { Shell } from "@/components/Shell";
import { InteractiveAuthProvider } from "@/lib/interactive-auth";
import { BrandProvider } from "@/lib/branding";
import { SessionProvider } from "@/lib/session";
import "./globals.css";

export const metadata: Metadata = {
  title: `${process.env.NEXT_PUBLIC_AIRAVATA_BRAND_NAME || "Airavata"} Dashboard`,
  description: "Manage compute, storage, applications and processes on Apache Airavata",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <BrandProvider>
          <SessionProvider>
            <InteractiveAuthProvider>
              <Shell>{children}</Shell>
            </InteractiveAuthProvider>
          </SessionProvider>
        </BrandProvider>
      </body>
    </html>
  );
}
