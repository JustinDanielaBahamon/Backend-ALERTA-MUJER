# Admin Account Setup - Secure Process

## ⚠️ Security Notice

**Never commit passwords, password hashes, or credentials to version control.**

## Recommended Process for Creating Admin Accounts

### 1. Register through the API

Use the standard registration endpoint:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Admin User",
    "email": "admin@yourdomain.com",
    "password": "YourSecurePassword123!",
    "telefono": "+57 300 000 0000"
  }'
```

### 2. Promote to Admin via Database

Connect to your PostgreSQL database and run:

```sql
-- Find the user ID
SELECT id, email, role_id FROM identity.users WHERE email = 'admin@yourdomain.com';

-- Promote to admin role (role_id = 2)
UPDATE identity.users 
SET role_id = 2 
WHERE email = 'admin@yourdomain.com';

-- Create admin profile
INSERT INTO identity.admin_profile (account_id, created_at)
SELECT a.id, NOW()
FROM identity.account a
JOIN identity.users u ON u.id = a.user_id
WHERE u.email = 'admin@yourdomain.com';
```

### 3. Verify Admin Access

Try logging in through the admin panel to confirm the promotion worked.

## Production Environment

For production on Render:

1. Register the account through the deployed API
2. Use the Render PostgreSQL shell to run the promotion SQL
3. Never use Liquibase changesets to create accounts with passwords

## Security Best Practices

- ✅ Use strong, unique passwords for each admin account
- ✅ Enable 2FA when available
- ✅ Rotate passwords regularly
- ✅ Limit the number of admin accounts
- ✅ Audit admin actions through the audit_log table
- ❌ Never commit credentials to Git
- ❌ Never share admin credentials via email/chat
- ❌ Never use default passwords in production

## Current Production Admin

If you created an admin account using the old changeset method:

1. **Change the password immediately** through the web interface
2. If no password change feature exists, delete the account and create a new one following this guide
3. The compromised credentials should be considered permanently exposed (they're in Git history)
