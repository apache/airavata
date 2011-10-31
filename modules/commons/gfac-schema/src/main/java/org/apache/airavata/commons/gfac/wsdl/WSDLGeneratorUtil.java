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

package org.apache.airavata.commons.gfac.wsdl;

import org.apache.airavata.schemas.gfac.MethodType;
import org.apache.airavata.schemas.gfac.PortTypeType;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;

public class WSDLGeneratorUtil {

    public static MethodType findOperationFromServiceDesc(String methodName, ServiceDescriptionType serviceDescType)
            throws GFacWSDLException {
        PortTypeType portType = serviceDescType.getPortType();
        if (serviceDescType.getPortType().getMethod().getMethodName().equals(methodName)) {
            serviceDescType.getPortType().getMethod();
        }

        if (isInbuiltOperation(methodName)) {
            MethodType builtInOperationType = portType.addNewMethod();
            builtInOperationType.setMethodName(methodName);
            return builtInOperationType;
        }

        throw new GFacWSDLException("Method name " + methodName + " not found");
    }

    public static boolean isInbuiltOperation(String name) {
        return GFacSchemaConstants.InbuitOperations.OP_KILL.equals(name)
                || GFacSchemaConstants.InbuitOperations.OP_PING.equals(name)
                || GFacSchemaConstants.InbuitOperations.OP_SHUTDOWN.equals(name);
    }

}