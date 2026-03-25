"use client";

import { useState } from "react";
import { Users } from "lucide-react";
import { SearchBar } from "@/components/ui/search-bar";
import { UserList } from "@/components/users/UserList";
import { useUsers } from "@/hooks/useUsers";

export default function UsersPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const { data: users, isLoading } = useUsers();

  const filteredUsers = users?.filter(
    (user) =>
      user.userId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      `${user.firstName} ${user.lastName}`.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <div className="p-2 bg-primary/10 rounded-lg">
          <Users className="h-6 w-6 text-primary" />
        </div>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Users</h1>
          <p className="text-muted-foreground">
            Manage user accounts and permissions
          </p>
        </div>
      </div>

      <SearchBar
        placeholder="Search users by name, username, or email..."
        value={searchTerm}
        onChange={setSearchTerm}
      />

      <UserList users={filteredUsers} isLoading={isLoading} />
    </div>
  );
}
