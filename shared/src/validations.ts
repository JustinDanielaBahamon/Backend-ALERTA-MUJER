import { z } from 'zod';

export const registerSchema = z.object({
  nombre: z.string().min(1, 'El nombre es requerido'),
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'La contraseña debe tener al menos 6 caracteres'),
  telefono: z.string().optional(),
});

export const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(1, 'La contraseña es requerida'),
});

export const forgotPasswordSchema = z.object({
  email: z.string().email('Email inválido'),
});

export const resetPasswordSchema = z.object({
  token: z.string().min(1, 'El token es requerido'),
  newPassword: z.string().min(6, 'La contraseña debe tener al menos 6 caracteres'),
});

export const updateUserSchema = z.object({
  nombre: z.string().optional(),
  telefono: z.string().optional(),
  avatarColor: z.string().optional(),
  contactoEmergencia: z.string().optional(),
});
