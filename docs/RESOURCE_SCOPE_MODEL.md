# Resource Scope Model

## Overview

The Airavata Research Catalog uses a **two-level scope model with inferred delegation** for managing resource access and ownership.

## Scope Types

### USER Scope
- **Stored in Database**: Yes (scope = `USER`)
- **Ownership**: Resource owned by a specific user
- **Database Fields**: 
  - `RESOURCE_SCOPE = 'USER'`
  - `OWNER_ID = userId`
  - `GATEWAY_ID = gatewayId`
- **Who Can Create**: Any authenticated user
- **Access**: Only the owner can directly access (unless shared via groups)

### GATEWAY Scope
- **Stored in Database**: Yes (scope = `GATEWAY`)
- **Ownership**: Resource owned at the gateway level
- **Database Fields**:
  - `RESOURCE_SCOPE = 'GATEWAY'`
  - `OWNER_ID = NULL`
  - `GATEWAY_ID = gatewayId`
- **Who Can Create**: Gateway administrators
- **Access**: All users in the gateway can access

### DELEGATED Scope
- **Stored in Database**: **No** (inferred at runtime)
- **Ownership**: Resource accessible via group credentials but not directly owned
- **Inference Logic**: 
  - Resource has `GROUP_RESOURCE_PROFILE_ID` set
  - User is a member of that group
  - Resource is NOT directly owned by the user (scope != USER or ownerId != userId)
  - Resource is NOT directly owned by the gateway (scope != GATEWAY or gatewayId doesn't match)
- **Who Can Create**: Cannot be created directly (always inferred)
- **Access**: Users who are members of the associated group resource profile

## Database Schema

### CATALOG_RESOURCE Table
```sql
RESOURCE_SCOPE VARCHAR(50) -- Only 'USER' or 'GATEWAY'
OWNER_ID VARCHAR(255)       -- User ID for USER scope, NULL for GATEWAY scope
GROUP_RESOURCE_PROFILE_ID VARCHAR(255) -- Optional: for delegation tracking
GATEWAY_ID VARCHAR(255)    -- Always set
```

### APPLICATION_INTERFACE Table
```sql
RESOURCE_SCOPE VARCHAR(50) -- Only 'USER' or 'GATEWAY'
OWNER_ID VARCHAR(255)       -- User ID for USER scope, NULL for GATEWAY scope
GROUP_RESOURCE_PROFILE_ID VARCHAR(255) -- Optional: for delegation tracking
GATEWAY_ID VARCHAR(255)    -- Always set
```

## API Behavior

### Creating Resources

When creating a resource via API:

1. **Scope must be USER or GATEWAY** - DELEGATED cannot be set
2. **For USER scope**:
   - `ownerId` is automatically set to the authenticated user's ID
   - `groupResourceProfileId` can be optionally set for delegation tracking
3. **For GATEWAY scope**:
   - `ownerId` is set to `null`
   - Only gateway admins can create gateway-level resources
   - `groupResourceProfileId` can be optionally set for delegation tracking

### Retrieving Resources

When retrieving resources via API:

1. The service layer automatically infers DELEGATED scope
2. Resources are returned with one of three scopes:
   - `USER`: Directly owned by the requesting user
   - `GATEWAY`: Directly owned by the gateway
   - `DELEGATED`: Accessible via group but not directly owned

## Scope Inference Algorithm

```java
if (resource.scope == USER && resource.ownerId == userId) {
    return "USER";  // Directly owned by user
}
if (resource.scope == GATEWAY && resource.gatewayId == gatewayId) {
    return "GATEWAY";  // Directly owned by gateway
}
if (resource.groupResourceProfileId in userAccessibleGroups && 
    !directlyOwned) {
    return "DELEGATED";  // Accessible via group delegation
}
return resource.scope;  // Default to stored scope
```

## Resource Types

Only two resource types are supported:

1. **DATASET**: Data files and datasets
2. **REPOSITORY**: Code repositories, notebooks, models, or any version-controlled resource

The REPOSITORY type can contain:
- General code repositories (git, svn, etc.)
- Jupyter notebooks (via `notebookPath` and `jupyterServerUrl`)
- ML models (via `modelUrl`, `applicationInterfaceId`, `framework`)

## Examples

### Example 1: User Creates a Dataset
```json
POST /api/v1/rf/resources
{
  "name": "My Dataset",
  "type": "DATASET",
  "scope": "USER",
  "datasetUrl": "https://example.com/data.csv"
}
```
Result: Resource stored with `scope=USER`, `ownerId=currentUserId`

### Example 2: Gateway Admin Creates an Application
```json
POST /api/v1/applications
{
  "applicationName": "Gateway App",
  "scope": "GATEWAY"
}
```
Result: Application stored with `scope=GATEWAY`, `ownerId=null`

### Example 3: User Accesses Group Resource
User requests resources → Service finds resource with:
- `scope=USER` (stored)
- `ownerId=otherUserId` (different user)
- `groupResourceProfileId=group123` (user is member of group123)

Result: Resource returned with `scope=DELEGATED` (inferred)

## Migration Notes

The migration script `V5__Add_resource_scope.sql`:
1. Adds `RESOURCE_SCOPE` column (defaults to `USER`)
2. Adds `GROUP_RESOURCE_PROFILE_ID` column for delegation tracking
3. Migrates existing data:
   - Resources with `OWNER_ID` → `scope=USER`
   - Resources with only `GATEWAY_ID` → `scope=GATEWAY`

## Backward Compatibility

**All backward compatibility code has been removed:**
- No more NOTEBOOK/MODEL type mappings
- Only DATASET and REPOSITORY types are supported
- Scope inference is the only way to get DELEGATED scope
