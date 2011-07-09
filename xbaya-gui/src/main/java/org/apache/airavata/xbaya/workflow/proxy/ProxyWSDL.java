/*
 * Copyright (c) 2009 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: $
 */
package org.apache.airavata.xbaya.workflow.proxy;

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

/**
 * @author Chathura Herath
 */
public class ProxyWSDL {
    // FIXME Remove this once the duel network problem is solved
    public static String WSDL = "<wsdl:definitions targetNamespace='http://extreme.indiana.edu/weps' "
            + "xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/' xmlns:wsdl='http://schemas.xmlsoap.org/wsdl/' "
            + "xmlns:tns='http://extreme.indiana.edu/weps' xmlns:http='http://schemas.xmlsoap.org/wsdl/http/' "
            + "xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/wsdl/soap/' "
            + "xmlns:mime='http://schemas.xmlsoap.org/wsdl/mime/'> " + "<wsdl:types> "
            + "<xs:schema elementFormDefault='qualified' " + "targetNamespace='http://extreme.indiana.edu/weps'> "
            + "<xs:element name='DeploymentInformation'> " + "<xs:complexType> " + "<xs:sequence> "
            + "<xs:element name='ProcessName' type='xs:string' /> "
            + "<xs:element maxOccurs='1' minOccurs='0' name='TemplateId' " + "type='xs:string' /> "
            + "<xs:element name='DeploymentDocuments' type='tns:DeploymentDocumentsType'> " +

            "</xs:element> " + "</xs:sequence> " + "</xs:complexType> " + "</xs:element> "
            + "<xs:complexType name='DeploymentDocumentsType'> " + "<xs:sequence> "
            + "<xs:element name='BPEL' type='xs:anyType' /> "
            + "<xs:element name='DeploymentDescriptor' type='xs:anyType' /> "
            + "<xs:element name='ProcessWSDL' type='tns:XMLFile' /> " +

            "<xs:element maxOccurs='unbounded' minOccurs='1' " + "name='ServiceWSDLs' type='tns:XMLFile' /> "
            + "<xs:element maxOccurs='unbounded' minOccurs='0' name='Other' " + "type='xs:anyType' /> "
            + "</xs:sequence> " + "</xs:complexType> " + "<xs:complexType name='XMLFile'> " + "<xs:sequence> "
            + "<xs:element name='FileName' type='xs:string' /> " + "<xs:element name='Content' type='xs:anyType'> "
            + "</xs:element> " +

            "</xs:sequence> " + "</xs:complexType> " + "<xs:element name='Result' type='xs:string' /> "
            + "<xs:element name='ProcessName' type='xs:string' /> " + "<xs:element name='CreateInstanceResponse'> "
            + "<xs:complexType> " + "<xs:sequence> " + "<xs:element name='ProcessName' type='xs:string' /> "
            + "<xs:element name='ProcessWSDL' type='xs:anyType'> " +

            "</xs:element> " + "</xs:sequence> " + "</xs:complexType> " + "</xs:element> " + "</xs:schema> "
            + "</wsdl:types> " + "<wsdl:message name='DeployRequest'> "
            + "<wsdl:part name='DeploymentInformation' element='tns:DeploymentInformation'> " + "</wsdl:part> " +

            "</wsdl:message> " + "<wsdl:message name='CreateInstanceResponse'> "
            + "<wsdl:part name='Response' element='tns:CreateInstanceResponse'> " + "</wsdl:part> "
            + "</wsdl:message> " + "<wsdl:message name='CreateInstanceRequest'> "
            + "<wsdl:part name='ProcessName' element='tns:ProcessName'> " + "</wsdl:part> " + "</wsdl:message> " +

            "<wsdl:message name='DeployResponse'> " + "<wsdl:part name='Response' element='tns:Result'> "
            + "</wsdl:part> " + "</wsdl:message> " + "<wsdl:portType name='WEPSPortType'> "
            + "<wsdl:operation name='deploy'> " + "<wsdl:input message='tns:DeployRequest'> " + "</wsdl:input> "
            + "<wsdl:output message='tns:DeployResponse'> " +

            "</wsdl:output> " + "</wsdl:operation> " + "<wsdl:operation name='createInstance'> "
            + "<wsdl:input message='tns:CreateInstanceRequest'> " + "</wsdl:input> "
            + "<wsdl:output message='tns:CreateInstanceResponse'> " + "</wsdl:output> " + "</wsdl:operation> "
            + "</wsdl:portType> " +

            "<wsdl:binding name='WEPSBinding' type='tns:WEPSPortType'> " + "<soap:binding style='document' "
            + "transport='http://schemas.xmlsoap.org/soap/http' /> " + "<wsdl:operation name='deploy'> "
            + "<soap:operation soapAction='urn:deploy' /> " + "<wsdl:input> " + "<soap:body use='literal' /> "
            + "</wsdl:input> " + "<wsdl:output> " + "<soap:body use='literal' /> " +

            "</wsdl:output> " + "</wsdl:operation> " + "<wsdl:operation name='createInstance'> "
            + "<soap:operation soapAction='urn:createInstance' /> " + "<wsdl:input> " + "<soap:body use='literal' /> "
            + "</wsdl:input> " + "<wsdl:output> " + "<soap:body use='literal' /> " +

            "</wsdl:output> " + "</wsdl:operation> " + "</wsdl:binding> " + "<wsdl:service name='WEPSService'> "
            + "<wsdl:port name='WEPS' binding='tns:WEPSBinding'> " + "<soap:address "
            + "location='http://silktree.cs.indiana.edu:18080/axis2/services/WEPSService' /> " + "</wsdl:port> "
            + "</wsdl:service> " + "</wsdl:definitions> ";

}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2009 The Trustees of Indiana University. All rights reserved.
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
