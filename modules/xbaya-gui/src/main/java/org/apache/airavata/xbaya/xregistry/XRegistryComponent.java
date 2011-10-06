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

package org.apache.airavata.xbaya.xregistry;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xregistry.generated.ServiceDescData;
import xregistry.generated.WsdlData;

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

    private static final Logger logger = LoggerFactory.getLogger(XRegistryComponent.class);

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
                logger.info("qname: " + qname);
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
            logger.info("concreateWSDL:" + wsdl);
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