/*
 * Copyright (c) 2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: XRegistry.java,v 1.5 2008/04/01 21:44:27 echintha Exp $
 */
package org.apache.airavata.xbaya.xregistry;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.gui.ComponentTreeNode;
import org.apache.airavata.xbaya.component.registry.ComponentRegistry;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.component.ws.WSComponent;
import org.apache.airavata.xbaya.component.ws.WSComponentFactory;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.ietf.jgss.GSSCredential;
import org.ogce.xregistry.client.XRegistryClient;
import org.ogce.xregistry.utils.XRegistryClientException;

import xregistry.generated.ServiceDescData;
import xregistry.generated.WsdlData;
import xsul5.MLogger;

/**
 * @author Satoshi Shirasuna
 */
public class XRegistryComponent extends ComponentRegistry {

    /**
     * Either abstract WSDL or concrete WSDL.
     */
    public enum Type {
        /**
         * ABSTRACT
         */
        ABSTRACT,

        /**
         * CONCRETE
         */
        CONCRETE;
    }

    private static final MLogger logger = MLogger.getLogger();

    private URI url;

    private Type type;

    private XRegistryClient xregistryClient;

    private GSSCredential proxy;

    /**
     * Constructs a XRegistry.
     * 
     * @param url
     * @param type
     */
    public XRegistryComponent(URI url, Type type) {
        this(url, type, null);
    }

    /**
     * Constructs a XRegistry.
     * 
     * @param url
     * @param type
     * @param proxy
     */
    public XRegistryComponent(URI url, Type type, GSSCredential proxy) {
        this.url = url;
        this.type = type;
        this.proxy = proxy;
    }

    /**
     * @see org.apache.airavata.xbaya.component.registry.ComponentRegistry#getComponentTree()
     */
    @Override
    public ComponentTreeNode getComponentTree() throws ComponentRegistryException {
        if (this.xregistryClient == null) {
            connect();
        }

        try {
            ComponentTreeNode tree = new ComponentTreeNode(this);

            List<QName> qnames = new ArrayList<QName>();
            if (this.type == Type.CONCRETE) {
                // "" returns all CWSDLs.
                WsdlData[] datas = this.xregistryClient.findServiceInstance("");
                if (datas != null) {
                    for (WsdlData wsdlData : datas) {
                        qnames.add(wsdlData.getName());
                    }
                }
            } else {
                ServiceDescData[] datas = this.xregistryClient.findServiceDesc("");
                if (datas != null) {
                    for (ServiceDescData wsdlData : datas) {
                        qnames.add(wsdlData.getName());
                    }
                }
            }

            Map<String, ComponentTreeNode> namespaceMap = new HashMap<String, ComponentTreeNode>();

            for (QName qname : qnames) {
                logger.finest("qname: " + qname);
                XRegistryComponentReference componentRef = new XRegistryComponentReference(this, qname);
                String namespace = qname.getNamespaceURI();
                ComponentTreeNode namespaceNode = namespaceMap.get(namespace);
                if (namespaceNode == null) {
                    namespaceNode = new ComponentTreeNode(namespace);
                    namespaceMap.put(namespace, namespaceNode);
                    tree.add(namespaceNode);
                }
                ComponentTreeNode treeLeaf = new ComponentTreeNode(componentRef);
                namespaceNode.add(treeLeaf);
            }
            return tree;
        } catch (XRegistryClientException e) {
            throw new ComponentRegistryException(e);
        } catch (RuntimeException e) {
            throw new ComponentRegistryException(e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return this.url.toString();
    }

    /**
     * @param qname
     * @return The Component
     * @throws ComponentRegistryException
     * @throws ComponentException
     */
    public List<WSComponent> getComponents(QName qname) throws ComponentRegistryException, ComponentException {
        if (this.xregistryClient == null) {
            connect();
        }
        try {
            String wsdl;
            if (this.type == Type.CONCRETE) {
                wsdl = this.xregistryClient.getConcreateWsdl(qname);
            } else {
                wsdl = this.xregistryClient.getAbstractWsdl(qname);
            }
            logger.finest("concreateWSDL:" + wsdl);
            List<WSComponent> components = WSComponentFactory.createComponents(wsdl);
            return components;
        } catch (XRegistryClientException e) {
            throw new ComponentRegistryException(e);
        } catch (RuntimeException e) {
            throw new ComponentRegistryException(e);
        }
    }

    private void connect() throws ComponentRegistryException {
        try {
            this.xregistryClient = new XRegistryClient(this.proxy, XBayaSecurity.getTrustedCertificates(),
                    this.url.toString());
        } catch (XRegistryClientException e) {
            throw new XBayaRuntimeException(e);
        }
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2007 The Trustees of Indiana University. All rights reserved.
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
