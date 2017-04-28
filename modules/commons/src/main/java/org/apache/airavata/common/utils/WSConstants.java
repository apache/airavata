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

import javax.xml.namespace.QName;

import org.xmlpull.infoset.XmlNamespace;

public interface WSConstants {

    /**
     * xmlns
     */
    public final static String XMLNS = "xmlns";

    /**
     * XML Schema prefix, xsd
     */
    public static final String XSD_NS_PREFIX = "xsd";

    /**
     * XML Schema URI.
     */
    public static final String XSD_NS_URI = "http://www.w3.org/2001/XMLSchema";

//    /**
//     * XML Schema Namespace
//     */
//    public static final XmlNamespace XSD_NS = XmlConstants.BUILDER.newNamespace(XSD_NS_PREFIX, XSD_NS_URI);

    /**
     * The any type.
     */
    public static final QName XSD_ANY_TYPE = new QName(XSD_NS_URI, "any", XSD_NS_PREFIX);

    /**
     * xsd:anyURI
     */
    public static final QName XSD_ANY_URI = new QName(XSD_NS_URI, "anyURI", XSD_NS_PREFIX);

    /**
     * tns
     */
    public static final String TARGET_NS_PREFIX = "tns";

    /**
     * typens
     */
    public static final String TYPE_NS_PREFIX = "typens";

    /**
     * schema
     */
    public static final String SCHEMA_TAG = "schema";

    /**
     * Element name for annotation, annotation
     */
    public static final String ANNOTATION_TAG = "annotation";

    /**
     * Element name for documentation, documentation
     */
    public static final String DOCUMENTATION_TAG = "documentation";

    /**
     * appinfo
     */
    public static final String APPINFO_TAG = "appinfo";

    /**
     * element
     */
    public static final String ELEMENT_TAG = "element";

    /**
     * sequence
     */
    public static final String SEQUENCE_TAG = "sequence";

    /**
     * complexType
     */
    public static final String COMPLEX_TYPE_TAG = "complexType";

    /**
     * simpleType
     */
    public static final String SIMPLE_TYPE_TAG = "simpleType";

    /**
     * name
     */
    public static final String NAME_ATTRIBUTE = "name";

    /**
     * type
     */
    public static final String TYPE_ATTRIBUTE = "type";

    /**
     * targetNamespace
     */
    public static final String TARGET_NAMESPACE_ATTRIBUTE = "targetNamespace";

    /**
     * elementFormDefault
     */
    public final static String ELEMENT_FORM_DEFAULT_ATTRIBUTE = "elementFormDefault";

    /**
     * unqualified
     */
    public final static String UNQUALIFIED_VALUE = "unqualified";

    /**
     * default
     */
    public static final String DEFAULT_ATTRIBUTE = "default";

    /**
     * UsingAddressing
     */
    public static final String USING_ADDRESSING_TAG = "UsingAddressing";

    /**
     * <appinfo xmlns="http://www.w3.org/2001/XMLSchema">
     * 
     * </appinfo>
     */
//    public static final String EMPTY_APPINFO = "<appinfo xmlns=\"http://www.w3.org/2001/XMLSchema\">\n\n</appinfo>";
    public static final String EMPTY_APPINFO = "{'appinfo': '' }";

    /**
     * minOccurs
     */
    public static final String MIN_OCCURS_ATTRIBUTE = "minOccurs";

    /**
     * maxOccurs
     */
    public static final String MAX_OCCURS_ATTRIBUTE = "maxOccurs";

    /**
     * unbounded
     */
    public static final String UNBOUNDED_VALUE = "unbounded";

    /**
     * import
     */
    public static final String IMPORT_TAG = "import";

    /**
     * schemaLocation
     */
    public static final String SCHEMA_LOCATION_ATTRIBUTE = "schemaLocation";

    public static final String LEAD_NS_URI = "http://www.extreme.indiana.edu/lead";

    /**
     * The any type.
     */
    public static final QName LEAD_ANY_TYPE = new QName(LEAD_NS_URI, "any",
            XSD_NS_PREFIX);


}