-- Script manual para crear usuario admin en producción
-- Email: nuevoadmin@test.com
-- Password: Admin2024!

-- 1. Crear usuario
INSERT INTO identity.users (role_id, first_name, last_name, telephone, email, created_at) 
VALUES (2, 'Nuevo', 'Admin', '+57 320 000 0011', 'nuevoadmin@test.com', NOW())
ON CONFLICT DO NOTHING
RETURNING id;

-- 2. Obtener el ID del usuario (reemplaza XXX con el ID del paso anterior si es necesario)
DO $$
DECLARE
    v_user_id BIGINT;
    v_account_id BIGINT;
BEGIN
    -- Obtener user_id
    SELECT id INTO v_user_id FROM identity.users WHERE email = 'nuevoadmin@test.com';
    
    -- Crear cuenta si no existe
    INSERT INTO identity.account (user_id, password_hash, status, last_access)
    VALUES (v_user_id, '$2a$10$rZ6qJYzQX9EZDfGvK5HY4.KmVBZK2pXJ8YqZGqN5YWKPzL5JsXw9u', 'active', NULL)
    ON CONFLICT DO NOTHING;
    
    -- Crear perfil de usuario si no existe
    INSERT INTO identity.user_profile (user_id, profile_photo_url, tutorial_completed, tutorial_seen_at, created_at)
    VALUES (v_user_id, NULL, TRUE, NOW(), NOW())
    ON CONFLICT DO NOTHING;
    
    -- Obtener account_id y crear admin_profile
    SELECT id INTO v_account_id FROM identity.account WHERE user_id = v_user_id;
    
    INSERT INTO identity.admin_profile (account_id, created_at)
    VALUES (v_account_id, NOW())
    ON CONFLICT DO NOTHING;
    
    RAISE NOTICE 'Usuario admin creado exitosamente con ID: %', v_user_id;
END$$;

-- 3. Verificar que se creó correctamente
SELECT u.id, u.email, u.first_name, u.last_name, r.name as role, a.status as account_status
FROM identity.users u
JOIN identity.role r ON r.id = u.role_id
LEFT JOIN identity.account a ON a.user_id = u.id
WHERE u.email = 'nuevoadmin@test.com';
