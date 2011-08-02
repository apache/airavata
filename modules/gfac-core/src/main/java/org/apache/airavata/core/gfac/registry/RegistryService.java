/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.core.gfac.registry;

import javax.xml.namespace.QName;

import org.apache.airavata.core.gfac.exception.GfacException;

public interface RegistryService {

    public void registerConcreteWsdl(String wsdlAsStr, int lifetimeAsSeconds) throws GfacException;

    public String getConcreateWsdl(String wsdlQName) throws GfacException;

    public void removeConcreteWsdl(String wsdlQName) throws GfacException;

    public String getAbstractWsdl(String wsdlQName) throws GfacException;

    public void removeAwsdl(String wsdlQName) throws GfacException;

    public void registerServiceMap(String serviceMapAsStr, String abstractWsdlAsString) throws GfacException;

    public void removeServiceMap(String serviceQName) throws GfacException;

    public String getServiceMap(String serviceQName) throws GfacException;

    public void registerHostDesc(String hostDescAsStr) throws GfacException;

    public String getHostDesc(String hostName) throws GfacException;

    public void removeHostDesc(String hostName) throws GfacException;

    public void registerAppDesc(String appDescAsStr) throws GfacException;

    public String getAppDesc(String appQName, String hostName) throws GfacException;

    public void removeAppDesc(String appQName, String hostName) throws GfacException;

    public void registerOutputFiles(QName resourceId, String resourceName, String resourceType, String resourceDesc,
            String resourceDocument, String resourceParentTypedID, String owner) throws GfacException;

    public String[] findService(String serviceName) throws GfacException;

    public String[] findServiceDesc(String serviceName) throws GfacException;

    public String[] findAppDesc(String query) throws GfacException;

    public String[] listHosts() throws GfacException;

    public String[] listApps() throws GfacException;

    public String[] app2Hosts(String appName) throws GfacException;

    public String[] listAwsdl() throws GfacException;

    public boolean isAuthorizedToAcsses(String resourceID, String actor, String action) throws GfacException;

}
