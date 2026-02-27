"use client";

import * as React from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

export interface SearchBarProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, "value" | "onChange"> {
  value?: string;
  onChange?: (value: string) => void;
  /** Optional class for the outer wrapper */
  wrapperClassName?: string;
  /** Optional content after the input (e.g. divider + filter buttons) */
  children?: React.ReactNode;
}

const SearchBar = React.forwardRef<HTMLInputElement, SearchBarProps>(
  ({ placeholder = "Search...", value = "", onChange, wrapperClassName, className, children, ...props }, ref) => {
    return (
      <div className={cn("flex items-center gap-1 p-1 bg-muted/50 rounded-lg border", wrapperClassName)}>
        <div className="relative flex-1 min-w-0">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground shrink-0 pointer-events-none" />
          <Input
            ref={ref}
            type="search"
            placeholder={placeholder}
            value={value}
            onChange={(e) => onChange?.(e.target.value)}
            className={cn("pl-10 border-0 bg-transparent shadow-none focus-visible:ring-0", className)}
            {...props}
          />
        </div>
        {children}
      </div>
    );
  }
);
SearchBar.displayName = "SearchBar";

export { SearchBar };
