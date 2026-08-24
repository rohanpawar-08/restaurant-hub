/**
 * Contract interface matching Spring Boot `UserResponse` DTO.
 * Explicitly omits passwordHash to ensure security.
 */
export interface UserApiResponse {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  role: 'CUSTOMER' | 'ADMIN';
  createdAt: string;
}
