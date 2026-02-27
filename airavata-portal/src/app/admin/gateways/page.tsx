"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Building2, Plus, ArrowRight, Info, Activity, Server, Database, AppWindow } from "lucide-react";
import { Button } from "@/components/ui/button";
import { SearchBar } from "@/components/ui/search-bar";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { useQuery } from "@tanstack/react-query";
import { gatewaysApi } from "@/lib/api/gateways";
import { statisticsApi } from "@/lib/api/statistics";
import { useGateway } from "@/contexts/GatewayContext";
import { usePortalConfig } from "@/contexts/PortalConfigContext";
import { GatewayModal } from "@/components/gateways/GatewayModal";

export default function GatewaysPage() {
  const router = useRouter();
  const { needsFirstGateway, isLoading: gatewaysLoading } = useGateway();
  const { appVersion } = usePortalConfig();
  const [searchTerm, setSearchTerm] = useState("");
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  const { data: gateways = [], isLoading } = useQuery({
    queryKey: ["gateways"],
    queryFn: () => gatewaysApi.list(),
  });

  const { data: systemStats, isLoading: isLoadingStats } = useQuery({
    queryKey: ["system-statistics"],
    queryFn: () => statisticsApi.getSystemStatistics(),
  });

  useEffect(() => {
    if (!gatewaysLoading && needsFirstGateway) {
      router.replace("/onboarding/create-gateway");
    }
  }, [gatewaysLoading, needsFirstGateway, router]);

  const filteredGateways = gateways.filter(
    (gateway) =>
      gateway.gatewayName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      gateway.gatewayId.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (needsFirstGateway) {
    return null;
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary/10 rounded-lg">
            <Building2 className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Gateways</h1>
            <p className="text-muted-foreground">
              Manage gateway configurations
            </p>
          </div>
        </div>
        <Button onClick={() => setIsAddModalOpen(true)}>
          <Plus className="mr-2 h-4 w-4" />
          Add Gateway
        </Button>
      </div>

      <SearchBar
        placeholder="Search gateways..."
        value={searchTerm}
        onChange={setSearchTerm}
      />

      <GatewayModal open={isAddModalOpen} onOpenChange={setIsAddModalOpen} />

      {isLoading ? (
        <Skeleton className="h-64 w-full" />
      ) : filteredGateways.length === 0 ? (
        <Card>
          <CardContent className="py-16 text-center">
            <Building2 className="h-16 w-16 mx-auto text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold">No gateways available</h3>
            <p className="text-muted-foreground mt-1">
              {searchTerm ? "No gateways found" : "Create your first gateway to get started"}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="border rounded-lg overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="h-9 px-3">Name</TableHead>
                <TableHead className="h-9 px-3">Gateway ID</TableHead>
                <TableHead className="h-9 px-3 w-10"></TableHead>
                <TableHead className="h-9 px-3 w-10"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredGateways.map((gateway) => (
                <TableRow key={gateway.gatewayId}>
                  <TableCell className="py-1.5 px-3">
                      <span className="font-medium text-sm">{gateway.gatewayName || gateway.gatewayId}</span>
                  </TableCell>
                  <TableCell className="py-1.5 px-3 text-sm text-muted-foreground">
                    {gateway.gatewayId}
                  </TableCell>
                  <TableCell className="py-1.5 px-3">
                    {gateway.gatewayPublicAbstract ? (
                      <Popover>
                        <PopoverTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-7 w-7">
                            <Info className="h-4 w-4 text-muted-foreground" />
                          </Button>
                        </PopoverTrigger>
                        <PopoverContent side="top" className="text-sm">
                          {gateway.gatewayPublicAbstract}
                        </PopoverContent>
                      </Popover>
                    ) : null}
                  </TableCell>
                  <TableCell className="py-1.5 px-3">
                    <Button variant="ghost" size="sm" asChild>
                      <Link href={`/${gateway.gatewayId}/admin/gateway`}>
                        <ArrowRight className="h-4 w-4" />
                      </Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Airavata Services Status */}
      <Card>
        <CardHeader>
          <CardTitle>Airavata Services</CardTitle>
          <CardDescription>Status and versions of core services</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="border rounded-lg overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="h-9 px-3">Service</TableHead>
                  <TableHead className="h-9 px-3">Status</TableHead>
                  <TableHead className="h-9 px-3">Version</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow>
                  <TableCell className="py-1.5 px-3 font-medium">API</TableCell>
                  <TableCell className="py-1.5 px-3">
                    <div className="flex items-center gap-2">
                      <span className="inline-block h-2.5 w-2.5 rounded-sm bg-primary shrink-0" title="Operational" />
                      <span className="text-sm">Operational</span>
                    </div>
                  </TableCell>
                  <TableCell className="py-1.5 px-3 text-muted-foreground">&mdash;</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="py-1.5 px-3 font-medium">IAM</TableCell>
                  <TableCell className="py-1.5 px-3">
                    <div className="flex items-center gap-2">
                      <span className="inline-block h-2.5 w-2.5 rounded-sm bg-primary shrink-0" title="Operational" />
                      <span className="text-sm">Operational</span>
                    </div>
                  </TableCell>
                  <TableCell className="py-1.5 px-3 text-muted-foreground">&mdash;</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="py-1.5 px-3 font-medium">State Store</TableCell>
                  <TableCell className="py-1.5 px-3">
                    <div className="flex items-center gap-2">
                      <span className="inline-block h-2.5 w-2.5 rounded-sm bg-primary shrink-0" title="Operational" />
                      <span className="text-sm">Operational</span>
                    </div>
                  </TableCell>
                  <TableCell className="py-1.5 px-3 text-muted-foreground">&mdash;</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="py-1.5 px-3 font-medium">Dashboard</TableCell>
                  <TableCell className="py-1.5 px-3">
                    <div className="flex items-center gap-2">
                      <span className="inline-block h-2.5 w-2.5 rounded-sm bg-green-500 shrink-0" title="Running" />
                      <span className="text-sm">Running</span>
                    </div>
                  </TableCell>
                  <TableCell className="py-1.5 px-3 font-mono text-sm">{appVersion || "0.1.0"}</TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      {/* System Resource Counts */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Gateways</CardTitle>
            <Activity className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {isLoadingStats ? (
              <Skeleton className="h-8 w-12" />
            ) : (
              <div className="text-2xl font-bold">{systemStats?.totalGateways || 0}</div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Compute Resources</CardTitle>
            <Server className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {isLoadingStats ? (
              <Skeleton className="h-8 w-12" />
            ) : (
              <div className="text-2xl font-bold">{systemStats?.totalComputeResources || 0}</div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Storage Resources</CardTitle>
            <Database className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {isLoadingStats ? (
              <Skeleton className="h-8 w-12" />
            ) : (
              <div className="text-2xl font-bold">{systemStats?.totalStorageResources || 0}</div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Applications</CardTitle>
            <AppWindow className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {isLoadingStats ? (
              <Skeleton className="h-8 w-12" />
            ) : (
              <div className="text-2xl font-bold">{systemStats?.totalApplications || 0}</div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
