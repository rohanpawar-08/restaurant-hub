export interface User {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  role?: string;
  createdAt: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegistrationData {
  fullName: string;
  email: string;
  phone: string;
  password: string;
  confirmPassword?: string;
}

/**
 * Development-only mock stored user record.
 * Contains mock password for frontend simulation only.
 * MUST NOT be used in production or sent to backend.
 */
export interface MockUserRecord extends User {
  passwordHashMock?: string;
}
