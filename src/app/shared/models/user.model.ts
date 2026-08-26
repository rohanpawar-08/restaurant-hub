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
