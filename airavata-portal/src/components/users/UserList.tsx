"use client";

import Link from "next/link";
import { Eye, CheckCircle, XCircle, MailCheck, Mail, Users } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
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
import { GatewayBadge } from "@/components/gateway/GatewayBadge";
import { formatDate } from "@/lib/utils";
import { useGateway } from "@/contexts/GatewayContext";
import type { User } from "@/lib/api/users";

interface Props {
  users?: User[];
  isLoading: boolean;
}

export function UserList({ users, isLoading }: Props) {
  const { selectedGatewayId, getGatewayName } = useGateway();
  const gatewayName = selectedGatewayId ? getGatewayName(selectedGatewayId) : "default";

  if (isLoading) {
    return <Skeleton className="h-64 w-full" />;
  }

  if (!users || users.length === 0) {
    return (
      <Card>
        <CardContent className="py-16 text-center">
          <Users className="h-12 w-12 mx-auto text-muted-foreground/50 mb-4" />
          <p className="text-muted-foreground">No users found</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="border rounded-lg overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="h-9 px-3">Name</TableHead>
            <TableHead className="h-9 px-3">Username</TableHead>
            <TableHead className="h-9 px-3">Email</TableHead>
            <TableHead className="h-9 px-3">Gateway</TableHead>
            <TableHead className="h-9 px-3">Status</TableHead>
            <TableHead className="h-9 px-3">Joined</TableHead>
            <TableHead className="h-9 px-3 w-10"></TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {users.map((user) => (
            <TableRow key={user.airavataInternalUserId}>
              <TableCell className="py-1.5 px-3">
                <span className="font-medium text-sm">
                  {user.firstName} {user.lastName}
                </span>
              </TableCell>
              <TableCell className="py-1.5 px-3 text-sm text-muted-foreground">
                {user.userId}
              </TableCell>
              <TableCell className="py-1.5 px-3">
                <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
                  {user.emailVerified ? (
                    <MailCheck className="h-3.5 w-3.5 text-green-600 shrink-0" />
                  ) : (
                    <Mail className="h-3.5 w-3.5 text-muted-foreground shrink-0" />
                  )}
                  {user.email}
                </div>
              </TableCell>
              <TableCell className="py-1.5 px-3">
                {user.gatewayId && <GatewayBadge gatewayId={user.gatewayId} />}
              </TableCell>
              <TableCell className="py-1.5 px-3">
                {user.enabled ? (
                  <Badge variant="outline" className="bg-emerald-50 text-emerald-700 border-emerald-200 text-xs gap-1">
                    <CheckCircle className="h-3 w-3" />
                    Active
                  </Badge>
                ) : (
                  <Badge variant="outline" className="bg-gray-50 text-gray-600 border-gray-200 text-xs gap-1">
                    <XCircle className="h-3 w-3" />
                    Disabled
                  </Badge>
                )}
              </TableCell>
              <TableCell className="py-1.5 px-3 text-sm text-muted-foreground">
                {user.createdTime ? formatDate(user.createdTime) : "-"}
              </TableCell>
              <TableCell className="py-1.5 px-3">
                <Button variant="ghost" size="icon" className="h-7 w-7" asChild>
                  <Link href={`/${gatewayName}/admin/users/${user.userId}`}>
                    <Eye className="h-4 w-4" />
                  </Link>
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
