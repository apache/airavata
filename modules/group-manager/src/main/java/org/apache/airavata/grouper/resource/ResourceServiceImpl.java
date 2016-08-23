/**
 *
 */
package org.apache.airavata.grouper.resource;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.*;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import org.apache.airavata.grouper.AiravataGrouperUtil;
import org.apache.airavata.grouper.SubjectType;
import org.apache.airavata.grouper.group.GroupServiceImpl;
import org.apache.airavata.grouper.permission.PermissionAction;
import org.apache.airavata.grouper.permission.PermissionServiceImpl;
import org.apache.airavata.grouper.role.RoleServiceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.airavata.grouper.AiravataGrouperUtil.*;
import static org.apache.airavata.grouper.permission.PermissionAction.READ;
import static org.apache.airavata.grouper.permission.PermissionAction.WRITE;
import static org.apache.airavata.grouper.resource.ResourceType.*;

/**
 * @author vsachdeva
 *
 */
public class ResourceServiceImpl {


  //TODO: break this method into smaller methods
  public void createResource(Resource resource) throws ResourceNotFoundException {

    validateResource(resource);

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      AttributeDefName parentAttributeDefName = null;

      // make sure that the parent resource exists in grouper if it is in the request
      if (resource.getParentResourceId() != null) {
        parentAttributeDefName = AttributeDefNameFinder.findByName(resource.getResourceType().getParentResoruceType()
            .getStemFromResourceType()+COLON+resource.getParentResourceId(), false);
        if (parentAttributeDefName == null) {
          throw new ResourceNotFoundException(resource.getParentResourceId() +" was not found.");
        }
      }

      Subject subject = SubjectFinder.findByIdAndSource(resource.getOwnerId(), SUBJECT_SOURCE, false);
      if (subject == null) {
        throw new IllegalArgumentException("Resource owner id "+resource.getOwnerId()+" could not be found.");
      }

      // create an attribute def if doesn't exist
      AttributeDef attributeDef = AttributeDefFinder.findByName(PERMISSIONS_ATTRIBUTE_DEF, false);
      if (attributeDef == null) {
        AttributeDefSave attributeDefSave = new AttributeDefSave(grouperSession);
        attributeDef = attributeDefSave.assignAttributeDefType(AttributeDefType.perm).assignToGroup(true)
          .assignToEffMembership(true).assignName(PERMISSIONS_ATTRIBUTE_DEF).assignCreateParentStemsIfNotExist(true)
          .assignSaveMode(SaveMode.INSERT_OR_UPDATE).save();
        AttributeAssignAction read = attributeDef.getAttributeDefActionDelegate().addAction(READ.name());
        AttributeAssignAction write = attributeDef.getAttributeDefActionDelegate().addAction(WRITE.name());
        write.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(read);
      }

      // create attribute def name
      AttributeDefNameSave attributeDefNameSave = new AttributeDefNameSave(grouperSession, attributeDef);
      attributeDefNameSave.assignCreateParentStemsIfNotExist(true);
      attributeDefNameSave.assignSaveMode(SaveMode.INSERT_OR_UPDATE);
      attributeDefNameSave.assignAttributeDefNameNameToEdit(resource.getResourceType().getStemFromResourceType()+COLON+resource.getId());
      attributeDefNameSave.assignName(resource.getResourceType().getStemFromResourceType()+COLON+resource.getId());
      attributeDefNameSave.assignDescription(resource.getDescription());
      attributeDefNameSave.assignDisplayName(resource.getName());
      AttributeDefName attributeDefName = attributeDefNameSave.save();

      // set the inheritance if parent attribute def name is not null
      if (parentAttributeDefName != null) {
        parentAttributeDefName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attributeDefName);
      }

      RoleServiceImpl roleService = new RoleServiceImpl();
      //TODO remove the session being passed
      Group readRole = roleService.createRole(resource.getId()+"_"+READ.name(), grouperSession);
      Group writeRole = roleService.createRole(resource.getId()+"_"+WRITE.name(), grouperSession);

      readRole.getPermissionRoleDelegate().assignRolePermission(READ.name(), attributeDefName, PermissionAllowed.ALLOWED);
      writeRole.getPermissionRoleDelegate().assignRolePermission(WRITE.name(), attributeDefName, PermissionAllowed.ALLOWED);
      writeRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(readRole);

      // give the write role to ownerId
      roleService.assignRoleToUser(resource.getOwnerId(), resource.getId()+"_"+WRITE.name(), grouperSession);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public void deleteResource(String resourceId, ResourceType resourceType) throws ResourceNotFoundException {
    if (resourceId == null || resourceType == null) {
      throw new IllegalArgumentException("resouceId "+resourceId+" is null or resourceType"+resourceType+" is null.");
    }
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(resourceType.getStemFromResourceType()+COLON+resourceId, false);
      if (attributeDefName == null) {
        throw new ResourceNotFoundException(resourceId +" was not found.");
      }
      RoleServiceImpl roleService = new RoleServiceImpl();
      // delete all the children resources and roles
      for (AttributeDefName childAttributeDefName: attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesImpliedByThis()) {
        childAttributeDefName.delete();
        // don't change the order since write inherits read
        roleService.deleteRole(childAttributeDefName.getExtension()+"_"+WRITE.name(), grouperSession);
        roleService.deleteRole(childAttributeDefName.getExtension()+"_"+READ.name(), grouperSession);
      }
      attributeDefName.delete();
      // don't change the order since write inherits read
      roleService.deleteRole(resourceId+"_"+WRITE.name(), grouperSession);
      roleService.deleteRole(resourceId+"_"+READ.name(), grouperSession);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public Resource getResource(String resourceId, ResourceType resourceType) throws ResourceNotFoundException {
    if (resourceId == null || resourceType == null) {
      throw new IllegalArgumentException("resouceId "+resourceId+" is null or resourceType"+resourceType+" is null.");
    }
    GrouperSession grouperSession = null;
    Resource resource = null;
    try {
      grouperSession = GrouperSession.startRootSession();
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(resourceType.getStemFromResourceType()+COLON+resourceId, false);
      if (attributeDefName == null) {
        throw new ResourceNotFoundException(resourceId +" was not found.");
      }
      resource = new Resource(resourceId, resourceType);
      resource.setDescription(attributeDefName.getDescription());
      resource.setName(attributeDefName.getDisplayExtension());
      Set<AttributeDefName> parentAttributeDefNames = attributeDefName.getAttributeDefNameSetDelegate().getAttributeDefNamesThatImplyThisImmediate();
      if (parentAttributeDefNames != null && parentAttributeDefNames.size() > 0) {
        resource.setParentResourceId(parentAttributeDefNames.iterator().next().getExtension());
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    return resource;
  }

  /**
   *
   * @param userId
   * @param resourceType
   * @param actions - write or read
   * @param pageNumber - 1 index based
   * @param pageSize - items to fetch
   * @return
   * @throws SubjectNotFoundException
   */
  public Set<Resource> getAccessibleResourcesForUser(String userId, ResourceType resourceType,
      PermissionAction action, boolean pagination, Integer pageNumber, Integer pageSize) throws SubjectNotFoundException {

    if (userId == null || resourceType == null || action == null) {
      throw new IllegalArgumentException("Invalid input");
    }
    if (pagination && (pageNumber < 0 || pageSize < 1)) {
      throw new IllegalArgumentException("Invalid pagination properties");
    }

    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.startRootSession();

      PermissionFinder permissionFinder = new PermissionFinder();
      permissionFinder.addPermissionDef(PERMISSIONS_ATTRIBUTE_DEF);
      permissionFinder.addAction(action.name());
      Subject subject = SubjectFinder.findByIdAndSource(userId, SUBJECT_SOURCE, false);
      if (subject == null) {
        throw new SubjectNotFoundException("userId "+userId+" was not found.");
      }
      permissionFinder.addSubject(subject);

      Stem stem = StemFinder.findByName(grouperSession, resourceType.getStemFromResourceType(), true);
      permissionFinder.assignPermissionNameFolder(stem);
      permissionFinder.assignPermissionNameFolderScope(Scope.ONE);
      if (pagination) {
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.paging(pageSize, pageNumber, false);
        permissionFinder.assignQueryOptions(queryOptions);
      }
      Set<PermissionEntry> permissions = permissionFinder.findPermissions();

      Set<Resource> resources = new HashSet<Resource>();
      for (PermissionEntry entry: permissions) {
        Resource resource = new Resource(entry.getAttributeDefName().getExtension(), resourceType);
        resource.setName(entry.getAttributeDefNameDispName());
        resources.add(resource);
      }
      return resources;

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  // action can be read or write only
  public Set<String> getAllAccessibleUsers(String resourceId, ResourceType resourceType, PermissionAction action) {

    if (resourceId == null || resourceType == null || action == null) {
      throw new IllegalArgumentException("Invalid input");
    }

    GrouperSession grouperSession = null;
    Set<String> userIds = new HashSet<String>();
    try {
      grouperSession = GrouperSession.startRootSession();

      PermissionFinder permissionFinder = new PermissionFinder();
      permissionFinder.addPermissionDef(PERMISSIONS_ATTRIBUTE_DEF);
      permissionFinder.addAction(action.name());

      Stem stem = StemFinder.findByName(grouperSession, resourceType.getStemFromResourceType(), true);
      permissionFinder.assignPermissionNameFolder(stem);
      permissionFinder.assignPermissionNameFolderScope(Scope.ONE);
      permissionFinder.addRole(AiravataGrouperUtil.ROLES_STEM_NAME+ ":" + resourceId + "_" + action.toString());
      Set<PermissionEntry> permissions = permissionFinder.findPermissions();

      for (PermissionEntry entry: permissions) {
        if (entry.getSubjectSourceId().equals(SUBJECT_SOURCE)) {
          userIds.add(entry.getSubjectId());
        }
      }

      return userIds;

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  private void validateResource(Resource resource) {
    if (resource.getResourceType() == null) {
      throw new IllegalArgumentException("Resource type is a required field");
    }
    if ((resource.getResourceType().equals(EXPERIMENT) ||  resource.getResourceType().equals(DATA)) && resource.getParentResourceId() == null) {
      throw new IllegalArgumentException("Resource type Experiment or Data must provide valid parent resource id");
    }
    if (resource.getOwnerId() == null) {
      throw new IllegalArgumentException("Resource ownerId is a required field.");
    }
  }

  public static void main(String[] args) {
    ResourceServiceImpl resourceService = new ResourceServiceImpl();

    // create a Project resource
    Resource projectResource = new Resource("project resource id", PROJECT);
    projectResource.setDescription("project resource description");
    projectResource.setName("project resource name");
    projectResource.setOwnerId("airavata_id_1");
    resourceService.createResource(projectResource);

    // create an Experiment resource
    Resource experimentResource = new Resource("experiment resource id", EXPERIMENT);
    experimentResource.setDescription("experiment resource description");
    experimentResource.setName("experiment resource name");
    experimentResource.setParentResourceId("project resource id");
    experimentResource.setOwnerId("airavata_id_1");
    resourceService.createResource(experimentResource);

    //create another experiment resource within the same project resource
    Resource experimentResource1 = new Resource("experiment resource id1", ResourceType.EXPERIMENT);
    experimentResource1.setDescription("experiment resource description1");
    experimentResource1.setName("experiment resource name1");
    experimentResource1.setParentResourceId("project resource id");
    experimentResource1.setOwnerId("airavata_id_1");
    resourceService.createResource(experimentResource1);

    // create a data file resource
    Resource dataResource = new Resource("data resource id", ResourceType.DATA);
    dataResource.setDescription("data resource description");
    dataResource.setName("data resource name");
    dataResource.setParentResourceId("experiment resource id1");
    dataResource.setOwnerId("airavata_id_1");
    resourceService.createResource(dataResource);

    // get the experiment resource and it should have parent set to project
    Resource resource = resourceService.getResource("experiment resource id1", EXPERIMENT);
    System.out.println(resource);

    Set<Resource> accessibleResourcesForUser = resourceService.getAccessibleResourcesForUser("airavata_id_1", EXPERIMENT, WRITE, true, 1, 2);
    System.out.println("accessible resources on page 1 are "+accessibleResourcesForUser.size());


    //share the experiment with airavata_id_2
    PermissionServiceImpl permissionService = new PermissionServiceImpl();
    permissionService.grantPermission("airavata_id_2", SubjectType.PERSON, "experiment resource id1", EXPERIMENT, WRITE);

    // create a group of users
    GroupServiceImpl groupService = new GroupServiceImpl();
    org.apache.airavata.grouper.group.Group group = new org.apache.airavata.grouper.group.Group("airavata test group id", "airavata_id_1");
    group.setName("airavata test group name");
    group.setDescription("airavata test group description");
    List<String> members = new ArrayList<String>();
    members.add("airavata_id_3");
    members.add("airavata_id_4");
    group.setMembers(members);
    groupService.createGroup(group);

    // now share the same experiment with this group as well
    permissionService.grantPermission("airavata test group id", SubjectType.GROUP, "experiment resource id1", EXPERIMENT, READ);

    accessibleResourcesForUser = resourceService.getAccessibleResourcesForUser("airavata_id_3", EXPERIMENT, READ, true, 1, 2);
    System.out.println("accessible resources on page 1 are "+accessibleResourcesForUser.size());

    // get all resources, or no pagination
    accessibleResourcesForUser = resourceService.getAccessibleResourcesForUser("airavata_id_1", EXPERIMENT, READ, false, 1, 2);
    System.out.println("accessible resources without pagination are "+accessibleResourcesForUser.size());

    Set<String> allAccessibleUsers = resourceService.getAllAccessibleUsers("experiment resource id1", EXPERIMENT, READ);
    System.out.println("users who have read access on experiment resource id1 are "+allAccessibleUsers);

    //delete the project resource, it will delete all the children/experiment resources and roles as well
    resourceService.deleteResource("project resource id", PROJECT);
  }

}
