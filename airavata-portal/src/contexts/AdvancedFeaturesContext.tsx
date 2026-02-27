"use client";

import { createContext, useContext, useState, useEffect, ReactNode } from "react";

export type UserRole = "user" | "gateway-admin" | "system-admin";

interface UserRoleContextType {
  selectedRole: UserRole;
  setSelectedRole: (role: UserRole) => void;
  availableRoles: UserRole[];
}

const UserRoleContext = createContext<UserRoleContextType | undefined>(undefined);

export function UserRoleProvider({ 
  children,
  isRootUser,
  hasGatewayAccess,
}: { 
  children: ReactNode;
  isRootUser: boolean;
  hasGatewayAccess: boolean;
}) {
  // Determine available roles based on user permissions
  const availableRoles: UserRole[] = [];
  if (hasGatewayAccess) {
    availableRoles.push("user");
  }
  if (hasGatewayAccess) {
    availableRoles.push("gateway-admin");
  }
  if (isRootUser) {
    availableRoles.push("system-admin");
  }

  // Default role: gateway-admin if available, otherwise user
  const defaultRole: UserRole = availableRoles.includes("gateway-admin") 
    ? "gateway-admin" 
    : availableRoles.includes("user")
    ? "user"
    : "system-admin";

  const [selectedRole, setSelectedRole] = useState<UserRole>(defaultRole);

  // Load from sessionStorage on mount
  useEffect(() => {
    if (typeof window !== "undefined") {
      const stored = sessionStorage.getItem("selectedRole") as UserRole | null;
      if (stored && availableRoles.includes(stored)) {
        setSelectedRole(stored);
      } else {
        setSelectedRole(defaultRole);
      }
    }
  }, []);

  // Save to sessionStorage when changed
  useEffect(() => {
    if (typeof window !== "undefined") {
      sessionStorage.setItem("selectedRole", selectedRole);
    }
  }, [selectedRole]);

  return (
    <UserRoleContext.Provider
      value={{
        selectedRole,
        setSelectedRole,
        availableRoles,
      }}
    >
      {children}
    </UserRoleContext.Provider>
  );
}

export function useUserRole() {
  const context = useContext(UserRoleContext);
  if (context === undefined) {
    throw new Error("useUserRole must be used within a UserRoleProvider");
  }
  return context;
}
