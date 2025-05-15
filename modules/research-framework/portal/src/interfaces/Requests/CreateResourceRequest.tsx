import { PrivacyEnum } from "../PrivacyEnum";

export interface CreateResourceRequest {
  name: string;
  description: string;
  tags: string[];
  headerImage: string;
  authors: string[];
  privacy: PrivacyEnum;
}
