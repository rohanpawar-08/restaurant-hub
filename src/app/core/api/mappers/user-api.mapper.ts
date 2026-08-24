import { User } from '../../../shared/models/user.model';
import { UserApiResponse } from '../models/user-api.model';

/**
 * Pure mapper function translating Spring Boot UserApiResponse DTO into Angular User domain model.
 */
export function mapUserApiResponseToUser(response: UserApiResponse): User {
  return {
    id: String(response.id),
    fullName: response.fullName,
    email: response.email,
    phone: response.phone,
    role: response.role,
    createdAt: response.createdAt,
  };
}
