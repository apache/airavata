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
package org.apache.airavata.workflow.model.gpel;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import javax.xml.namespace.QName;

import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;

//import xsul5.wsdl.WsdlDefinitions;
//import xsul5.wsdl.WsdlPortType;
//import xsul5.wsdl.WsdlResolver;
//import xsul5.wsdl.WsdlUtil;

public class DSCUtil {

////    /**
////     * For debugging
////     * 
////     * @param args
////     */
////    public static void main(String[] args) {
////        WsdlDefinitions awsdl = WsdlResolver.getInstance().loadWsdl(DSCUtil.class, "wsdls/math/adder-awsdl.xml");
////        WsdlDefinitions cwsdl = convertToCWSDL(awsdl, URI.create("http://localhost"));
////        System.out.println(cwsdl.xmlStringPretty());
////    }
//
//    /**
//     * Creates CWSDLs for all WSDLs in a workflow.
//     * 
//     * @param workflow
//     * @param dscURL
//     * @return The Map<partnerLinkName, CWSDL>.
//     * @throws URISyntaxException
//     */
//    public static Map<String, WsdlDefinitions> createCWSDLs(Workflow workflow, String dscURL) throws URISyntaxException {
//        return createCWSDLs(workflow, new URI(dscURL));
//    }
//
//    /**
//     * @param workflow
//     * @param dscURL
//     * @return The Map<partnerLinkName, CWSDL>.
//     */
//    public static Map<String, WsdlDefinitions> createCWSDLs(Workflow workflow, URI dscURL) {
//        Map<String, WsdlDefinitions> WSDLMap = new HashMap<String, WsdlDefinitions>();
//        Graph graph = workflow.getGraph();
////        for (WSNode node : GraphUtil.getWSNodes(graph)) {
////            String partnerLinkName = BPELScript.createPartnerLinkName(node.getID());
////            WsdlDefinitions wsdl = node.getComponent().getWSDL();
////            if (WSDLUtil.isAWSDL(wsdl)) {
////                try {
////                    wsdl = convertToCWSDL(WSDLUtil.deepClone(wsdl), dscURL);
////                } catch (UtilsException e) {
////                    e.printStackTrace();
////                }
////            }
////            WSDLMap.put(partnerLinkName, wsdl);
////        }
//        return WSDLMap;
//    }
//
//    /**
//     * Converts a specified AWSDL to CWSDL using DSC URI.
//     * 
//     * @param definitions
//     *            The specified AWSDL. This will be modified.
//     * @param dscURI
//     * @return The CWSDL converted.
//     */
//    public static WsdlDefinitions convertToCWSDL(WsdlDefinitions definitions, URI dscURI) {
//        // Create a new List to avoid ConcurrentModificationException.
//        List<WsdlPortType> portTypes = new ArrayList<WsdlPortType>();
//        for (WsdlPortType portType : definitions.portTypes()) {
//            portTypes.add(portType);
//        }
//
//        for (WsdlPortType portType : portTypes) {
//            URI uri = creatEPR(dscURI, portType.getQName());
//            WsdlUtil.createCWSDL(definitions, portType, uri);
//        }
//        return definitions;
//    }

    private static URI creatEPR(URI dscURI, QName portTypeQName) {
        String encodedPortType;
        try {
            encodedPortType = URLEncoder.encode(portTypeQName.toString(), "UTF-8");
            URI epr = dscURI.resolve("/" + encodedPortType);
            return epr;
        } catch (UnsupportedEncodingException e) {
            // Should not happen
            throw new WorkflowRuntimeException(e);
        }
    }
}