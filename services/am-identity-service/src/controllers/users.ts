import { Request, Response } from 'express';
import pool from '../../../shared/src/db';
import { AppError } from '../../../shared/src/errors';
import { updateUserSchema } from '../../../shared/src/validations';

export async function getMe(req: Request, res: Response): Promise<void> {
  const userId = (req as any).user.userId;

  const result = await pool.query(
    `SELECT u.id, u.nombre, u.email, u.telefono, u.rol, u.estado, u.avatar_color,
            u.contacto_emergencia, u.fecha_registro, u.alertas, u.ultima_actividad,
            u.first_name, u.last_name, u.document_number, u.document_type, u.birthdate,
            p.profile_photo_url, p.tutorial_completed
     FROM identity.users u
     LEFT JOIN identity.user_profile p ON p.user_id = u.id
     WHERE u.id = $1`,
    [userId]
  );

  if (result.rows.length === 0) {
    throw new AppError('Usuario no encontrado', 'USER_NOT_FOUND', 404);
  }

  const user = result.rows[0];

  res.json({
    id: user.id,
    nombre: user.nombre,
    email: user.email,
    telefono: user.telefono,
    rol: user.rol,
    estado: user.estado,
    avatarColor: user.avatar_color,
    contactoEmergencia: user.contacto_emergencia,
    fechaRegistro: user.fecha_registro,
    alertas: user.alertas,
    ultimaActividad: user.ultima_actividad,
    firstName: user.first_name,
    lastName: user.last_name,
    documentNumber: user.document_number,
    documentType: user.document_type,
    birthdate: user.birthdate,
    profilePhotoUrl: user.profile_photo_url,
    tutorialCompleted: user.tutorial_completed,
  });
}

export async function updateMe(req: Request, res: Response): Promise<void> {
  const userId = (req as any).user.userId;
  const updates = updateUserSchema.parse(req.body);

  const fields: string[] = [];
  const values: any[] = [];
  let paramIndex = 1;

  if (updates.nombre) {
    fields.push(`nombre = $${paramIndex++}`);
    values.push(updates.nombre);
  }
  if (updates.telefono) {
    fields.push(`telefono = $${paramIndex++}`);
    values.push(updates.telefono);
  }
  if (updates.avatarColor) {
    fields.push(`avatar_color = $${paramIndex++}`);
    values.push(updates.avatarColor);
  }
  if (updates.contactoEmergencia) {
    fields.push(`contacto_emergencia = $${paramIndex++}`);
    values.push(updates.contactoEmergencia);
  }

  if (fields.length === 0) {
    throw new AppError('No hay campos para actualizar', 'NO_FIELDS', 400);
  }

  values.push(userId);

  const result = await pool.query(
    `UPDATE identity.users SET ${fields.join(', ')} WHERE id = $${paramIndex} RETURNING *`,
    values
  );

  res.json(result.rows[0]);
}
