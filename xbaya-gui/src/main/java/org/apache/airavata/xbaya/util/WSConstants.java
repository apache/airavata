/*
 * Copyright (c) 2005 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: WSConstants.java,v 1.9 2008/11/11 20:24:04 cherath Exp $
 */
package org.apache.airavata.xbaya.util;

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

import javax.xml.namespace.QName;

import org.xmlpull.infoset.XmlNamespace;

import xsul5.XmlConstants;

/**
 * @author Satoshi Shirasuna
 */
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

    /**
     * XML Schema Namespace
     */
    public static final XmlNamespace XSD_NS = XmlConstants.BUILDER.newNamespace(XSD_NS_PREFIX, XSD_NS_URI);

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
    public static final String EMPTY_APPINFO = "<appinfo xmlns=\"http://www.w3.org/2001/XMLSchema\">\n\n</appinfo>";

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
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2005 The Trustees of Indiana University. All rights reserved.
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
