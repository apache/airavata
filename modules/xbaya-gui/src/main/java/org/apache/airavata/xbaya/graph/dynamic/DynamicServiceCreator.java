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

package org.apache.airavata.xbaya.graph.dynamic;

import java.io.File;
import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.invoker.Invoker;
import org.apache.airavata.xbaya.invoker.factory.InvokerFactory;

import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlResolver;

public class DynamicServiceCreator {

    private String dynamicFactoryWSDLLocation;

    private static String classStr = "package org.apache.airavata.xbaya;" +

    "public class DefaultClassName{" +

    "public int operationName(String[] stringArray0){" +

    "return 8;" + "}" + "}";

    /**
     * Constructs a DynamicServiceCreator.
     * 
     * @param dynamicFactoryWSDLLocation
     */
    public DynamicServiceCreator(String dynamicFactoryWSDLLocation) {
        this.dynamicFactoryWSDLLocation = dynamicFactoryWSDLLocation;
    }

    public void createService(String code) throws XBayaException {
        try {
            WsdlDefinitions definitions = null;
            if (this.dynamicFactoryWSDLLocation != null && !this.dynamicFactoryWSDLLocation.equals("")) {
                definitions = WsdlResolver.getInstance().loadWsdl(new File(".").toURI(),
                        new URI(this.dynamicFactoryWSDLLocation));
            }

            // Create Invoker
            // FIXME: Should pass the right leadcontext header for last argument
            Invoker invoker = InvokerFactory.createInvoker(new QName("http://extreme.indiana.edu",
                    "ServiceCreatorPortType"), definitions, null, null, null);

            invoker.setup();

            invoker.setOperation("deployServiceFromClass");
            invoker.setInput("classAsString", code);
            invoker.invoke();
            invoker.getOutput("return");
        } catch (Exception e) {
            throw new XBayaException(e);
        }

    }

    public static void main(String[] args) throws XBayaException {
        DynamicServiceCreator c = new DynamicServiceCreator("http://127.0.0.1:8080/axis2/services/ServiceCreator?wsdl");
        c.createService(classStr);
    }

}