"use client";

import { useState, useRef, useEffect } from "react";
import { ChevronDown, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { SearchBar } from "@/components/ui/search-bar";
import { cn } from "@/lib/utils";
import type { Application } from "@/types";

interface ApplicationSearchSelectProps {
  applications?: Application[];
  selectedApplication?: Application;
  onSelect: (application: Application) => void;
  isLoading?: boolean;
  placeholder?: string;
}

export function ApplicationSearchSelect({
  applications = [],
  selectedApplication,
  onSelect,
  isLoading = false,
  placeholder = "Select an application...",
}: ApplicationSearchSelectProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const dropdownRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const filteredApplications = applications.filter(
    (app) =>
      app.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      app.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
        setSearchTerm("");
      }
    };

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      setTimeout(() => inputRef.current?.focus(), 0);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isOpen]);

  const handleSelect = (app: Application) => {
    onSelect(app);
    setIsOpen(false);
    setSearchTerm("");
  };

  return (
    <div className="relative w-full" ref={dropdownRef}>
      <Button
        type="button"
        variant="outline"
        className={cn(
          "w-full justify-between h-9 text-left font-normal",
          !selectedApplication && "text-muted-foreground"
        )}
        onClick={() => setIsOpen(!isOpen)}
      >
        <span className="truncate">
          {selectedApplication
            ? `${selectedApplication.name}${selectedApplication.version ? ` v${selectedApplication.version}` : ""}`
            : placeholder}
        </span>
        <ChevronDown className="h-4 w-4 opacity-50 shrink-0 ml-2" />
      </Button>

      {isOpen && (
        <div className="absolute z-50 w-full mt-1 bg-popover border rounded-md shadow-md">
          <div className="p-2 border-b">
            <SearchBar
              ref={inputRef}
              placeholder="Search applications..."
              value={searchTerm}
              onChange={setSearchTerm}
              onClick={(e) => e.stopPropagation()}
              wrapperClassName="border-0 rounded-md"
            />
          </div>
          <div className="max-h-[300px] overflow-y-auto">
            {isLoading ? (
              <div className="p-4 text-center text-sm text-muted-foreground">
                Loading applications...
              </div>
            ) : filteredApplications.length === 0 ? (
              <div className="p-4 text-center text-sm text-muted-foreground">
                {searchTerm ? "No applications found" : "No applications available"}
              </div>
            ) : (
              filteredApplications.map((app) => (
                <button
                  key={app.applicationId}
                  type="button"
                  className={cn(
                    "w-full text-left px-3 py-2 text-sm hover:bg-accent focus:bg-accent focus:outline-none flex items-center justify-between",
                    selectedApplication?.applicationId === app.applicationId && "bg-accent"
                  )}
                  onClick={() => handleSelect(app)}
                >
                  <div className="flex-1 min-w-0">
                    <div className="font-medium truncate">
                      {app.name}
                      {app.version && (
                        <span className="ml-2 text-xs text-muted-foreground font-normal">
                          v{app.version}
                        </span>
                      )}
                    </div>
                    {app.description && (
                      <div className="text-xs text-muted-foreground truncate">
                        {app.description}
                      </div>
                    )}
                  </div>
                  {selectedApplication?.applicationId === app.applicationId && (
                    <Check className="h-4 w-4 text-primary ml-2 shrink-0" />
                  )}
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
