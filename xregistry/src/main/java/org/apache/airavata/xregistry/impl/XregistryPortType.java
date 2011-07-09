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
/* DO NOT MODIFY!!!! This file was generated automatically by xwsdlc (version 2.8.2) */
package org.apache.airavata.xregistry.impl;
import javax.xml.namespace.QName;

import org.apache.airavata.xregistry.XregistryException;

public interface XregistryPortType {
    public final static QName XWSDLC_PORTTYPE_QNAME = new QName("http://extreme.indiana.edu/xregistry2/2007_02_21", "IXregistryPortType");
    public xregistry.generated.GetAbstractWsdlResponseDocument getAbstractWsdl(xregistry.generated.GetAbstractWsdlDocument input)throws XregistryException;
    public xregistry.generated.GetAppDescResponseDocument getAppDesc(xregistry.generated.GetAppDescDocument input)throws XregistryException;
    public xregistry.generated.GetConcreateWsdlResponseDocument getConcreateWsdl(xregistry.generated.GetConcreateWsdlDocument input)throws XregistryException;
    public xregistry.generated.GetHostDescResponseDocument getHostDesc(xregistry.generated.GetHostDescDocument input)throws XregistryException;
    public xregistry.generated.GetServiceDescResponseDocument getServiceDesc(xregistry.generated.GetServiceDescDocument input)throws XregistryException;
    public xregistry.generated.RegisterAppDescResponseDocument registerAppDesc(xregistry.generated.RegisterAppDescDocument input)throws XregistryException;
    public xregistry.generated.RegisterConcreteWsdlResponseDocument registerConcreteWsdl(xregistry.generated.RegisterConcreteWsdlDocument input)throws XregistryException;
    public xregistry.generated.RegisterHostDescResponseDocument registerHostDesc(xregistry.generated.RegisterHostDescDocument input)throws XregistryException;
    public xregistry.generated.RegisterServiceDescResponseDocument registerServiceDesc(xregistry.generated.RegisterServiceDescDocument input)throws XregistryException;
    public xregistry.generated.App2HostsResponseDocument app2Hosts(xregistry.generated.App2HostsDocument input)throws XregistryException;
    public xregistry.generated.FindHostsResponseDocument findHosts(xregistry.generated.FindHostsDocument input)throws XregistryException;
    public xregistry.generated.FindServiceDescResponseDocument findServiceDesc(xregistry.generated.FindServiceDescDocument input)throws XregistryException;
    public xregistry.generated.FindServiceInstanceResponseDocument findServiceInstance(xregistry.generated.FindServiceInstanceDocument input)throws XregistryException;
    public xregistry.generated.ListGroupsResponseDocument listGroups(xregistry.generated.ListGroupsDocument input)throws XregistryException;
    public xregistry.generated.ListGroupsGivenAUserResponseDocument listGroupsGivenAUser(xregistry.generated.ListGroupsGivenAUserDocument input)throws XregistryException;
    public xregistry.generated.ListUsersResponseDocument listUsers(xregistry.generated.ListUsersDocument input)throws XregistryException;
    public xregistry.generated.ListSubActorsGivenAGroupResponseDocument listSubActorsGivenAGroup(xregistry.generated.ListSubActorsGivenAGroupDocument input)throws XregistryException;
    public xregistry.generated.FindAppDescResponseDocument findAppDesc(xregistry.generated.FindAppDescDocument input)throws XregistryException;
    public xregistry.generated.AddCapabilityResponseDocument addCapability(xregistry.generated.AddCapabilityDocument input)throws XregistryException;
    public xregistry.generated.AddCapabilityTokenResponseDocument addCapabilityToken(xregistry.generated.AddCapabilityTokenDocument input)throws XregistryException;
    public xregistry.generated.AddUsertoGroupResponseDocument addUsertoGroup(xregistry.generated.AddUsertoGroupDocument input)throws XregistryException;
    public xregistry.generated.AddGrouptoGroupResponseDocument addGrouptoGroup(xregistry.generated.AddGrouptoGroupDocument input)throws XregistryException;
    public xregistry.generated.CreateGroupResponseDocument createGroup(xregistry.generated.CreateGroupDocument input)throws XregistryException;
    public xregistry.generated.CreateUserResponseDocument createUser(xregistry.generated.CreateUserDocument input)throws XregistryException;
    public xregistry.generated.DeleteGroupResponseDocument deleteGroup(xregistry.generated.DeleteGroupDocument input)throws XregistryException;
    public xregistry.generated.DeleteUserResponseDocument deleteUser(xregistry.generated.DeleteUserDocument input)throws XregistryException;
    public xregistry.generated.GetCapabilityResponseDocument getCapability(xregistry.generated.GetCapabilityDocument input)throws XregistryException;
    public xregistry.generated.RemoveAppDescResponseDocument removeAppDesc(xregistry.generated.RemoveAppDescDocument input)throws XregistryException;
    public xregistry.generated.RemoveCapabilityResponseDocument removeCapability(xregistry.generated.RemoveCapabilityDocument input)throws XregistryException;
    public xregistry.generated.RemoveConcreteWsdlResponseDocument removeConcreteWsdl(xregistry.generated.RemoveConcreteWsdlDocument input)throws XregistryException;
    public xregistry.generated.RemoveGroupFromGroupResponseDocument removeGroupFromGroup(xregistry.generated.RemoveGroupFromGroupDocument input)throws XregistryException;
    public xregistry.generated.RemoveHostDescResponseDocument removeHostDesc(xregistry.generated.RemoveHostDescDocument input)throws XregistryException;
    public xregistry.generated.RemoveServiceDescResponseDocument removeServiceDesc(xregistry.generated.RemoveServiceDescDocument input)throws XregistryException;
    public xregistry.generated.RemoveUserFromGroupResponseDocument removeUserFromGroup(xregistry.generated.RemoveUserFromGroupDocument input)throws XregistryException;
    public xregistry.generated.IsAuthorizedToAcssesResponseDocument isAuthorizedToAcsses(xregistry.generated.IsAuthorizedToAcssesDocument input)throws XregistryException;
    public xregistry.generated.AddResourceResponseDocument addResource(xregistry.generated.AddResourceDocument input)throws XregistryException;
    public xregistry.generated.RemoveResourceResponseDocument removeResource(xregistry.generated.RemoveResourceDocument input)throws XregistryException;
    public xregistry.generated.FindResourceResponseDocument findResource(xregistry.generated.FindResourceDocument input)throws XregistryException;
    public xregistry.generated.GetResourceResponseDocument getResource(xregistry.generated.GetResourceDocument input)throws XregistryException;
    public xregistry.generated.AddOGCEResourceResponseDocument addOGCEResource(xregistry.generated.AddOGCEResourceDocument input)throws XregistryException;
    public xregistry.generated.RemoveOGCEResourceResponseDocument removeOGCEResource(xregistry.generated.RemoveOGCEResourceDocument input)throws XregistryException;
    public xregistry.generated.FindOGCEResourceResponseDocument findOGCEResource(xregistry.generated.FindOGCEResourceDocument input)throws XregistryException;
    public xregistry.generated.GetOGCEResourceResponseDocument getOGCEResource(xregistry.generated.GetOGCEResourceDocument input)throws XregistryException;

}

