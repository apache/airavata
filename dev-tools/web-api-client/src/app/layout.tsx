import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Airavata API Console",
  description: "Invoke Airavata REST APIs using a CILogon-authenticated session",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
