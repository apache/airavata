/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.airavata.xregistry.impl;

import java.sql.Timestamp;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.XregistryConstants.Action;
import org.apache.airavata.xregistry.XregistryConstants.DocType;
import org.apache.airavata.xregistry.cap.CapabilityRegistry;
import org.apache.airavata.xregistry.cap.CapabilityRegistryImpl;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.doc.AppData;
import org.apache.airavata.xregistry.doc.DocData;
import org.apache.airavata.xregistry.doc.DocumentRegistry;
import org.apache.airavata.xregistry.doc.DocumentRegistryImpl;
import org.apache.airavata.xregistry.doc.ResourceUtils;
import org.apache.airavata.xregistry.group.Group;
import org.apache.airavata.xregistry.group.GroupManager;

import xregistry.generated.CapabilityToken;
import xregistry.generated.ListSubActorsGivenAGroupResponseDocument.ListSubActorsGivenAGroupResponse.Actor;
import xsul.MLogger;

public class XregistryImpl{
    protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);
    private final GlobalContext globalContext;
    private final GroupManager groupManager;
    private final DocumentRegistry docRegistry;
    private final CapabilityRegistry capabilityRegistry;
    
    
    public XregistryImpl(GlobalContext globalContext){
        this.globalContext = globalContext;
        groupManager = globalContext.getGroupManager();
        docRegistry = new DocumentRegistryImpl(globalContext);
        capabilityRegistry = new CapabilityRegistryImpl(globalContext,groupManager);
    }
   
    private boolean authorize(String user,String resourceID,Action action) throws XregistryException{
        log.info("Request Recived for "+ resourceID + " from "+user + " for action "+ action );
        boolean isAUthorized =  globalContext.getAuthorizer().isAuthorized(user, resourceID, action);
//        if(isAUthorized){
//            log.info("Request Authorized for "+ resourceID + " from "+user + " for action "+ action );
//        }
        
        return isAUthorized;
    }
    
    
    
    /** 
     * Add/remove/ get documents
     */
    public String getAppDesc(String user, String appQName, String hostName) throws XregistryException {
        authorize(user, ResourceUtils.getResourceID(DocType.AppDesc,appQName,hostName ), Action.Read);
        return docRegistry.getAppDesc( appQName, hostName);
    }
    public String getHostDesc(String user, String hostName) throws XregistryException {
        authorize(user, ResourceUtils.getResourceID( DocType.HostDesc,hostName), Action.Read);
        return docRegistry.getHostDesc( hostName);
    }

    public String getServiceDesc(String user, String serviceQName) throws XregistryException {
        authorize(user, ResourceUtils.getResourceID(DocType.ServiceDesc,serviceQName), Action.Read);
        return docRegistry.getServiceDesc(serviceQName);
    }

    public String registerAppDesc(String user, String appDescAsStr) throws XregistryException {
        authorize(user, null, Action.AddNew);
        return docRegistry.registerAppDesc(user, appDescAsStr);
    }

    
    public String registerHostDesc(String user, String hostDescAsStr) throws XregistryException {
        authorize(user, null, Action.AddNew);
        return docRegistry.registerHostDesc(user, hostDescAsStr);
    }

    public String registerServiceDesc(String user, String serviceMapAsStr, String abstractWsdlAsString) throws XregistryException {
        authorize(user, null, Action.AddNew);
        return docRegistry.registerServiceDesc(user, serviceMapAsStr, abstractWsdlAsString);
    }

    public void removeAppDesc(String user, String appQName, String hostName) throws XregistryException {
        authorize(user, ResourceUtils.getResourceID(DocType.AppDesc,appQName,hostName), Action.Write);
        docRegistry.removeAppDesc(appQName, hostName);
        
    }


    public void removeHostDesc(String user, String hostName) throws XregistryException {
        authorize(user, ResourceUtils.getResourceID(DocType.HostDesc,hostName),Action.Write);
        docRegistry.removeHostDesc( hostName);
    }

    public void removeServiceDesc(String user, String serviceQName) throws XregistryException {
        authorize(user, ResourceUtils.getResourceID(DocType.ServiceDesc,serviceQName),Action.Write);
        docRegistry.removeServiceDesc( serviceQName);
        
    }

    public void removeConcreteWsdl(String user, String wsdlQName) throws XregistryException {
        authorize(user, ResourceUtils.getResourceID(DocType.CWsdl,wsdlQName),Action.Write);
        docRegistry.removeConcreteWsdl( wsdlQName);
        
    }
    public String registerConcreteWsdl(String user, String wsdlAsStr, int lifetimeAsSeconds) throws XregistryException {
        authorize(user, null, Action.AddNew);
        return docRegistry.registerConcreteWsdl(user, wsdlAsStr, lifetimeAsSeconds);
    }
    
    public String getConcreateWsdl(String user, String wsdlQName) throws XregistryException {
        authorize(user, ResourceUtils.getResourceID(DocType.CWsdl,wsdlQName), Action.Read);
        return docRegistry.getConcreateWsdl( wsdlQName);
    }

    public String getAbstractWsdl(String user, String wsdlQName) throws XregistryException {
        authorize(user, ResourceUtils.getResourceID(DocType.ServiceDesc,wsdlQName), Action.Read);
        return docRegistry.getAbstractWsdl( wsdlQName);
    }

    
    /**
     * Capability Management operations
     * @throws XregistryException 
     */
    
    public void removeCapability(String user,String resourceID, String actor) throws XregistryException {
        authorize(user, resourceID, Action.ResourceAdmin);
        capabilityRegistry.removeCapability(user, resourceID, actor);
    }
    
    public void addCapability(String user, String resourceID, String actor, boolean isUser, String action) throws XregistryException {
        authorize(user, resourceID, Action.ResourceAdmin);
        capabilityRegistry.addCapability(user, resourceID, actor, isUser, action);
    }
    
    public void addCapability(String user, String resourceID, String actor, boolean isUser, String action,String assertions, Timestamp notbefore, Timestamp notafter) throws XregistryException {
        authorize(user, resourceID, Action.ResourceAdmin);
        capabilityRegistry.addCapability(user, resourceID, actor, isUser, action,assertions, notbefore, notafter);  
    }
    public CapabilityToken[] getCapability(String user, String resourceID, String actor, boolean actorType, String action) throws XregistryException  {
        authorize(user, resourceID, Action.Read);
        return capabilityRegistry.getCapability(user, resourceID, actor, actorType, action);
    }

    public List<DocData>  findServiceInstance(String user,String serviceName)throws XregistryException{
        authorize(user, null, Action.Read);
        return docRegistry.findServiceInstance(user, serviceName);
    }

    public List<AppData> findAppDesc(String user, String query) throws XregistryException {
        authorize(user, null, Action.Read);
        return docRegistry.findAppDesc(user, query);
    }

    public List<DocData> findHosts(String user, String hostName) throws XregistryException {
        authorize(user, null, Action.Read);
        return docRegistry.findHosts(user,hostName);
    }

    public List<DocData> findServiceDesc(String user, String serviceName) throws XregistryException {
        authorize(user, null, Action.Read);
        return docRegistry.findServiceDesc(user, serviceName);
    }

    public void addUsertoGroup(String user,String groupName, String usertoAdded) throws XregistryException {
        authorize(user, null, Action.SysAdmin);
        groupManager.addUsertoGroup(groupName, usertoAdded);
    }

    public void createGroup(String user,String newGroup, String description) throws XregistryException {
        authorize(user, null, Action.SysAdmin);
        groupManager.createGroup(newGroup, description);
    }

    public void createUser(String user,String newUser, String description) throws XregistryException {
        authorize(user, null, Action.SysAdmin);
        groupManager.createUser(newUser, description,false);
    }

    public void deleteGroup(String user,String groupID) throws XregistryException {
        authorize(user, null, Action.SysAdmin);
        groupManager.deleteGroup(groupID);
    }

    public void deleteUser(String user,String userID) throws XregistryException {
        authorize(user, null, Action.SysAdmin);
        groupManager.deleteUser(userID);
    }

    public void addAGroupToGroup(String user,String groupName,String grouptoAddedName) throws XregistryException {
        authorize(user, null, Action.SysAdmin);
        groupManager.addGrouptoGroup(groupName, grouptoAddedName);
    }

    
    public Group getGroup(String user,String name) throws XregistryException {
        return groupManager.getGroup(name);
    }

    public String[]  listGroups(String user) throws XregistryException {
        authorize(user, null, Action.Read);
        return groupManager.listGroups();
    }

    public String[]  listGroupsGivenAUser(String user,String targetUser) throws XregistryException {
        authorize(user, null, Action.Read);
        return groupManager.listGroupsGivenAUser(targetUser);
    }

    public String[]  listUsers(String user) throws XregistryException {
        authorize(user, null, Action.Read);
        return groupManager.listUsers();
    }


    public void removeUserFromGroup(String user, String group, String usertoRemoved) throws XregistryException {
        authorize(user, null, Action.Read);
        groupManager.removeUserFromGroup(group, usertoRemoved);
        
    }

    
    public void removeGroupFromGroup(String user,String group,String grouptoRemoved)throws XregistryException{
        authorize(user, null, Action.SysAdmin);
        groupManager.removeGroupFromGroup( group, grouptoRemoved);
    }

    public String[]  app2Hosts(String user, String appName) throws XregistryException {
        authorize(user, null, Action.Read);
        return docRegistry.app2Hosts( appName);
    }

   
    
    public Actor[]  listSubActorsGivenAGroup(String user,String targetGroup) throws XregistryException {
        authorize(user, null, Action.Read);
        return groupManager.listSubActorsGivenAGroup(targetGroup);
    }

    public boolean isAuthorizedToAcsses(String user, String resourceID, String actor, String action) throws XregistryException {
        return capabilityRegistry.isAuthorizedToAcsses(resourceID, actor, action);
    }
    
    
     public void registerDocument(String user,QName resourceID,String document)throws XregistryException{
         authorize(user, null, Action.Read);
         docRegistry.registerDocument(user, resourceID, document);
     }
    
    public void removeDocument(String user,QName resourceID)throws XregistryException{
        authorize(user, null, Action.Read);
        docRegistry.removeDocument(user, resourceID);
    }
    
    public List<DocData> findDocument(String user,String query)throws XregistryException{
        authorize(user, null, Action.Read);
        return docRegistry.findDocument(user, query);
    }
    
    public String getDocument(String user,QName docName) throws XregistryException{
        authorize(user, null, Action.Read);
        return docRegistry.getDocument(user,docName);
    }
    
    public void registerOGCEResource(String user,QName resourceID,String resourceName, String resourceType, String resourceDesc, String resourceDocument, String parentTypedID)throws XregistryException{
        authorize(user, null, Action.Read);
        docRegistry.registerOGCEResource(user, resourceID,resourceName, resourceType, resourceDesc, resourceDocument, parentTypedID);
    }
   
   public void removeOGCEResource(String user,QName resourceID, String resourceType)throws XregistryException{
       authorize(user, null, Action.Read);
       docRegistry.removeOGCEResource(user, resourceID, resourceType);
   }
   
   public List<DocData> findOGCEResource(String user,String query, String resourceName,String resourceType, String parentTypedID)throws XregistryException{
       authorize(user, null, Action.Read);
       return docRegistry.findOGCEResource(user, query,resourceName,resourceType, parentTypedID);
   }
   
   public String getOGCEResource(String user,QName resourceID,String resourceType,String parentTypedID) throws XregistryException{
       authorize(user, resourceID.toString(), Action.Read);
       return docRegistry.getOGCEResource(user,resourceID,resourceType,parentTypedID);
   }
}

