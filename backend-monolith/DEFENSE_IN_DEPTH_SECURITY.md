# Defense in Depth Security Implementation - AlertaMujer

## Overview

This document describes the Defense in Depth security implementation for the AlertaMujer PostgreSQL database. The security approach uses multiple layers of protection to ensure data integrity and prevent unauthorized access.

## Security Layers Implemented

### 1. Role-Based Access Control (RBAC)

#### Users Created

**alerta_app** (Runtime Application User)
- **Purpose:** Used by Spring Boot application at runtime
- **Privileges:** DML only (SELECT, INSERT, UPDATE, DELETE)
- **No DDL:** Cannot CREATE, ALTER, or DROP tables
- **Login:** Yes
- **Superuser:** No
- **Createdb:** No
- **Createrole:** No

**alerta_admin** (Deployment/Admin User)
- **Purpose:** Used during deployment and by database administrators
- **Privileges:** Full DDL + DML (ALL PRIVILEGES)
- **Login:** Yes
- **Superuser:** No
- **Createdb:** No
- **Createrole:** No

#### Privileges Granted

**alerta_app:**
- Usage on all schemas (identity, alert, resource, admin)
- SELECT, INSERT, UPDATE, DELETE on all tables
- USAGE, SELECT on all sequences (for auto-increment IDs)
- EXECUTE on all functions
- Automatic grants on future objects via ALTER DEFAULT PRIVILEGES

**alerta_admin:**
- ALL PRIVILEGES on all schemas
- ALL PRIVILEGES on all tables
- ALL PRIVILEGES on all sequences
- Full control for migrations and maintenance

#### Security Benefit

If an attacker manages to inject SQL through the application:
- They **cannot** execute `DROP TABLE` (no DDL privileges)
- They **cannot** alter schema structure
- They **cannot** create new tables or modify existing ones
- They are limited to DML operations only

### 2. Row-Level Security (RLS)

#### Tables Protected

**alert.alert**
- Users can only SELECT their own alerts
- Users can only INSERT their own alerts
- Users can only UPDATE their own alerts
- Users can only DELETE their own alerts

**alert.location_log**
- Users can only SELECT their own location logs
- Users can only INSERT their own location logs
- Users can only UPDATE their own location logs
- Users can only DELETE their own location logs

#### Implementation Details

**User Context Function:**
```sql
CREATE OR REPLACE FUNCTION alert.set_current_user_id(p_user_id BIGINT)
RETURNS VOID AS $$
BEGIN
    PERFORM set_config('alert.current_user_id', p_user_id::TEXT, false);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

**RLS Policies:**
```sql
CREATE POLICY alert_user_select_policy ON alert.alert
    FOR SELECT
    USING (user_id = alert.get_current_user_id());
```

#### Security Benefit

Even if the backend validation fails:
- PostgreSQL enforces row-level access
- Users cannot see or modify other users' data
- Access control is enforced at the database engine level
- SQL injection cannot bypass RLS

### 3. Audit Triggers

#### Tables Audited

**identity.users**
- Audits UPDATE operations
- Audits DELETE operations
- Logs to admin.audit_log table

**alert.alert**
- Audits UPDATE operations
- Audits DELETE operations
- Logs to admin.audit_log table

#### Audit Log Fields

- `account_id`: ID of the user who made the change
- `action`: 'UPDATE' or 'DELETE'
- `entity_type`: 'USER' or 'ALERT'
- `entity_id`: ID of the affected record
- `description`: Description of the change
- `ip_address`: IP address of the client
- `created_at`: Timestamp of the audit event

#### Security Benefit

- All modifications are logged automatically
- Even direct DB modifications by DBAs are audited
- Audit trail is immutable (trigger-based)
- Helps detect suspicious activity
- Provides forensic evidence for investigations

### 4. Check Constraints

#### Geographic Constraints

**alert.location_log:**
- `chk_location_log_latitude`: latitude BETWEEN -90 AND 90
- `chk_location_log_longitude`: longitude BETWEEN -180 AND 180

**alert.alert** (if columns exist):
- `chk_alert_latitude`: latitude BETWEEN -90 AND 90
- `chk_alert_longitude`: longitude BETWEEN -180 AND 180

**resource.zone:**
- `chk_zone_radius_positive`: radius_meters > 0

#### Data Integrity Constraints

**resource.zone:**
- `chk_zone_risk_level`: risk_level IN ('low', 'medium', 'high')
- `chk_zone_type`: zone_type IN ('risk', 'safe')

**identity.account:**
- `chk_account_status`: status IN ('active', 'inactive', 'blocked', 'pending')

**alert.alert:**
- `chk_alert_status`: status IN ('active', 'resolved', 'cancelled', 'expired')
- `chk_alert_type`: alert_type IN ('main', 'medical', 'police', 'fire')

**identity.device:**
- `chk_device_gps_status`: gps_status IN ('active', 'inactive', 'unknown', 'denied')
- `chk_device_status`: status IN ('active', 'inactive', 'blocked', 'lost')

#### Security Benefit

- Invalid data is rejected at the database level
- Prevents data corruption
- Enforces business rules regardless of application logic
- SQL injection cannot bypass constraints

## Deployment Instructions

### 1. Pre-Deployment Checklist

- [ ] Ensure PostgreSQL 16 is installed
- [ ] Ensure database `alerta_mujer` exists
- [ ] Ensure Liquibase has run initial schema migration
- [ ] Change default passwords in the script
- [ ] Backup the database

### 2. Change Default Passwords

**IMPORTANT:** The script contains placeholder passwords that MUST be changed:

```sql
-- In the script, change these lines:
CREATE ROLE alerta_app WITH PASSWORD 'CHANGE_ME_ALERTA_APP_PASSWORD';
CREATE ROLE alerta_admin WITH PASSWORD 'CHANGE_ME_ALERTA_ADMIN_PASSWORD';

-- To:
CREATE ROLE alerta_app WITH PASSWORD 'your_secure_random_password_here';
CREATE ROLE alerta_admin WITH PASSWORD 'your_secure_random_password_here';
```

### 3. Run the Script

```bash
# Using psql
psql -U postgres -d alerta_mujer -f src/main/resources/db/security/defense-in-depth.sql

# Or connect first
psql -U postgres -d alerta_mujer
\i src/main/resources/db/security/defense-in-depth.sql
```

### 4. Verify Installation

The script includes verification queries at the end:

```sql
-- Verify roles
SELECT rolname, rolcanlogin, rolsuper, rolcreatedb, rolcreaterole
FROM pg_roles
WHERE rolname IN ('alerta_app', 'alerta_admin');

-- Verify RLS is enabled
SELECT schemaname, tablename, rowsecurity
FROM pg_tables
WHERE schemaname IN ('alert')
AND tablename IN ('alert', 'location_log');

-- Verify policies
SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual
FROM pg_policies
WHERE schemaname = 'alert';

-- Verify constraints
SELECT n.nspname AS schema, c.relname AS table, con.conname AS constraint
FROM pg_constraint con
JOIN pg_class c ON con.conrelid = c.oid
JOIN pg_namespace n ON c.relnamespace = n.oid
WHERE n.nspname IN ('alert', 'identity', 'resource')
AND con.contype = 'c';
```

## Spring Boot Integration

### 1. Update application.properties

```properties
# Use alerta_app for runtime (DML only)
spring.datasource.username=alerta_app
spring.datasource.password=your_secure_random_password_here

# Use alerta_admin for Liquibase migrations (DDL + DML)
# This requires a separate configuration or environment variable
```

### 2. Configure Liquibase to Use alerta_admin

**Option A: Environment Variable**
```bash
export LIQUIBASE_USERNAME=alerta_admin
export LIQUIBASE_PASSWORD=your_secure_random_password_here
```

**Option B: Separate Liquibase Configuration**
Create `application-liquibase.properties`:
```properties
spring.liquibase.username=alerta_admin
spring.liquibase.password=your_secure_random_password_here
```

### 3. Implement User Context Setter

Create a service to set the user context before queries:

```java
@Service
public class UserContextService {

    @PersistenceContext
    private EntityManager entityManager;

    public void setCurrentUserId(Long userId) {
        entityManager.createNativeQuery(
            "SELECT alert.set_current_user_id(:userId)"
        )
        .setParameter("userId", userId)
        .executeUpdate();
    }

    public Long getCurrentUserId() {
        Object result = entityManager.createNativeQuery(
            "SELECT alert.get_current_user_id()"
        ).getSingleResult();
        return result != null ? ((Number) result).longValue() : null;
    }
}
```

### 4. Use in Application Services

```java
@Service
public class AlertService {

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private AlertRepository alertRepository;

    public List<Alert> getUserAlerts(Long userId) {
        // Set user context for RLS
        userContextService.setCurrentUserId(userId);

        // RLS will automatically filter results
        return alertRepository.findAll();
    }

    public Alert createAlert(Alert alert, Long userId) {
        // Set user context for RLS
        userContextService.setCurrentUserId(userId);

        // Set user_id in the entity
        alert.setUserId(userId);

        // RLS will verify the insert
        return alertRepository.save(alert);
    }
}
```

### 5. Alternative: Spring Security Integration

Create a custom filter to set user context automatically:

```java
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Autowired
    private UserContextService userContextService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // Get current user from Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // Extract user ID from authentication
            Long userId = extractUserId(authentication);

            // Set user context for RLS
            userContextService.setCurrentUserId(userId);
        }

        filterChain.doFilter(request, response);
    }

    private Long extractUserId(Authentication authentication) {
        // Implement based on your authentication setup
        // For example, if using custom UserDetails:
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
```

## Security Testing

### 1. Test RBAC

```sql
-- Connect as alerta_app
psql -U alerta_app -d alerta_mujer

-- Try to drop a table (should fail)
DROP TABLE identity.users;
-- ERROR: permission denied for table users

-- Try to create a table (should fail)
CREATE TABLE test_table (id INT);
-- ERROR: permission denied for schema identity

-- Try to insert data (should succeed)
INSERT INTO identity.role (name, description) VALUES ('test_role', 'Test');
-- SUCCESS
```

### 2. Test RLS

```sql
-- Connect as alerta_app
psql -U alerta_app -d alerta_mujer

-- Set user context to user 1
SELECT alert.set_current_user_id(1);

-- Insert alert for user 1 (should succeed)
INSERT INTO alert.alert (user_id, user_profile_id, device_id, alert_type, activation_method, status)
VALUES (1, 1, 1, 'main', 'panic_button', 'active');
-- SUCCESS

-- Try to insert alert for user 2 (should fail)
INSERT INTO alert.alert (user_id, user_profile_id, device_id, alert_type, activation_method, status)
VALUES (2, 2, 2, 'main', 'panic_button', 'active');
-- ERROR: new row violates row-level security policy

-- Select all alerts (should only show user 1's alerts)
SELECT * FROM alert.alert;
-- Only returns alerts where user_id = 1
```

### 3. Test Audit Triggers

```sql
-- Update a user record
UPDATE identity.users SET role_id = 2 WHERE id = 1;

-- Check audit log
SELECT * FROM admin.audit_log ORDER BY created_at DESC LIMIT 1;
-- Should show UPDATE action on USER entity

-- Delete an alert
DELETE FROM alert.alert WHERE id = 1;

-- Check audit log
SELECT * FROM admin.audit_log ORDER BY created_at DESC LIMIT 1;
-- Should show DELETE action on ALERT entity
```

### 4. Test Check Constraints

```sql
-- Try to insert invalid latitude (should fail)
INSERT INTO alert.location_log (user_id, latitude, longitude, recorded_at)
VALUES (1, 91, 0, NOW());
-- ERROR: new row violates check constraint "chk_location_log_latitude"

-- Try to insert invalid longitude (should fail)
INSERT INTO alert.location_log (user_id, latitude, longitude, recorded_at)
VALUES (1, 0, 181, NOW());
-- ERROR: new row violates check constraint "chk_location_log_longitude"

-- Try to insert valid coordinates (should succeed)
INSERT INTO alert.location_log (user_id, latitude, longitude, recorded_at)
VALUES (1, 4.711, -74.072, NOW());
-- SUCCESS
```

## Maintenance

### 1. Password Rotation

Rotate passwords regularly:

```sql
ALTER ROLE alerta_app WITH PASSWORD 'new_secure_password';
ALTER ROLE alerta_admin WITH PASSWORD 'new_secure_password';
```

Update application.properties after password change.

### 2. Audit Log Cleanup

Archive or delete old audit logs:

```sql
-- Delete logs older than 1 year
DELETE FROM admin.audit_log
WHERE created_at < NOW() - INTERVAL '1 year';

-- Or archive to a separate table
CREATE TABLE admin.audit_log_archive AS
SELECT * FROM admin.audit_log
WHERE created_at < NOW() - INTERVAL '1 year';

DELETE FROM admin.audit_log
WHERE created_at < NOW() - INTERVAL '1 year';
```

### 3. Review and Update Policies

Regularly review RLS policies and constraints to ensure they meet security requirements.

### 4. Monitor Failed Access Attempts

Monitor PostgreSQL logs for failed access attempts:

```sql
-- Check for constraint violations
SELECT * FROM admin.audit_log
WHERE description LIKE '%violates%'
ORDER BY created_at DESC;
```

## Troubleshooting

### Issue: RLS Policies Not Working

**Symptom:** Users can see other users' data

**Solution:**
1. Verify RLS is enabled:
   ```sql
   SELECT schemaname, tablename, rowsecurity
   FROM pg_tables
   WHERE tablename IN ('alert', 'location_log');
   ```
2. Verify user context is set:
   ```sql
   SELECT alert.get_current_user_id();
   ```
3. Verify policies exist:
   ```sql
   SELECT * FROM pg_policies WHERE schemaname = 'alert';
   ```

### Issue: Liquibase Fails with Permission Denied

**Symptom:** Liquibase migration fails with permission error

**Solution:**
Ensure Liquibase is using `alerta_admin` (not `alerta_app`):
```properties
spring.liquibase.username=alerta_admin
spring.liquibase.password=your_secure_password
```

### Issue: Application Cannot Connect

**Symptom:** Connection refused or authentication failed

**Solution:**
1. Verify roles exist:
   ```sql
   SELECT rolname FROM pg_roles WHERE rolname IN ('alerta_app', 'alerta_admin');
   ```
2. Verify password is correct in application.properties
3. Verify pg_hba.conf allows password authentication

### Issue: Audit Triggers Not Firing

**Symptom:** No records in audit_log table

**Solution:**
1. Verify triggers exist:
   ```sql
   SELECT trigger_name, event_object_table
   FROM information_schema.triggers
   WHERE trigger_name LIKE '%audit%';
   ```
2. Verify function exists:
   ```sql
   SELECT routine_name FROM information_schema.routines
   WHERE routine_name LIKE '%audit%';
   ```

## Security Best Practices

1. **Never use alerta_admin in production runtime**
   - Use alerta_app for application
   - Use alerta_admin only for migrations and DBA tasks

2. **Rotate passwords regularly**
   - Every 90 days minimum
   - Use strong, randomly generated passwords

3. **Monitor audit logs**
   - Review suspicious activity
   - Set up alerts for unusual patterns

4. **Keep PostgreSQL updated**
   - Apply security patches promptly
   - Use supported PostgreSQL versions

5. **Encrypt connections**
   - Use SSL/TLS for database connections
   - Configure pg_hba.conf to require SSL

6. **Limit network access**
   - Use firewall rules
   - Restrict access to application servers only

7. **Regular security audits**
   - Review RLS policies
   - Review constraints
   - Review user privileges

## References

- [PostgreSQL Row-Level Security](https://www.postgresql.org/docs/current/ddl-rowsecurity.html)
- [PostgreSQL Triggers](https://www.postgresql.org/docs/current/sql-createtrigger.html)
- [PostgreSQL Check Constraints](https://www.postgresql.org/docs/current/ddl-constraints.html)
- [PostgreSQL Role Management](https://www.postgresql.org/docs/current/user-manag.html)
- [OWASP Database Security](https://owasp.org/www-community/controls/Database_Security_Cheat_Sheet)

## Summary

This Defense in Depth implementation provides:

✅ **Role-Based Access Control** - Separate users for deployment and runtime
✅ **Row-Level Security** - Users can only access their own data
✅ **Audit Triggers** - All modifications are logged automatically
✅ **Check Constraints** - Data integrity enforced at database level
✅ **SQL Injection Protection** - DDL restrictions prevent schema modification
✅ **Multi-Layer Security** - Protection at multiple levels
✅ **PostgreSQL Native** - Leverages database engine security features

The security measures ensure that even if the application layer is compromised, the database layer provides additional protection.
