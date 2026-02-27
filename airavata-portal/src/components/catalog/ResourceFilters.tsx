"use client";

import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SearchBar } from "@/components/ui/search-bar";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { Badge } from "@/components/ui/badge";
import { useCatalogTags } from "@/hooks";
import { ArtifactType, type ArtifactFilters as Filters } from "@/types/catalog";

interface Props {
  filters: Filters;
  onFiltersChange: (filters: Filters) => void;
}

export function ResourceFilters({ filters, onFiltersChange }: Props) {
  const { data: allTags } = useCatalogTags();
  const [searchTerm, setSearchTerm] = useState(filters.nameSearch || "");

  // Debounce search
  useEffect(() => {
    const timer = setTimeout(() => {
      onFiltersChange({ ...filters, nameSearch: searchTerm || undefined });
    }, 400);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const toggleResourceType = (type: ArtifactType) => {
    const currentType = filters.type;
    onFiltersChange({
      ...filters,
      type: currentType === type ? undefined : type,
    });
  };

  const toggleTag = (tagId: string) => {
    const currentTags = filters.tags || [];
    const newTags = currentTags.includes(tagId)
      ? currentTags.filter((t) => t !== tagId)
      : [...currentTags, tagId];
    onFiltersChange({
      ...filters,
      tags: newTags.length > 0 ? newTags : undefined,
    });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Filters</CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-2">
          <Label>Search</Label>
          <SearchBar
            placeholder="Search resources..."
            value={searchTerm}
            onChange={setSearchTerm}
          />
        </div>

        <div className="space-y-2">
          <Label>Resource Type</Label>
          <div className="space-y-2">
            {Object.values(ArtifactType).map((type) => (
              <div key={type} className="flex items-center space-x-2">
                <Checkbox
                  id={type}
                  checked={filters.type === type}
                  onCheckedChange={() => toggleResourceType(type)}
                />
                <label
                  htmlFor={type}
                  className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                >
                  {type}
                </label>
              </div>
            ))}
          </div>
        </div>

        {allTags && allTags.length > 0 && (
          <div className="space-y-2">
            <Label>Tags</Label>
            <div className="flex flex-wrap gap-2">
              {allTags.map((tag) => (
                <Badge
                  key={tag.id}
                  variant={filters.tags?.includes(tag.id) ? "default" : "outline"}
                  className="cursor-pointer"
                  onClick={() => toggleTag(tag.id)}
                >
                  {tag.name}
                </Badge>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
