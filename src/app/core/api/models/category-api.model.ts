/**
 * Spring Boot CategoryResponse REST API contract model.
 * Matches backend `CategoryResponse` record.
 */
export interface CategoryApiResponse {
  id: number;
  name: string;
  slug: string;
  active: boolean;
}
