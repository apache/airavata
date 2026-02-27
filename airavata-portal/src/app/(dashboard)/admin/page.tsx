"use client";

import Link from "next/link";
import { Server, Building2, Users, Settings } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

const adminSections = [
  {
    title: "Resources",
    description: "Manage compute and storage resources",
    href: "/admin/resources",
    icon: Server,
    color: "text-blue-600",
    bgColor: "bg-blue-100",
  },
  {
    title: "Gateways",
    description: "Manage gateway configurations",
    href: "/admin/gateways",
    icon: Building2,
    color: "text-orange-600",
    bgColor: "bg-orange-100",
  },
  {
    title: "Users",
    description: "Manage user accounts and permissions",
    href: "/admin/users",
    icon: Users,
    color: "text-indigo-600",
    bgColor: "bg-indigo-100",
  },
  {
    title: "Settings",
    description: "Configure system settings",
    href: "/admin/settings",
    icon: Settings,
    color: "text-gray-600",
    bgColor: "bg-gray-100",
  },
];

export default function AdminPage() {
  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Administration</h1>
        <p className="text-muted-foreground">
          Manage resources, applications, and system settings
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {adminSections.map((section) => (
          <Link key={section.href} href={section.href}>
            <Card className="transition-shadow hover:shadow-md cursor-pointer h-full">
              <CardHeader>
                <div className="flex items-center gap-3">
                  <div className={`p-2 rounded-lg ${section.bgColor}`}>
                    <section.icon className={`h-6 w-6 ${section.color}`} />
                  </div>
                  <div>
                    <CardTitle className="text-lg">{section.title}</CardTitle>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <CardDescription>{section.description}</CardDescription>
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
