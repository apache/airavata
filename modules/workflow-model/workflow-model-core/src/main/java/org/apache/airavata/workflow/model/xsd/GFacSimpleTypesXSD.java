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
package org.apache.airavata.workflow.model.xsd;

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