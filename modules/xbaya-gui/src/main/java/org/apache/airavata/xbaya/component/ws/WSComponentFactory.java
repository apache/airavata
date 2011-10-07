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

package org.apache.airavata.xbaya.component.ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.common.exception.UtilsException;
import org.apache.airavata.common.utils.WSDLUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.xmlpull.infoset.XmlElement;

import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlPortType;
import xsul5.wsdl.WsdlPortTypeOperation;

public class WSComponentFactory {

    /**
     * @param wsdlString
     * @return The list of components in the specified WSDL.
     * @throws ComponentException
     */
    public static List<WSComponent> createComponents(String wsdlString) throws ComponentException {
        try {
            return createComponents(XMLUtil.stringToXmlElement(wsdlString));
        } catch (RuntimeException e) {
            throw new ComponentException(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
        }

    }

    /**
     * @param componentElement
     * @return The list of components in the specified WSDL.
     * @throws ComponentException
     */
    public static List<WSComponent> createComponents(XmlElement componentElement) throws ComponentException {
        try {
            WsdlDefinitions definitions = new WsdlDefinitions(componentElement);
            return createComponents(definitions);
        } catch (RuntimeException e) {
            throw new ComponentException(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
        }
    }

    /**
     * @param wsdl
     * @return The list of components in the specified WSDL.
     * @throws ComponentException
     */
    public static List<WSComponent> createComponents(WsdlDefinitions wsdl) throws ComponentException {
        List<WSComponent> components = new ArrayList<WSComponent>();
        try{
            QName portTypeQName = WSDLUtil.getFirstPortTypeQName(wsdl);
            WsdlPortType portType = wsdl.getPortType(portTypeQName.getLocalPart());
        for (WsdlPortTypeOperation operation : portType.operations()) {
            String operationName = operation.getOperationName();
            WSComponent component = createComponent(wsdl, portTypeQName, operationName);
            components.add(component);
        }
        }catch (Exception e){
            throw new ComponentException(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
        }
        return components;
    }

    /**
     * Creates a WSComponent.
     * 
     * @param wsdlString
     *            The string representation of the component
     * @return The WsdlComponent created
     * @throws ComponentException
     */
    @Deprecated
    public static WSComponent createComponent(String wsdlString) throws ComponentException {
        return createComponents(wsdlString).get(0);
    }

    /**
     * @param wsdl
     * @return The Component created
     * @throws ComponentException
     */
    public static WSComponent createComponent(WsdlDefinitions wsdl) throws ComponentException {
        return createComponent(wsdl, null, null);
    }

    /**
     * @param wsdl
     * @param portTypeQName
     * @param operationName
     * @return The component created.
     * @throws ComponentException
     */
    public static WSComponent createComponent(WsdlDefinitions wsdl, QName portTypeQName, String operationName)
            throws ComponentException {
        try {
            if (portTypeQName == null) {
                portTypeQName = WSDLUtil.getFirstPortTypeQName(wsdl);
            }
            if (operationName == null) {
                operationName = WSDLUtil.getFirstOperationName(wsdl, portTypeQName);
            }

            // check if it's WSComponent or WorkflowComponent
            WsdlPortType portType = wsdl.getPortType(portTypeQName.getLocalPart());
            XmlElement templateIDElement = portType.xml().element(WorkflowComponent.GPEL_NAMESPACE,
                    WorkflowComponent.WORKFLOW_TEMPLATE_ID_TAG);
            WSComponent component;
            if (templateIDElement == null) {
                component = new WSComponent(wsdl, portTypeQName, operationName);
            } else {
                component = new WorkflowComponent(wsdl, portTypeQName, operationName);
            }
            return component;
        } catch (RuntimeException e) {
            throw new ComponentException(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
        } catch (UtilsException e) {
            throw new ComponentException(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
        }
    }
}