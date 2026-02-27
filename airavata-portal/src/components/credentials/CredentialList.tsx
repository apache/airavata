"use client";

import { useState } from "react";
import { Trash2, Key, Lock } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { GatewayBadge } from "@/components/gateway/GatewayBadge";
import { formatDate } from "@/lib/utils";
import type { CredentialSummary } from "@/lib/api/credentials";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

interface Props {
  credentials?: CredentialSummary[];
  isLoading: boolean;
  onDelete: (token: string) => void;
  isDeleting: boolean;
}

export function CredentialList({ credentials, isLoading, onDelete, isDeleting }: Props) {
  const [deleteToken, setDeleteToken] = useState<string | null>(null);

  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {[...Array(3)].map((_, i) => (
          <Skeleton key={i} className="h-32" />
        ))}
      </div>
    );
  }

  if (!credentials || credentials.length === 0) {
    return (
      <Card>
        <CardContent className="py-16">
          <div className="text-center">
            <Key className="mx-auto h-12 w-12 text-muted-foreground/50" />
            <h3 className="mt-4 text-lg font-semibold">No credentials</h3>
            <p className="text-muted-foreground mt-2">
              Create your first credential to enable secure connections
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {credentials.map((credential) => (
          <Card key={credential.token}>
            <CardHeader>
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-2">
                  {credential.type === "SSH" ? (
                    <Key className="h-5 w-5 text-muted-foreground" />
                  ) : (
                    <Lock className="h-5 w-5 text-muted-foreground" />
                  )}
                  <CardTitle className="text-base">{credential.name || credential.description || credential.token.substring(0, 12)}</CardTitle>
                </div>
                <Badge variant="secondary">{credential.type}</Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {credential.gatewayId && <GatewayBadge gatewayId={credential.gatewayId} />}
                {(credential.description || credential.name) && (
                  <p className="text-sm text-muted-foreground line-clamp-2">
                    {credential.description || credential.name}
                  </p>
                )}
                <div className="text-xs text-muted-foreground">
                  <p>Token: {credential.token.substring(0, 16)}...</p>
                  {credential.persistedTime && (
                    <p className="mt-1">Created: {formatDate(credential.persistedTime)}</p>
                  )}
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  className="w-full text-destructive hover:text-destructive"
                  onClick={() => setDeleteToken(credential.token)}
                >
                  <Trash2 className="mr-2 h-4 w-4" />
                  Delete
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <AlertDialog open={!!deleteToken} onOpenChange={() => setDeleteToken(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Credential</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this credential? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (deleteToken) {
                  onDelete(deleteToken);
                  setDeleteToken(null);
                }
              }}
              disabled={isDeleting}
            >
              {isDeleting ? "Deleting..." : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
