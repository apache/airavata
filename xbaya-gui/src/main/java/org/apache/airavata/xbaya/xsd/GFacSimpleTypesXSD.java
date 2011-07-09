/*
 * Copyright (c) 2009 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: $
 */
package org.apache.airavata.xbaya.xsd;

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
public class GFacSimpleTypesXSD {

    public static final String XSD = " <xsd:schema elementFormDefault='unqualified' targetNamespace='http://www.extreme.indiana.edu/lead/xsd' \n"
            + " 	xmlns='http://www.w3.org/2001/XMLSchema' \n"
            + " 	xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n"
            + " 	xmlns:gfac='http://www.extreme.indiana.edu/lead/xsd'>\n"
            + "      <xsd:simpleType name='LEADFileIDType'>\n"
            + "        <xsd:restriction base='xsd:anyURI' />\n"
            + "      </xsd:simpleType>\n"
            + "      <xsd:simpleType name='LEADNameListFileType'>\n"
            + "        <xsd:restriction base='xsd:anyURI' />\n"
            + "      </xsd:simpleType>\n"
            + "      <xsd:simpleType name='LEADNameListPropertiesFileType'>\n"
            + "        <xsd:restriction base='xsd:anyURI' />\n"
            + "      </xsd:simpleType>\n"
            + "      <xsd:simpleType name='HostNameType'>\n"
            + "        <xsd:restriction base='xsd:string' />\n"
            + "      </xsd:simpleType>\n"
            + "      <xsd:complexType name='DataIDType'>\n"
            + "        <xsd:simpleContent>\n"
            + "          <xsd:extension base='xsd:anyURI'>\n"
            + "            <xsd:attribute name='location' type='xsd:string' use='optional' />\n"
            + "          </xsd:extension>\n"
            + "        </xsd:simpleContent>\n"
            + "      </xsd:complexType>\n"
            + "      <xsd:complexType name='LEADFileIDArrayType'>\n"
            + "        <xsd:sequence>\n"
            + "          <xsd:element maxOccurs='unbounded' minOccurs='0' name='value' type='xsd:anyURI' />\n"
            + "        </xsd:sequence>\n"
            + "      </xsd:complexType>\n"
            + "      <xsd:complexType name='StringArrayType'>\n"
            + "        <xsd:sequence>\n"
            + "          <xsd:element maxOccurs='unbounded' minOccurs='0' name='value' type='xsd:string' />\n"
            + "        </xsd:sequence>\n"
            + "      </xsd:complexType>\n"
            + "      <xsd:complexType name='IntegerArrayType'>\n"
            + "        <xsd:sequence>\n"
            + "          <xsd:element maxOccurs='unbounded' minOccurs='0' name='value' type='xsd:int' />\n"
            + "        </xsd:sequence>\n"
            + "      </xsd:complexType>\n"
            + "      <xsd:complexType name='FloatArrayType'>\n"
            + "        <xsd:sequence>\n"
            + "          <xsd:element maxOccurs='unbounded' minOccurs='0' name='value' type='xsd:float' />\n"
            + "        </xsd:sequence>\n"
            + "      </xsd:complexType>\n"
            + "      <xsd:complexType name='DoubleArrayType'>\n"
            + "        <xsd:sequence>\n"
            + "          <xsd:element maxOccurs='unbounded' minOccurs='0' name='value' type='xsd:double' />\n"
            + "        </xsd:sequence>\n"
            + "      </xsd:complexType>\n"
            + "      <xsd:complexType name='BooleanArrayType'>\n"
            + "        <xsd:sequence>\n"
            + "          <xsd:element maxOccurs='unbounded' minOccurs='0' name='value' type='xsd:boolean' />\n"
            + "        </xsd:sequence>\n"
            + "      </xsd:complexType>\n"
            + "      <xsd:complexType name='URIArrayType'>\n"
            + "        <xsd:sequence>\n"
            + "          <xsd:element maxOccurs='unbounded' minOccurs='0' name='value' type='xsd:anyURI' />\n"
            + "        </xsd:sequence>\n"
            + "      </xsd:complexType>\n"
            + "      <xsd:complexType name='DataIDArrayType'>\n"
            + "        <xsd:sequence>\n"
            + "          <xsd:element maxOccurs='unbounded' minOccurs='0' name='value' type='gfac:DataIDType' />\n"
            + "        </xsd:sequence>\n" + "      </xsd:complexType>\n" + "    </xsd:schema>\n";

    /**
     * @return
     */
    public static String getXml() {
        // TODO Auto-generated method stub
        return XSD;
    }

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
