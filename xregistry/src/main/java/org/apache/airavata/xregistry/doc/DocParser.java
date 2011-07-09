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

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.apache.airavata.xregistry.XregistryException;
import org.apache.xmlbeans.XmlException;

import edu.indiana.extreme.gfac.schemas.SchemaValidator;
import edu.indiana.extreme.gfac.wsdl.WSDLConstants;
import edu.indiana.extreme.gfac.wsdl.WSDLGenerator;
import edu.indiana.extreme.namespaces.x2004.x01.gFac.ApplicationDescriptionDocument;
import edu.indiana.extreme.namespaces.x2004.x01.gFac.ApplicationDescriptionType;
import edu.indiana.extreme.namespaces.x2004.x01.gFac.HostDescriptionDocument;
import edu.indiana.extreme.namespaces.x2004.x01.gFac.HostDescriptionType;
import edu.indiana.extreme.namespaces.x2004.x01.gFac.MethodType;
import edu.indiana.extreme.namespaces.x2004.x01.gFac.ServiceMapDocument;
import edu.indiana.extreme.namespaces.x2004.x01.gFac.ServiceMapType;
import edu.indiana.extreme.namespaces.x2004.x01.gFac.ApplicationDescriptionType.ApplicationName;
import edu.indiana.extreme.namespaces.x2004.x01.gFac.ServiceType.ServiceName;

public class DocParser {

    public static String parseHostDesc(String hostDescAsStr) throws XregistryException {
        try {
            HostDescriptionType hostDesc = HostDescriptionDocument.Factory.parse(
                    new StringReader(hostDescAsStr)).getHostDescription();
            hostDesc.validate();
            return hostDesc.getHostName();
        } catch (XmlException e) {
            throw new XregistryException(e);
        } catch (IOException e) {
            throw new XregistryException(e);
        }
    }

    public static ServiceMapType parseServiceDesc(String serviceDescAsStr)
            throws XregistryException {
        try {
            ServiceMapType serviceDesc = ServiceMapDocument.Factory.parse(
                    new StringReader(serviceDescAsStr)).getServiceMap();
            serviceDesc.validate();
            return serviceDesc;
        } catch (XmlException e) {
            throw new XregistryException(e);
        } catch (IOException e) {
            throw new XregistryException(e);
        }
    }

    public static QName getServiceName(ServiceName serviceName) {
        return new QName(serviceName.getTargetNamespace(), serviceName.getStringValue());
    }

    public static ApplicationDescriptionType parseAppeDesc(String appDescAsStr) throws XregistryException {
        try {
            ApplicationDescriptionType appDesc = ApplicationDescriptionDocument.Factory.parse(
                    new StringReader(appDescAsStr)).getApplicationDescription();
            appDesc.validate();
            return appDesc;
        } catch (XmlException e) {
            throw new XregistryException(e);
        } catch (IOException e) {
            throw new XregistryException(e);
        }
    }

    public static QName getAppName(ApplicationName appName) {
        return new QName(appName.getTargetNamespace(), appName.getStringValue());
    }

    public static MethodType findOperationWithApplication(ServiceMapType serviceMap) {
        MethodType[] methods = serviceMap.getPortTypeArray()[0].getMethodArray();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getApplication() != null) {
                return methods[i];
            }
        }
        return null;
    }
    
    public static String createWsdl(String serviceMapAsStr,boolean isAbstract) throws XregistryException{
        try {
            ServiceMapType serviceMap = ServiceMapDocument.Factory.parse(serviceMapAsStr).getServiceMap();
            SchemaValidator validator = new SchemaValidator(serviceMap);
            validator.validate();
            QName serviceQname = new QName(serviceMap.getService().getServiceName()
                    .getTargetNamespace(), serviceMap.getService().getServiceName()
                    .getStringValue());
            WSDLGenerator wsdlGenerator = new WSDLGenerator();
            Hashtable serviceTable = wsdlGenerator.generateWSDL(null, serviceQname, null,
                    serviceMap, isAbstract);
            if(isAbstract){
                return (String) serviceTable.get(WSDLConstants.AWSDL);
            }else{
                return (String) serviceTable.get(WSDLConstants.WSDL);
            }
        } catch (XmlException e) {
            throw new XregistryException(e);
        } catch (Exception e) {
            throw new XregistryException(e);
        }
    }

}
