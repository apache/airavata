import { AuthorType } from "./AuthorType";

export interface MetadataType {
  type: string;
  title: string;
  slug: string;
  description: string;
  author: AuthorType;
  tags: string[];
  date: string;
  images: {
    headerImage: string;
  };
}
