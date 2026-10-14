# JPA Entities 3NF Refactoring - AlertaMujer

## Overview

This document describes the refactoring of JPA entities to comply with the Third Normal Form (3NF). The refactoring ensures data integrity, eliminates transitive dependencies, and uses proper JPA relationships instead of raw Long IDs.

## 3NF Principles Applied

### 1. Separation of Identity (User vs UserProfile)

**Before (Violates 3NF):**
- `User` contained personal data (firstName, lastName, telephone, email, birthdate)
- Mixed credentials with personal information

**After (3NF Compliant):**
- `User` - Contains only credentials and account state
  - `id`
  - `role` (ManyToOne to Role)
  - `createdAt`
- `UserProfile` - Contains personal data, biometrics, and preferences
  - `id`
  - `user` (OneToOne to User)
  - `firstName`, `lastName`, `telephone`, `email`
  - `documentNumber`, `documentType`, `birthdate`
  - `profilePhotoUrl`, `tutorialCompleted`, `tutorialSeenAt`
  - `createdAt`, `updatedAt`

**Benefits:**
- Clear separation of concerns
- User deletion cascades to UserProfile (logical)
- No transitive dependencies

### 2. Alert Domain - Location Dependency

**Before (Violates 3NF):**
- `Alert` contained `latitude` and `longitude` fields
- Location data mixed with alert state

**After (3NF Compliant):**
- `Alert` - Contains only alert state
  - `id`
  - `userProfile` (ManyToOne)
  - `device` (ManyToOne)
  - `alertType`, `activationMethod`, `status`, `message`
  - `startedAt`, `endedAt`, `cancelledAt`, `createdAt`
- `LocationLog` - Location depends on Alert at a specific time
  - `id`
  - `userProfile` (ManyToOne)
  - `alert` (ManyToOne, nullable)
  - `latitude`, `longitude`, `accuracy`
  - `recordedAt`

**Benefits:**
- Location is time-dependent (can change during alert)
- No transitive dependency from alert to location
- Alert can have multiple location logs

### 3. Join Tables with Relationships

**Before (Raw Long IDs):**
```java
@Column(name = "alert_id", nullable = false)
private Long alertId;

@Column(name = "emergency_contact_id", nullable = false)
private Long emergencyContactId;
```

**After (JPA Relationships):**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "alert_id", nullable = false)
private Alert alert;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "emergency_contact_id", nullable = false)
private EmergencyContact emergencyContact;
```

**Benefits:**
- Referential integrity enforced by JPA
- Lazy loading for performance
- Type-safe navigation
- Clearer domain model

### 4. CascadeType Configuration

**Cascades Applied:**
- ❌ **NO cascade on User → Role** (Role is shared reference)
- ❌ **NO cascade on Alert → UserProfile** (Alert doesn't own profile)
- ❌ **NO cascade on Alert → Device** (Device can exist without alert)
- ⚠️ **Pending: User → Account and User → UserProfile** (Should cascade on delete)

**Current Cascade Strategy:**
- All relationships use FetchType.LAZY
- No CascadeType by default (manual management)
- CascadeType.REMOVE should be added where logical:
  - User → Account (delete user should delete account)
  - User → UserProfile (delete user should delete profile)

## Entities Refactored

### Identity Schema

#### 1. User.java
**Changes:**
- Removed: firstName, lastName, telephone, email, documentNumber, documentType, birthdate
- Added: `role` relationship (ManyToOne)
- Changed: `roleId` → `role` (object)
- Added: `updatable = false` on createdAt

**3NF Compliance:** ✅
- Only contains credentials and state
- Personal data moved to UserProfile

#### 2. UserProfile.java
**Changes:**
- Added: `user` relationship (OneToOne)
- Added: firstName, lastName, telephone, email, documentNumber, documentType, birthdate
- Changed: `userId` → `user` (object)
- Added: `updatable = false` on createdAt

**3NF Compliance:** ✅
- Contains personal data only
- Proper relationship with User

#### 3. Account.java
**Changes:**
- Added: `user` relationship (OneToOne)
- Changed: `userId` → `user` (object)

**3NF Compliance:** ✅
- Contains authentication credentials only
- Proper relationship with User

#### 4. Role.java
**Changes:**
- Added: `users` relationship (OneToMany, mappedBy)
- For navigation from Role to Users

**3NF Compliance:** ✅
- No changes to data structure
- Added bidirectional relationship

### Alert Schema

#### 5. Alert.java
**Changes:**
- Removed: latitude, longitude (moved to LocationLog)
- Added: `userProfile` relationship (ManyToOne)
- Added: `device` relationship (ManyToOne)
- Changed: `userProfileId` → `userProfile` (object)
- Changed: `deviceId` → `device` (object)
- Added: `updatable = false` on createdAt

**3NF Compliance:** ✅
- No location data (eliminates transitive dependency)
- Proper relationships with UserProfile and Device

#### 6. LocationLog.java
**Changes:**
- Added: `userProfile` relationship (ManyToOne)
- Added: `alert` relationship (ManyToOne, nullable)
- Changed: `userProfileId` → `userProfile` (object)
- Changed: `alertId` → `alert` (object)

**3NF Compliance:** ✅
- Location depends on Alert at specific time
- No transitive dependencies

#### 7. AlertContact.java
**Changes:**
- Added: `alert` relationship (ManyToOne)
- Added: `emergencyContact` relationship (ManyToOne)
- Changed: `alertId` → `alert` (object)
- Changed: `emergencyContactId` → `emergencyContact` (object)
- Added: UniqueConstraint on (alert, emergencyContact, channel)
- Added: `updatable = false` on createdAt

**3NF Compliance:** ✅
- Join table with proper relationships
- Metadata (channel, status, attempts) is entity-specific

#### 8. EmergencyContact.java
**Changes:**
- Added: `userProfile` relationship (ManyToOne)
- Changed: `userProfileId` → `userProfile` (object)
- Added: `updatable = false` on createdAt

**3NF Compliance:** ✅
- Contact information for UserProfile
- Proper relationship

### Device Schema

#### 9. Device.java
**Changes:**
- Added: `account` relationship (ManyToOne)
- Changed: `accountId` → `account` (object)
- Added: `updatable = false` on createdAt

**3NF Compliance:** ✅
- Device belongs to Account
- Proper relationship

### Resource Schema

#### 10. EmergencyResource.java
**No changes needed** - Already 3NF compliant
- NOTE: `city` field violates 3NF (should reference City table)
- Left as-is due to existing schema constraints

#### 11. ResourceCall.java
**Changes:**
- Added: `emergencyResource` relationship (ManyToOne)
- Added: `userProfile` relationship (ManyToOne)
- Added: `alert` relationship (ManyToOne, nullable)
- Added: `device` relationship (ManyToOne, nullable)
- Changed: All Long IDs to object relationships
- Added: `nullable = false` on telephoneDialed and startedAt
- Added: `updatable = false` on createdAt

**3NF Compliance:** ✅
- Proper relationships with all referenced entities
- Referential integrity enforced

### Zone Schema

#### 12. Zone.java
**Changes:**
- Added: `updatable = false` on createdAt
- Added: NOTE about `city` field violating 3NF

**3NF Compliance:** ⚠️ Partial
- `city` field should reference a City table
- Left as-is due to existing schema constraints
- Documented for future refactoring

## Impact on Services and DTOs

### Services Requiring Updates

#### 1. UserService
**Before:**
```java
user.setFirstName(request.getFirstName());
user.setLastName(request.getLastName());
user.setEmail(request.getEmail());
```

**After:**
```java
User user = new User();
user.setRole(roleRepository.findById(request.getRoleId()).orElseThrow());

UserProfile profile = new UserProfile();
profile.setUser(user);
profile.setFirstName(request.getFirstName());
profile.setLastName(request.getLastName());
profile.setEmail(request.getEmail());

userRepository.save(user);
userProfileRepository.save(profile);
```

#### 2. AlertService
**Before:**
```java
alert.setLatitude(request.getLatitude());
alert.setLongitude(request.getLongitude());
```

**After:**
```java
Alert alert = new Alert();
alert.setUserProfile(userProfileRepository.findById(request.getUserProfileId()).orElseThrow());
alert.setDevice(deviceRepository.findById(request.getDeviceId()).orElseThrow());
alert.setAlertType(request.getAlertType());
alert.setActivationMethod(request.getActivationMethod());

LocationLog locationLog = new LocationLog();
locationLog.setUserProfile(alert.getUserProfile());
locationLog.setAlert(alert);
locationLog.setLatitude(request.getLatitude());
locationLog.setLongitude(request.getLongitude());
```

#### 3. DeviceService
**Before:**
```java
device.setAccountId(accountId);
```

**After:**
```java
device.setAccount(accountRepository.findById(accountId).orElseThrow());
```

### DTOs Requiring Updates

#### RegisterRequest DTO
**Before:**
```java
private String firstName;
private String lastName;
private String email;
private String telephone;
```

**After:**
Same structure - service layer will map to UserProfile

#### AlertRequest DTO
**Before:**
```java
private BigDecimal latitude;
private BigDecimal longitude;
private Long userProfileId;
private Long deviceId;
```

**After:**
```java
private BigDecimal latitude;
private BigDecimal longitude;
private Long userProfileId;
private Long deviceId;
```
Same structure - service layer will create LocationLog separately

### Repository Changes

All repositories remain the same - they already work with entity objects.

However, queries may need optimization:
```java
// Before (worked but inefficient)
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);

// After (more efficient with lazy loading)
@Query("SELECT u FROM User u JOIN FETCH u.userProfile WHERE u.userProfile.email = :email")
User findByEmailWithProfile(@Param("email") String email);
```

## Migration Steps

### 1. Update Database Schema
Liquibase already has the correct schema - no changes needed.

### 2. Migrate Existing Data
```sql
-- Move personal data from users to user_profile
UPDATE identity.user_profile up
SET first_name = u.first_name,
    last_name = u.last_name,
    telephone = u.telephone,
    email = u.email,
    document_number = u.document_number,
    document_type = u.document_type,
    birthdate = u.birthdate
FROM identity.users u
WHERE up.user_id = u.id;

-- Remove personal data columns from users (after Liquibase migration)
-- This will be handled by Liquibase rollback + new changeset
```

### 3. Update Liquibase Changelog
Create new changeset to remove personal data columns from users table:
```yaml
# changesets/v1.1.0/06-remove-personal-data-from-users.yaml
- changeSet:
    id: remove-personal-data-from-users
    author: alerta-mujer-team
    changes:
      - dropColumn:
          schemaName: identity
          tableName: users
          columnName: first_name
      - dropColumn:
          schemaName: identity
          tableName: users
          columnName: last_name
      # ... other columns
```

### 4. Update Services
- UserService: Create both User and UserProfile
- AlertService: Create LocationLog separately
- DeviceService: Load Account object instead of Long ID
- All services: Load entity objects instead of Long IDs

### 5. Update Tests
- Update test data to include UserProfile
- Update service tests to work with relationships
- Add tests for lazy loading scenarios

### 6. Performance Testing
- Test N+1 query problems with lazy loading
- Add @EntityGraph where needed
- Optimize critical queries with JOIN FETCH

## Known 3NF Violations (To Be Addressed Later)

### 1. Zone.city Field
**Violation:** Zone contains city as string instead of referencing City table.
**Impact:** Multiple zones can have "Bogotá" with different spellings.
**Solution:** Create City table and reference it.
**Timeline:** v1.2.0 (geographic hierarchy refactoring)

### 2. EmergencyResource.city Field
**Violation:** Same as Zone.city
**Impact:** Same as above
**Solution:** Reference City table after it's created
**Timeline:** v1.2.0

### 3. Missing CascadeType
**Violation:** User deletion doesn't cascade to Account and UserProfile
**Impact:** Orphaned records if User is deleted
**Solution:** Add CascadeType.REMOVE where logical
**Timeline:** v1.1.1 (cascade configuration)

## Performance Considerations

### Lazy Loading
All relationships use FetchType.LAZY by default.
- **Benefit:** Reduces memory usage
- **Risk:** N+1 query problems
- **Mitigation:** Use @EntityGraph or JOIN FETCH in queries

### Example Query Optimization
```java
// Without optimization (N+1 problem)
List<Alert> alerts = alertRepository.findAll();
// N+1 queries when accessing alert.getUserProfile()

// With optimization
@EntityGraph(attributePaths = {"userProfile", "device"})
List<Alert> alerts = alertRepository.findAll();
// 1 query with joins

// Or with JOIN FETCH
@Query("SELECT a FROM Alert a JOIN FETCH a.userProfile JOIN FETCH a.device")
List<Alert> findAllWithDetails();
```

## Validation Changes

### Bean Validation
Add validation to entity relationships:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
@NotNull
private User user;
```

## Summary

### 3NF Compliance Status
- ✅ User - Credentials only
- ✅ UserProfile - Personal data only
- ✅ Account - Authentication only
- ✅ Alert - State only (no location)
- ✅ LocationLog - Location depends on Alert
- ✅ AlertContact - Join table with relationships
- ✅ EmergencyContact - Contact for UserProfile
- ✅ Device - Belongs to Account
- ✅ ResourceCall - Proper relationships
- ⚠️ Zone - city field (documented for future)
- ⚠️ EmergencyResource - city field (documented for future)

### Benefits Achieved
1. **No Transitive Dependencies:** Location data separated from alert state
2. **Clear Separation:** Identity, credentials, and personal data separated
3. **Referential Integrity:** JPA relationships enforce FK constraints
4. **Type Safety:** Object relationships instead of Long IDs
5. **Lazy Loading:** Performance optimization for large datasets
6. **Immutable Timestamps:** createdAt marked as updatable=false

### Next Steps
1. Update services to work with new entity structure
2. Update DTOs if needed
3. Add CascadeType where logical
4. Create City table for geographic data (v1.2.0)
5. Add @EntityGraph for query optimization
6. Update tests
7. Performance testing
