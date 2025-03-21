/**
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
 */
package org.apache.airavata.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class WSDLUtil {

    private static final Logger logger = LoggerFactory.getLogger(WSDLUtil.class);

    /**
     * @param uri
     * @return The URI with "?wsdl" at the end.
     */
    public static String appendWSDLQuary(String uri) {
        URI wsdlURI = appendWSDLQuary(URI.create(uri));
        return wsdlURI.toString();
    }

    /**
     * Get namespaces from a DOM Element
     * @param element The DOM Element
     * @return List of namespaces
     */
    public static List<QName> getNamespaces(Element element) {
        List<QName> namespaces = new LinkedList<>();
        
        // Get the namespace of the element itself
        String namespaceURI = element.getNamespaceURI();
        String prefix = element.getPrefix();
        if (namespaceURI != null && !namespaceURI.isEmpty()) {
            namespaces.add(new QName(namespaceURI, prefix));
        }
        
        // Get namespaces from attributes
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            String attrNamespaceURI = attr.getNamespaceURI();
            String attrPrefix = attr.getPrefix();
            
            if (attrNamespaceURI != null && !attrNamespaceURI.isEmpty()) {
                QName ns = new QName(attrNamespaceURI, attrPrefix);
                if (!namespaces.contains(ns)) {
                    namespaces.add(ns);
                }
            }
            
            // Check for xmlns attributes
            if (attr.getNodeName().startsWith("xmlns:")) {
                String nsPrefix = attr.getNodeName().substring(6);
                String nsURI = attr.getNodeValue();
                QName ns = new QName(nsURI, nsPrefix);
                if (!namespaces.contains(ns)) {
                    namespaces.add(ns);
                }
            }
        }
        
        // Process child elements
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                List<QName> childNamespaces = getNamespaces((Element) child);
                for (QName ns : childNamespaces) {
                    if (!namespaces.contains(ns)) {
                        namespaces.add(ns);
                    }
                }
            }
        }
        
        return namespaces;
    }

    /**
     * @param uri
     * @return The URI with "?wsdl" at the end.
     */
    public static URI appendWSDLQuary(URI uri) {
        if (uri.toString().endsWith("?wsdl")) {
            logger.warn("URL already has ?wsdl at the end: " + uri.toString());
            // Don't throw exception to be more error tolerant.
            return uri;
        }
        String path = uri.getPath();
        if (path == null || path.length() == 0) {
            uri = uri.resolve("/");
        }
        uri = URI.create(uri.toString() + "?wsdl");
        return uri;
    }

    /**
     * 
     * @param vals
     * @param <T>
     * @return
     */
    public static <T extends Object> T getfirst(Iterable<T> vals) {
        for (T class1 : vals) {
            return class1;
        }
        throw new RuntimeException("Iterator empty");
    }

    /**
     * @param workflowID
     * @return
     */
    public static String findWorkflowName(URI workflowID) {
        String[] splits = workflowID.toString().split("/");
        return splits[splits.length - 1];
    }

    /**
     * Replace attribute value in a DOM Element
     * @param element The DOM Element
     * @param name The attribute name
     * @param oldValue The old value
     * @param newValue The new value
     */
    public static void replaceAttributeValue(Element element, String name, String oldValue, String newValue) {
        if (element.hasAttribute(name) && oldValue.equals(element.getAttribute(name))) {
            element.setAttribute(name, newValue);
        }
        
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                replaceAttributeValue((Element) child, name, oldValue, newValue);
            }
        }
    }

    /**
     * Check if an attribute exists with a specific value
     * @param element The DOM Element
     * @param name The attribute name
     * @param value The attribute value
     * @return true if the attribute exists with the specified value
     */
    public static boolean attributeExist(Element element, String name, String value) {
        if (element.hasAttribute(name) && value.equals(element.getAttribute(name))) {
            return true;
        }
        
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                if (attributeExist((Element) child, name, value)) {
                    return true;
                }
            }
        }
        
        return false;
    }
}