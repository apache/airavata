"use client";

import { useUserRole, type UserRole } from "@/contexts/AdvancedFeaturesContext";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { User, Shield, ShieldCheck } from "lucide-react";
import { cn } from "@/lib/utils";

const roleConfig: Record<UserRole, { label: string; icon: typeof User; color: string }> = {
  "user": {
    label: "User",
    icon: User,
    color: "text-muted-foreground",
  },
  "gateway-admin": {
    label: "Gateway Admin",
    icon: Shield,
    color: "text-blue-600",
  },
  "system-admin": {
    label: "System Admin",
    icon: ShieldCheck,
    color: "text-amber-600",
  },
};

export function RoleSelector({ className }: { className?: string }) {
  const { selectedRole, setSelectedRole, availableRoles } = useUserRole();

  if (availableRoles.length <= 1) {
    // Don't show selector if only one role available
    const role = availableRoles[0] || "user";
    const config = roleConfig[role];
    const Icon = config.icon;
    return (
      <div className={cn("flex items-center gap-2 px-3 py-1.5 rounded-md border bg-background", className)}>
        <Icon className={cn("h-4 w-4", config.color)} />
        <span className="text-sm font-medium">{config.label}</span>
      </div>
    );
  }

  const currentConfig = roleConfig[selectedRole];
  const Icon = currentConfig.icon;

  return (
    <Select value={selectedRole} onValueChange={(value) => setSelectedRole(value as UserRole)}>
      <SelectTrigger className={cn("w-[180px]", className)}>
        <div className="flex items-center gap-2">
          <Icon className={cn("h-4 w-4", currentConfig.color)} />
          <span className="font-medium">{currentConfig.label}</span>
        </div>
      </SelectTrigger>
      <SelectContent>
        {availableRoles.map((role) => {
          const config = roleConfig[role];
          const RoleIcon = config.icon;
          return (
            <SelectItem key={role} value={role}>
              <div className="flex items-center gap-2">
                <RoleIcon className={cn("h-4 w-4", config.color)} />
                <span>{config.label}</span>
              </div>
            </SelectItem>
          );
        })}
      </SelectContent>
    </Select>
  );
}
