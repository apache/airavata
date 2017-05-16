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
package org.apache.airavata.workflow.engine.gfac;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.xmlpull.v1.builder.Iterable;
import org.xmlpull.v1.builder.XmlElement;
//
//import xsul.wsif.WSIFMessage;
//import xsul.wsif.impl.WSIFMessageElement;

public class GFacRegistryClient {

    /**
     * GFAC_NAMESPACE
     */
    public static final String GFAC_NAMESPACE = "http://www.extreme.indiana.edu/namespaces/2004/01/gFac";

    private static final String SEARCH_SERVICE_INSTANCE = "searchServiceInstance";

    private static final String SEARCH_SERVIE = "searchService";

    private static final String QNAME = "qname";

    private static final String DESC_AS_STRING = "descAsStr";

    private static final String LIFE_TIME = "lifetimeAsSeconds";

    private static final String RESULTS = "results";

    private static final String GET_ABSTRACT_WSDL = "getAbstractWsdl";

    private String wsdlURL;

    private SimpleWSClient client;

    /**
     * Constructs a GFacRegistryClient.
     * 
     * @param wsdlURL
     */
    public GFacRegistryClient(URI wsdlURL) {
        this(wsdlURL.toString());
    }

    /**
     * Constructs a GfacRegistryClient.
     * 
     * @param wsdlURL
     */
    public GFacRegistryClient(String wsdlURL) {
        this.wsdlURL = wsdlURL;
        this.client = new SimpleWSClient();
    }

//    /**
//     * @param appDescAsStr
//     * @throws ComponentRegistryException
//     */
//    public void registerAppDesc(String appDescAsStr) throws ComponentRegistryException {
//        this.client.sendSOAPMessage(this.wsdlURL, new String[][] { { DESC_AS_STRING, appDescAsStr } },
//                "registerAppDesc");
//    }
//
//    /**
//     * @param wsdlAsStr
//     * @param lifetimeAsSeconds
//     * @throws ComponentRegistryException
//     */
//    public void registerConcreteWsdl(String wsdlAsStr, int lifetimeAsSeconds) throws ComponentRegistryException {
//        this.client.sendSOAPMessage(this.wsdlURL,
//                new String[][] { { DESC_AS_STRING, wsdlAsStr }, { LIFE_TIME, String.valueOf(lifetimeAsSeconds) } },
//                "registerConcreteWsdl");
//
//    }
//
//    /**
//     * @param wsdlQName
//     * @return The concrete WSDL
//     * @throws ComponentRegistryException
//     */
//    public String getConcreteWsdl(String wsdlQName) throws ComponentRegistryException {
//        WSIFMessage response = this.client.sendSOAPMessage(this.wsdlURL, new String[][] { { QNAME, wsdlQName } },
//                "getConcreateWsdl");
//        return (String) response.getObjectPart(DESC_AS_STRING);
//    }
//
//    /**
//     * @param wsdlQName
//     * @throws ComponentRegistryException
//     */
//    public void removeConcreteWsdl(String wsdlQName) throws ComponentRegistryException {
//        this.client.sendSOAPMessage(this.wsdlURL, new String[][] { { QNAME, wsdlQName } }, "removeConcreteWsdl");
//
//    }
//
//    /**
//     * @param serviceName
//     * @return The list of concreate WSDL QNames.
//     * @throws ComponentRegistryException
//     */
//    public String[] findService(String serviceName) throws ComponentRegistryException {
//        WSIFMessage response = this.client.sendSOAPMessage(this.wsdlURL, new String[][] { { QNAME, serviceName } },
//                SEARCH_SERVICE_INSTANCE);
//        return findArrayValue(RESULTS, (WSIFMessageElement) response).toArray(new String[] {});
//    }
//
//    /**
//     * @param serviceName
//     * @return The list of abstract WSDL QNames.
//     * @throws ComponentRegistryException
//     */
//    public String[] findServiceDesc(String serviceName) throws ComponentRegistryException {
//        WSIFMessage response = this.client.sendSOAPMessage(this.wsdlURL, new String[][] { { QNAME, serviceName } },
//                SEARCH_SERVIE);
//        return findArrayValue(RESULTS, (WSIFMessageElement) response).toArray(new String[] {});
//    }
//
//    /**
//     * @param wsdlQName
//     * @return The AWSDL.
//     * @throws ComponentRegistryException
//     */
//    public String getAbstractWsdl(String wsdlQName) throws ComponentRegistryException {
//        WSIFMessage response = this.client.sendSOAPMessage(this.wsdlURL, new String[][] { { QNAME, wsdlQName } },
//                GET_ABSTRACT_WSDL);
//        return (String) response.getObjectPart(DESC_AS_STRING);
//    }
//
//    private static ArrayList<String> findArrayValue(String name, WSIFMessageElement response) {
//        XmlElement param = response.element(null, name);
//        if (param != null) {
//            Iterable it = param.elements(null, "value");
//            if (it != null) {
//                ArrayList<String> values = new ArrayList<String>();
//
//                Iterator arrayValues = it.iterator();
//                while (arrayValues.hasNext()) {
//                    values.add(((XmlElement) arrayValues.next()).requiredTextContent());
//                }
//                return values;
//            }
//        }
//        return null;
//    }
}
