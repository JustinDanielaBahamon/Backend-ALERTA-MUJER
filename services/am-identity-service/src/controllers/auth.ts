import { Request, Response } from 'express';
import bcrypt from 'bcrypt';
import { v4 as uuidv4 } from 'uuid';
import pool from '../../../shared/src/db';
import { signToken } from '../../../shared/src/jwt';
import { AppError } from '../../../shared/src/errors';
import { registerSchema, loginSchema, forgotPasswordSchema, resetPasswordSchema } from '../../../shared/src/validations';

export async function register(req: Request, res: Response): Promise<void> {
  const { nombre, email, password, telefono } = registerSchema.parse(req.body);

  const existing = await pool.query('SELECT id FROM identity.users WHERE email = $1', [email]);
  if (existing.rows.length > 0) {
    throw new AppError('El email ya está registrado', 'EMAIL_EXISTS', 409);
  }

  const hashedPassword = await bcrypt.hash(password, 10);

  const userResult = await pool.query(
    `INSERT INTO identity.users (nombre, email, correo, telefono, rol, role_id, first_name, last_name, created_at)
     VALUES ($1, $2, $3, $4, 'Usuaria', 1, $5, $6, NOW())
     RETURNING id, nombre, email, rol`,
    [nombre, email, email, telefono || '', nombre.split(' ')[0], nombre.split(' ').slice(1).join(' ')]
  );

  const user = userResult.rows[0];

  await pool.query(
    'INSERT INTO identity.account (user_id, password_hash, status) VALUES ($1, $2, $3)',
    [user.id, hashedPassword, 'active']
  );

  await pool.query(
    'INSERT INTO identity.user_profile (user_id, tutorial_completed) VALUES ($1, $2)',
    [user.id, false]
  );

  const token = signToken({ userId: user.id, role: 'user' });

  res.status(201).json({
    token,
    user: {
      id: user.id,
      nombre: user.nombre,
      email: user.email,
      rol: user.rol,
    },
  });
}

export async function login(req: Request, res: Response): Promise<void> {
  const { email, password } = loginSchema.parse(req.body);

  const userResult = await pool.query(
    `SELECT u.id, u.nombre, u.email, u.rol, u.telefono, u.avatar_color, u.contacto_emergencia,
            a.password_hash, a.status
     FROM identity.users u
     JOIN identity.account a ON a.user_id = u.id
     WHERE u.email = $1`,
    [email]
  );

  if (userResult.rows.length === 0) {
    throw new AppError('Credenciales incorrectas', 'INVALID_CREDENTIALS', 401);
  }

  const user = userResult.rows[0];

  if (user.status === 'blocked') {
    throw new AppError('Cuenta bloqueada', 'ACCOUNT_BLOCKED', 403);
  }

  const validPassword = await bcrypt.compare(password, user.password_hash);
  if (!validPassword) {
    throw new AppError('Credenciales incorrectas', 'INVALID_CREDENTIALS', 401);
  }

  await pool.query('UPDATE identity.account SET last_access = NOW() WHERE user_id = $1', [user.id]);

  const token = signToken({ userId: user.id, role: user.rol === 'Admin' ? 'admin' : 'user' });

  res.json({
    token,
    user: {
      id: user.id,
      nombre: user.nombre,
      email: user.email,
      rol: user.rol,
      telefono: user.telefono,
      avatarColor: user.avatar_color,
      contactoEmergencia: user.contacto_emergencia,
    },
  });
}

export async function forgotPassword(req: Request, res: Response): Promise<void> {
  const { email } = forgotPasswordSchema.parse(req.body);

  const userResult = await pool.query('SELECT id FROM identity.users WHERE email = $1', [email]);

  if (userResult.rows.length === 0) {
    res.json({ message: 'Si el email existe, se enviará un enlace de recuperación' });
    return;
  }

  const userId = userResult.rows[0].id;
  const token = uuidv4();
  const expiresAt = new Date(Date.now() + 60 * 60 * 1000);

  await pool.query(
    'INSERT INTO identity.recovery_request (user_id, token, expires_at) VALUES ($1, $2, $3)',
    [userId, token, expiresAt]
  );

  res.json({ message: 'Si el email existe, se enviará un enlace de recuperación' });
}

export async function resetPassword(req: Request, res: Response): Promise<void> {
  const { token, newPassword } = resetPasswordSchema.parse(req.body);

  const requestResult = await pool.query(
    `SELECT user_id FROM identity.recovery_request
     WHERE token = $1 AND used = FALSE AND expires_at > NOW()`,
    [token]
  );

  if (requestResult.rows.length === 0) {
    throw new AppError('Token inválido o expirado', 'INVALID_TOKEN', 400);
  }

  const userId = requestResult.rows[0].user_id;
  const hashedPassword = await bcrypt.hash(newPassword, 10);

  await pool.query('UPDATE identity.account SET password_hash = $1 WHERE user_id = $2', [hashedPassword, userId]);
  await pool.query('UPDATE identity.recovery_request SET used = TRUE WHERE token = $1', [token]);

  res.json({ message: 'Contraseña actualizada correctamente' });
}
