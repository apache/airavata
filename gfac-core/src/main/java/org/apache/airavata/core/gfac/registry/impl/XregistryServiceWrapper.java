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

package org.apache.airavata.core.gfac.registry.impl;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.registry.RegistryService;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.globus.gsi.TrustedCertificates;
import org.ietf.jgss.GSSCredential;
import org.ogce.xregistry.client.XRegistryClient;
import org.ogce.xregistry.doc.AppData;
import org.ogce.xregistry.doc.DocData;
import org.ogce.xregistry.utils.XRegistryClientException;

import xregistry.generated.CapabilityToken;
import xregistry.generated.FindAppDescResponseDocument;
import xregistry.generated.HostDescData;
import xregistry.generated.ServiceDescData;
import xregistry.generated.WsdlData;

public class XregistryServiceWrapper implements RegistryService {

    private XRegistryClient xregistryClient;

    public XregistryServiceWrapper(String xregistryUrl, String trustedCertFile, GSSCredential sessionCredentail)
            throws GfacException {
        try {
            if (sessionCredentail != null && trustedCertFile != null) {
                if (new File(trustedCertFile).isFile()) {
                    this.xregistryClient = new XRegistryClient(sessionCredentail, trustedCertFile, xregistryUrl);
                } else {
                    TrustedCertificates certificates = TrustedCertificates.load(trustedCertFile);
                    TrustedCertificates.setDefaultTrustedCertificates(certificates);
                    X509Certificate[] trustedCertificates = certificates.getCertificates();
                    System.out.println("xregistryUrl=" + xregistryUrl);
                    System.out.println("trustedCertificates=" + trustedCertificates);
                    this.xregistryClient = new XRegistryClient(sessionCredentail, trustedCertificates, xregistryUrl);
                }
            } else {
                throw new GfacException("Neither host certificate of gss credential is set",
                        FaultCode.ErrorAtDependentService);
            }
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public XregistryServiceWrapper(String xregistryUrl, String trustedCertFile, String hostcertsKeyFile)
            throws GfacException {
        try {
            if (hostcertsKeyFile != null && trustedCertFile != null) {
                if (new File(trustedCertFile).isFile()) {
                    this.xregistryClient = new XRegistryClient(hostcertsKeyFile, trustedCertFile, xregistryUrl);
                } else {
                    TrustedCertificates certificates = TrustedCertificates.load(trustedCertFile);
                    TrustedCertificates.setDefaultTrustedCertificates(certificates);
                    X509Certificate[] trustedCertificates = certificates.getCertificates();
                    System.out.println("xregistryUrl=" + xregistryUrl);
                    System.out.println("hostcertsKeyFile=" + hostcertsKeyFile);
                    System.out.println("trustedCertificates=" + trustedCertificates);
                    this.xregistryClient = new XRegistryClient(hostcertsKeyFile, trustedCertificates, xregistryUrl);
                }
            } else {
                throw new GfacException("Neither host certificate of gss credential is set",
                        FaultCode.ErrorAtDependentService);
            }
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String[] app2Hosts(String appName) throws GfacException {
        try {
            return xregistryClient.app2Hosts(appName);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String[] findAppDesc(String query) throws GfacException {
        try {

            FindAppDescResponseDocument.FindAppDescResponse.AppData[] xregAppDesc = xregistryClient.findAppDesc(query);
            AppData[] appDesc = null;
            if (xregAppDesc != null) {
                List<AppData> appDescList = new ArrayList<AppData>();
                for (int i = 0; i < xregAppDesc.length; i++) {
                    try {
                        FindAppDescResponseDocument.FindAppDescResponse.AppData xbeansData = xregAppDesc[i];
                        AppData resultAppData = new AppData(xbeansData.getName(), xbeansData.getOwner(),
                                xbeansData.getHostName());
                        resultAppData.allowedAction = xbeansData.getAllowedAction();
                        resultAppData.resourceID = xbeansData.getName();
                        appDescList.add(resultAppData);
                    } catch (XmlValueOutOfRangeException e) {
                        throw new GfacException("Problem with retrieving object : " + e.getLocalizedMessage(),
                                FaultCode.ErrorAtDependentService);
                    }
                }
                appDesc = appDescList.toArray(new AppData[0]);
            } else {
                return null;
            }

            String[] finalResults = new String[appDesc.length];
            for (int i = 0; i < appDesc.length; i++) {
                finalResults[i] = appDesc[i].name + "#" + appDesc[i].secondryName;
            }
            return finalResults;
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String[] findService(String serviceName) throws GfacException {
        try {
            WsdlData[] serviceInstanceData = xregistryClient.findServiceInstance(serviceName);
            String[] results = new String[serviceInstanceData.length];
            for (int i = 0; i < serviceInstanceData.length; i++) {
                try {
                    results[i] = serviceInstanceData[i].getName().toString();
                } catch (XmlValueOutOfRangeException e) {
                    throw new GfacException("Problem with retrieving object : " + e.getLocalizedMessage(),
                            FaultCode.ErrorAtDependentService);
                }
            }
            return results;
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String[] findServiceDesc(String serviceName) throws GfacException {
        try {
            ServiceDescData[] serviceDescData = xregistryClient.findServiceDesc(serviceName);
            String[] results = new String[serviceDescData.length];
            for (int i = 0; i < serviceDescData.length; i++) {
                try {
                    results[i] = serviceDescData[i].getName().toString();
                } catch (XmlValueOutOfRangeException e) {
                    throw new GfacException("Problem with retrieving object : " + e.getLocalizedMessage(),
                            FaultCode.ErrorAtDependentService);
                }
            }
            return results;
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String getAbstractWsdl(String wsdlQName) throws GfacException {
        try {
            return xregistryClient.getAbstractWsdl(QName.valueOf(wsdlQName));
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String getAppDesc(String appQName, String hostName) throws GfacException {
        try {
            return xregistryClient.getAppDesc(appQName, hostName);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String getConcreateWsdl(String wsdlQName) throws GfacException {
        try {
            return xregistryClient.getConcreateWsdl(QName.valueOf(wsdlQName));
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String getHostDesc(String hostName) throws GfacException {
        try {
            return xregistryClient.getHostDesc(hostName);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String getServiceMap(String serviceQName) throws GfacException {
        try {
            return xregistryClient.getServiceDesc(QName.valueOf(serviceQName));
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String[] listApps() throws GfacException {
        return findAppDesc("");
    }

    public String[] listAwsdl() throws GfacException {
        return findServiceDesc("");
    }

    public String[] listHosts() throws GfacException {
        try {
            HostDescData[] hostDescData = xregistryClient.findHosts("");
            String[] results = new String[hostDescData.length];
            for (int i = 0; i < hostDescData.length; i++) {
                try {
                    results[i] = hostDescData[i].getName().toString();
                } catch (XmlValueOutOfRangeException e) {
                    throw new GfacException("Problem with retrieving object : " + e.getLocalizedMessage(),
                            FaultCode.ErrorAtDependentService);
                }
            }
            return results;
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void registerAppDesc(String appDescAsStr) throws GfacException {
        try {
            xregistryClient.registerAppDesc(appDescAsStr);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void registerConcreteWsdl(String wsdlAsStr, int lifetimeAsSeconds) throws GfacException {
        try {
            xregistryClient.registerConcreteWsdl(wsdlAsStr, lifetimeAsSeconds);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void registerOutputFiles(QName resourceId, String resourceName, String resourceType, String resourceDesc,
            String resourceDocument, String resourceParentTypedID, String owner) throws GfacException {
        try {
            xregistryClient.registerOGCEResource(resourceId, resourceName, resourceType, resourceDesc,
                    resourceDocument, resourceParentTypedID, owner);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void registerHostDesc(String hostDescAsStr) throws GfacException {
        try {
            xregistryClient.registerHostDesc(hostDescAsStr);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void registerServiceMap(String serviceMapAsStr, String abstractWsdlAsString) throws GfacException {
        try {
            xregistryClient.registerServiceDesc(serviceMapAsStr, abstractWsdlAsString);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void removeAppDesc(String appQName, String hostName) throws GfacException {
        try {
            xregistryClient.removeAppDesc(QName.valueOf(appQName), hostName);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void removeAwsdl(String wsdlQName) throws GfacException {
        throw new UnsupportedOperationException();
    }

    public void removeConcreteWsdl(String wsdlQName) throws GfacException {
        try {
            xregistryClient.removeConcreteWsdl(QName.valueOf(wsdlQName));
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void removeHostDesc(String hostName) throws GfacException {
        try {
            xregistryClient.removeHostDesc(hostName);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void removeServiceMap(String serviceQName) throws GfacException {
        try {
            xregistryClient.removeServiceDesc(QName.valueOf(serviceQName));
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public boolean isAuthorizedToAcsses(String resourceID, String actor, String action) throws GfacException {
        try {
            return xregistryClient.isAuthorizedToAcsses(null, resourceID, actor, action);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void addCapability(String resource, String actor, boolean isUser, String action) throws GfacException {
        try {
            xregistryClient.addCapability(resource, actor, isUser, action);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public CapabilityToken[] getCapability(String resource, String actor, boolean isUser, String action)
            throws GfacException {
        try {
            return xregistryClient.findCapability(resource, actor, isUser, action);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public void removeCapability(String resourceID, String actor) throws GfacException {
        try {
            xregistryClient.removeCapability(resourceID, actor);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public AppData[] xfindAppDesc(String query) throws GfacException {
        try {
            xregistry.generated.FindAppDescResponseDocument.FindAppDescResponse.AppData[] xregAppDesc = xregistryClient
                    .findAppDesc(query);
            AppData[] appDesc = null;
            if (xregAppDesc != null) {
                List<AppData> appDescList = new ArrayList<AppData>();
                for (int i = 0; i < xregAppDesc.length; i++) {
                    try {
                        xregistry.generated.FindAppDescResponseDocument.FindAppDescResponse.AppData xbeansData = xregAppDesc[i];
                        AppData resultAppData = new AppData(xbeansData.getName(), xbeansData.getOwner(),
                                xbeansData.getHostName());
                        resultAppData.allowedAction = xbeansData.getAllowedAction();
                        resultAppData.resourceID = xbeansData.getName();
                        appDescList.add(resultAppData);
                    } catch (XmlValueOutOfRangeException e) {
                        throw new GfacException("Problem with retrieving object : " + e.getLocalizedMessage(),
                                FaultCode.ErrorAtDependentService);
                    }
                }
                appDesc = appDescList.toArray(new AppData[0]);
            } else {
                return null;
            }
            return appDesc;
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public DocData[] xfindHostDesc(String query) throws GfacException {
        try {
            HostDescData[] hostDescData = xregistryClient.findHosts(query);
            if (hostDescData == null) {
                return null;
            }

            List<DocData> results = new ArrayList<DocData>();
            for (int i = 0; i < hostDescData.length; i++) {
                try {
                    HostDescData host = hostDescData[i];
                    DocData data = new DocData(new QName(host.getResourceID()), host.getOwner());
                    data.allowedAction = host.getAllowedAction();
                    data.resourceID = new QName(host.getResourceID());
                    results.add(data);
                } catch (XmlValueOutOfRangeException e) {
                    throw new GfacException("Problem with retrieving object : " + e.getLocalizedMessage(),
                            FaultCode.ErrorAtDependentService);
                }
            }
            return results.toArray(new DocData[0]);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public DocData[] xfindServiceDesc(String query) throws GfacException {
        try {
            ServiceDescData[] serviceDescData = xregistryClient.findServiceDesc(query);
            if (serviceDescData == null) {
                return null;
            }
            List<DocData> results = new ArrayList<DocData>();
            for (int i = 0; i < serviceDescData.length; i++) {
                try {
                    DocData data = new DocData(serviceDescData[i].getName(), serviceDescData[i].getOwner());
                    data.allowedAction = serviceDescData[i].getAllowedAction();
                    data.resourceID = serviceDescData[i].getName();
                    results.add(data);
                } catch (XmlValueOutOfRangeException e) {
                    throw new GfacException("Problem with retrieving object : " + e.getLocalizedMessage(),
                            FaultCode.ErrorAtDependentService);
                }
            }
            return results.toArray(new DocData[0]);
        } catch (XRegistryClientException e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

}
