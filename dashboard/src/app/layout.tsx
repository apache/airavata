import type { Metadata } from "next";
import { Shell } from "@/components/Shell";
import { InteractiveAuthProvider } from "@/lib/interactive-auth";
import { SessionProvider } from "@/lib/session";
import "./globals.css";

export const metadata: Metadata = {
  title: "Airavata Dashboard",
  description: "Manage compute, storage, applications and processes on Apache Airavata",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <SessionProvider>
          <InteractiveAuthProvider>
            <Shell>{children}</Shell>
          </InteractiveAuthProvider>
        </SessionProvider>
      </body>
    </html>
  );
}
