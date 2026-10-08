# Liquibase Migration Guide - AlertaMujer

## Overview

This document describes the Liquibase migration structure created from the original `init.sql` file. The migration follows a modular, version-controlled approach for database schema evolution.

## Directory Structure

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Main orchestrator file
└── changesets/
    └── v1.0.0/
        ├── 01-identity-schema.yaml    # Identity service tables (11 tables)
        ├── 02-alert-schema.yaml       # Alert service tables (8 tables)
        ├── 03-resource-schema.yaml   # Resource service tables (4 tables)
        ├── 04-admin-schema.yaml       # Admin service tables (4 tables)
        └── 05-seed-data.yaml          # Seed data (context: test,dev)
```

## Database Schemas

### 1. Identity Schema (11 tables)
- `role` - User roles
- `users` - User accounts with credentials
- `account` - Authentication accounts with password hashes
- `user_profile` - User profile information
- `admin_profile` - Administrator-specific profiles
- `recovery_request` - Password recovery tokens
- `device` - User devices
- `device_permission` - Device-specific permissions
- `device_session` - **Native DB table** (no JPA entity) - Session management
- `alert_activation_setting` - Alert configuration per device
- `user_preference` - User UI preferences

### 2. Alert Schema (8 tables)
- `emergency_contact` - Emergency contacts for users
- `alert` - Active alerts
- `alert_contact` - Contact attempts during alerts
- `location_log` - GPS location tracking
- `notification` - User notifications
- `evidence` - Media evidence attached to alerts
- `frequent_location` - User's frequent locations
- `alert_reminder` - Scheduled alert reminders

### 3. Resource Schema (4 tables)
- `zone` - Geographic zones with risk levels
- `zone_report` - User reports about zones
- `emergency_resource` - Emergency services and resources
- `resource_call` - Calls made to emergency resources

### 4. Admin Schema (4 tables)
- `audit_log` - **Native DB table** (no JPA entity) - System audit trail
- `user_report` - User reports for moderation
- `moderation_action` - Administrative actions taken
- `system_configuration` - **Native DB table** (no JPA entity) - Global system settings

## Key Features

### 1. Separation of DDL and DML
- **DDL** (Data Definition Language): Tables creation in schema files (01-04)
- **DML** (Data Manipulation Language): Seed data in 05-seed-data.yaml
- Seed data only runs in `test` and `dev` contexts (NOT in production)

### 2. Rollback Support
Every changeset includes proper rollback statements:
- DDL changes use `dropTable` for rollback
- DML changes use `DELETE` statements with WHERE clauses
- Schema creation can be dropped with CASCADE

### 3. Context-Based Execution
- Seed data (05-seed-data.yaml) has `context: test,dev`
- Production runs: `spring.liquibase.contexts=prod` (no seed data)
- Development runs: `spring.liquibase.contexts=dev` (includes seed data)

### 4. Native Database Tables (No JPA Entities)
The following 9 tables are managed exclusively by Liquibase:
1. `identity.device_session` - Session management
2. `identity.recovery_request` - Recovery tokens
3. `identity.device_permission` - Device permissions
4. `identity.alert_activation_setting` - Alert config
5. `identity.user_preference` - User preferences
6. `admin.audit_log` - Audit trail
7. `admin.system_configuration` - System settings
8. `admin.user_report` - User reports
9. `admin.moderation_action` - Moderation actions

These tables are critical for system operation but don't require JPA entities.

## Configuration

### application.properties
```properties
# Liquibase Configuration
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.liquibase.contexts=${LIQUIBASE_CONTEXTS:dev}
```

### Environment Variables
- `LIQUIBASE_CONTEXTS` - Set to `prod` for production (no seed data), `dev` for development (with seed data)

## Migration Workflow

### Running Migrations
```bash
# Development (with seed data)
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.liquibase.contexts=dev

# Production (without seed data)
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.liquibase.contexts=prod
```

### Checking Migrations
Liquibase automatically:
1. Creates `DATABASECHANGELOG` table to track executed changesets
2. Creates `DATABASECHANGELOGLOCK` table to prevent concurrent execution
3. Skips already executed changesets based on checksums

### Rolling Back
```bash
# Rollback to a specific tag
mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0.0

# Rollback specific number of changesets
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

## Adding New Changes

### 1. Create a new changeset file
```bash
# Create v1.1.0 changeset
touch src/main/resources/db/changelog/changesets/v1.1.0/01-add-indexes.yaml
```

### 2. Add to master changelog
```yaml
# db.changelog-master.yaml
  - include:
      file: changesets/v1.1.0/01-add-indexes.yaml
      relativeToChangelogFile: true
```

### 3. Include rollback in each changeset
```yaml
  - changeSet:
      id: add-index-on-email
      author: alerta-mujer-team
      changes:
        - createIndex:
            schemaName: identity
            tableName: users
            indexName: idx_users_email
            columns:
              - column:
                  name: email
      rollback:
        - dropIndex:
            schemaName: identity
            tableName: users
            indexName: idx_users_email
```

## Benefits Over init.sql

1. **Version Control**: Each change is tracked with ID, author, and checksum
2. **Safe Rollback**: All changes can be reverted safely
3. **Environment-Specific**: Seed data only in dev/test environments
4. **Concurrent Safe**: Locking prevents multiple instances from running migrations simultaneously
5. **Incremental**: New changes can be added without touching existing ones
6. **Audit Trail**: DATABASECHANGELOG table tracks all executed changes
7. **Team Collaboration**: Changesets can be merged without conflicts
8. **Native Table Management**: Tables without JPA entities are still version-controlled

## Best Practices

1. **One changeset per logical change** (e.g., one table creation)
2. **Always include rollback** statements
3. **Use meaningful IDs** that describe the change
4. **Keep changesets small** and focused
5. **Separate DDL from DML** into different files
6. **Test migrations** in development before production
7. **Never modify existing changesets** - create new ones instead
8. **Use contexts** for environment-specific data

## Troubleshooting

### Migration Failed Midway
1. Check `DATABASECHANGELOG` to see which changesets succeeded
2. Fix the issue in the failed changeset
3. Liquibase will skip completed changesets on next run

### Checksum Validation Failed
If you modify an already-executed changeset:
1. Use `liquibase.clearCheckSums` to clear checksums (not recommended in production)
2. Better: Create a new changeset with the fix instead

### Context Not Working
Ensure you set the context in application.properties or as a command-line argument:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.liquibase.contexts=dev
```

## Next Steps

1. **Test migrations**: Run the application in development mode
2. **Verify schema**: Check that all 27 tables are created correctly
3. **Test rollback**: Verify rollback statements work correctly
4. **Add indexes**: Create v1.1.0 changeset for performance optimization
5. **Add triggers**: Create v1.1.1 changeset for audit triggers
6. **Configure RLS**: Create v1.1.2 changeset for Row-Level Security
