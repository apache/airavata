"use client";

import { DashboardLayout } from "@/components/layout";
import { useRootAdminRouteGuard } from "@/lib/route-guards";

export default function RootAdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  // Guard ensures user is a root/admin user
  useRootAdminRouteGuard();

  return <DashboardLayout>{children}</DashboardLayout>;
}
