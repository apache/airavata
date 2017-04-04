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
package org.apache.airavata.workflow.model.component.ws;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.utils.MessageConstants;

public class WSComponentFactory {

    /**
     * @param wsdlString
     * @return The list of components in the specified WSDL.
     * @throws ComponentException
     */
    public static List<WSComponent> createComponents(ApplicationInterfaceDescription application) throws ComponentException {
    	List<WSComponent> components = new ArrayList<WSComponent>();
        WSComponent component = createComponent(application, application.getApplicationInterfaceId());
        components.add(component);
        return components;

    }

    /**
     * @param wsdl
     * @param portTypeQName
     * @param operationName
     * @return The component created.
     * @throws ComponentException
     */
    public static WSComponent createComponent(ApplicationInterfaceDescription application, String operationName)
            throws ComponentException {
        try {
            WSComponent component;
            component = new WSComponent(new WSComponentApplication(application));
            return component;
        } catch (RuntimeException e) {
            throw new ComponentException(MessageConstants.COMPONENT_FORMAT_ERROR, e);
        }
    }

//	public static List<WSComponent> createComponents(
//			xsul5.wsdl.WsdlDefinitions wsdlDefinitions3ToWsdlDefintions5) {
//		return null;
//	}

	public static List<WSComponent> createComponents(String compString) {
		// TODO Auto-generated method stub
		return null;
	}
}