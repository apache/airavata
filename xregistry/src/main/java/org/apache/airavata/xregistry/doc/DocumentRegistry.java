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
package org.apache.airavata.xregistry.doc;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.xregistry.XregistryException;


public interface DocumentRegistry {

    public String registerHostDesc(String user, String hostDescAsStr) throws XregistryException;

    public String registerServiceDesc(String user, String serviceDescAsStr, String awsdlAsStr)
            throws XregistryException;

    public String registerAppDesc(String user, String appDescAsStr) throws XregistryException;

    public void removeServiceDesc(String serviceName) throws XregistryException;

    public void removeAppDesc(String appName, String hostName) throws XregistryException;

    public void removeHostDesc(String hostName) throws XregistryException;

    public String getServiceDesc(String serviceName) throws XregistryException;

    public String getAppDesc(String appName, String hostName) throws XregistryException;

    public String getHostDesc(String hostName) throws XregistryException;

    public String registerConcreteWsdl(String user, String wsdlAsStr, int lifetimeAsSeconds)
            throws XregistryException;

    public String getConcreateWsdl(String wsdlQName) throws XregistryException;

    public void removeConcreteWsdl(String wsdlQName) throws XregistryException;

    public String getAbstractWsdl(String wsdlQName) throws XregistryException;

    public List<DocData> findServiceInstance(String user,String serviceName) throws XregistryException;

    public List<DocData> findServiceDesc(String user,String serviceName) throws XregistryException;

    public List<AppData> findAppDesc(String user,String query) throws XregistryException;

    public List<DocData> findHosts(String user,String hostName) throws XregistryException;

    public String[] app2Hosts(String appName) throws XregistryException;
    
    public void registerDocument(String user,QName resourceID,String document)throws XregistryException;
    
    public void removeDocument(String user,QName resourceID)throws XregistryException;
    
    public List<DocData> findDocument(String user,String query)throws XregistryException;
    
    public String getDocument(String user,QName docName) throws XregistryException;
    
    public void registerOGCEResource(String user,QName resourceID,String resourceName, String resourceType, String resourceDesc, String resourceDocuemnt, String resoureParentTypedID)throws XregistryException;
    
    public void removeOGCEResource(String user,QName resourceID,String resourceType)throws XregistryException;
    
    public List<DocData> findOGCEResource(String user,String query,String resourceName,String resourceType,String resoureParentTypedID)throws XregistryException;
    
    public String getOGCEResource(String user,QName resourceID, String resourceType, String resoureParentTypedID) throws XregistryException;

}
