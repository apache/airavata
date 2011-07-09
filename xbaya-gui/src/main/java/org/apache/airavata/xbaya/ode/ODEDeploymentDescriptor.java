/*
 * Copyright (c) 2008 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: ODEDeploymentDescriptor.java,v 1.4 2009/01/10 06:48:18 cherath Exp $
 */
package org.apache.airavata.xbaya.ode;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.graph.GraphException;
import org.gpel.model.GpelPartnerLink;
import org.gpel.model.GpelPartnerLinksContainer;
import org.gpel.model.GpelProcess;
import org.xmlpull.infoset.XmlAttribute;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlInfosetBuilder;
import org.xmlpull.infoset.XmlNamespace;

import xsul5.XmlConstants;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlPort;
import xsul5.wsdl.WsdlPortType;
import xsul5.wsdl.WsdlService;

/**
 * @author Chathura Herath
 */
public class ODEDeploymentDescriptor {

    /**
     * NAME
     */
    private static final String NAME = "name";
    /**
     * PROCESS
     */
    private static final String PROCESS = "process";

    private static XmlInfosetBuilder builder = XmlConstants.BUILDER;

    public ODEDeploymentDescriptor() {
    }

    public XmlElement generate(String workflowName, WsdlDefinitions workflowWSDL, GpelProcess gpelProcess,
            Map<String, WsdlDefinitions> wsdls) throws GraphException {

        Iterator<WsdlService> services = workflowWSDL.services().iterator();
        WsdlService service = null;
        if (services.hasNext()) {
            service = services.next();
        } else {
            throw new IllegalStateException("NO Service found in the workflow WSDL:" + workflowName);
        }

        Iterator<WsdlPort> ports = service.ports().iterator();
        WsdlPort port = null;
        if (ports.hasNext()) {
            port = ports.next();
        } else {
            throw new IllegalStateException("NO Port found in the workflow WSDL:" + workflowName);
        }
        String targetNamespace = gpelProcess.getTargetNamespace();
        String targetNamespacePrefix = "wfns";

        XmlNamespace odeNs = builder.newNamespace("http://www.apache.org/ode/schemas/dd/2007/03");
        XmlElement deploy = builder.newFragment(odeNs, "deploy");
        deploy.declareNamespace(PROCESS, targetNamespace);
        deploy.declareNamespace(targetNamespacePrefix, workflowWSDL.getTargetNamespace());

        XmlElement process = deploy.addElement(odeNs, PROCESS);
        process.setAttributeValue(NAME, PROCESS + ":" + workflowName);
        // active
        XmlElement active = process.addElement(odeNs, "active");
        active.addChild("true");

        // provide
        XmlElement provide = process.addElement(odeNs, "provide");
        provide.setAttributeValue("partnerLink", "workflowUserPartner");
        XmlElement providerService = provide.addElement(odeNs, "service");
        providerService.setAttributeValue("port", port.getName());
        providerService.setAttributeValue(NAME, targetNamespacePrefix + ":" + service.getName());

        org.xmlpull.infoset.XmlElement wsdlXml = workflowWSDL.xml();
        Iterable<org.xmlpull.infoset.XmlElement> partnerLinkTypes = wsdlXml.elements(null, "partnerLinkType");

        GpelPartnerLinksContainer partnerLinks = gpelProcess.getPartnerLinks();
        Iterable<GpelPartnerLink> partnerLinkList = partnerLinks.partnerLinks();

        HashMap<String, String> newNamespaceMap = new HashMap<String, String>();

        for (GpelPartnerLink link : partnerLinkList) {
            String partnerRole = link.getPartnerRole();
            if (null != partnerRole) {
                // These are the parrtner links that are non providers
                XmlElement invoke = process.addElement(odeNs, "invoke");
                invoke.setAttributeValue("partnerLink", link.getName());

                XmlElement invokeService = invoke.addElement(odeNs, "service");
                // invokeService.addAttribute("name", arg1)
                QName partnerLinkTypeQName = link.getPartnerLinkTypeQName();
                Iterator<org.xmlpull.infoset.XmlElement> plIterator = partnerLinkTypes.iterator();
                while (plIterator.hasNext()) {
                    org.xmlpull.infoset.XmlElement plType = plIterator.next();
                    XmlAttribute plTypeName = plType.attribute(NAME);
                    if (plTypeName.getValue().equals(partnerLinkTypeQName.getLocalPart())) {
                        // found the correct partnerlink type
                        // now find the porttype

                        XmlAttribute plPortType = plType.element("role").attribute("portType");
                        String portTypeQnameString = plPortType.getValue();
                        String[] portTypeSegs = portTypeQnameString.split(":");
                        Iterator<org.xmlpull.infoset.XmlNamespace> namespaceIterator = wsdlXml.namespaces().iterator();
                        QName portTypeQname = null;
                        // find the qname of the porttype
                        while (namespaceIterator.hasNext()) {
                            org.xmlpull.infoset.XmlNamespace ns = (org.xmlpull.infoset.XmlNamespace) namespaceIterator
                                    .next();
                            if (ns.getPrefix().equals(portTypeSegs[0])) {
                                portTypeQname = new QName(ns.getName(), portTypeSegs[1]);
                            }

                        }

                        // now go through the WSDLS and find the one with the proper port type
                        Set<String> keys = wsdls.keySet();
                        for (String key : keys) {
                            WsdlDefinitions wsdl = wsdls.get(key);
                            WsdlPortType portType = wsdl.getPortType(portTypeQname.getLocalPart());
                            if (null != portType && portType.getQName().equals(portTypeQname)) {
                                // this is the right porttype so extract the service and you will be done
                                Iterator<WsdlService> svcIterator = wsdl.services().iterator();
                                String nsPrefix = null;
                                if (svcIterator.hasNext()) {
                                    WsdlService plService = svcIterator.next();
                                    if (null == newNamespaceMap.get(wsdl.getTargetNamespace())) {
                                        nsPrefix = "p" + newNamespaceMap.size();
                                        newNamespaceMap.put(wsdl.getTargetNamespace(), nsPrefix);
                                    } else {
                                        nsPrefix = newNamespaceMap.get(wsdl.getTargetNamespace());
                                    }

                                    String portName = null;
                                    Iterator<WsdlPort> portItr = plService.ports().iterator();
                                    if (portItr.hasNext()) {
                                        portName = portItr.next().getName();
                                    }
                                    invokeService.setAttributeValue(NAME, nsPrefix + ":" + plService.getName());
                                    invokeService.setAttributeValue("port", portName);

                                }
                            }
                        }
                    }
                }

            }
        }

        Set<String> keys = newNamespaceMap.keySet();
        for (String key : keys) {
            String nsPrefix = newNamespaceMap.get(key);
            deploy.setAttributeValue("xmlns:" + nsPrefix, key);
        }
        return deploy;
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2008 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
