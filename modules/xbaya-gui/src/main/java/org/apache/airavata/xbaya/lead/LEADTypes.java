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

package org.apache.airavata.xbaya.lead;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.WSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LEADTypes {

    private static final Logger logger = LoggerFactory.getLogger(LEADTypes.class);

    /**
     * http://www.extreme.indiana.edu/lead/xsd
     */
    public static final String LEAD_XSD_NS_URI = "http://airavata.apache.org/schemas/gfac/2012/12";

    // Simple types

    /**
     * LEADFileIDType
     */
    public static final QName LEAD_FILE_ID_TYPE = new QName(LEAD_XSD_NS_URI, "LEADFileIDType");

    /**
     * DATA_ID_TYPE
     */
    public static final QName DATA_ID_TYPE = new QName(LEAD_XSD_NS_URI, "DataIDType");

    /**
     * LEADWorkflowIDType
     */
    public static final QName LEAD_WORKFLOW_ID_TYPE = new QName(LEAD_XSD_NS_URI, "LEADWorkflowIDType");

    /**
     * LEADNameListFileType
     */
    public static final QName LEAD_NAME_LIST_FILE_TYPE = new QName(LEAD_XSD_NS_URI, "LEADNameListFileType");

    /**
     * LEADNameListPropertiesFileType
     */
    public static final QName LEAD_NAME_LIST_PROPERTIES_FILE_TYPE = new QName(LEAD_XSD_NS_URI,
            "LEADNameListPropertiesFileType");

    /**
     * HostNameType
     */
    public static final QName HOST_NAME_TYPE = new QName(LEAD_XSD_NS_URI, "HostNameType");

    // Array types

    /**
     * StringArrayType
     */
    public static final QName STRING_ARRAY_TYPE = new QName(LEAD_XSD_NS_URI, "StringArrayType");

    /**
     * IntegerArrayType
     */
    public static final QName INTEGER_ARRAY_TYPE = new QName(LEAD_XSD_NS_URI, "IntegerArrayType");

    /**
     * FloatArrayType
     */
    public static final QName FLOAT_ARRAY_TYPE = new QName(LEAD_XSD_NS_URI, "FloatArrayType");

    /**
     * DoubleArrayType
     */
    public static final QName DOUBLE_ARRAY_TYPE = new QName(LEAD_XSD_NS_URI, "DoubleArrayType");

    /**
     * BooleanArrayType
     */
    public static final QName BOOLEAN_ARRAY_TYPE = new QName(LEAD_XSD_NS_URI, "BooleanArrayType");

    /**
     * QNameArrayType
     */
    public static final QName QNAME_ARRAY_TYPE = new QName(LEAD_XSD_NS_URI, "QNameArrayType");

    /**
     * URIArrayType
     */
    public static final QName URI_ARRAY_TYPE = new QName(LEAD_XSD_NS_URI, "URIArrayType");

    /**
     * LEADFileIDArrayType
     */
    public static final QName LEAD_FILE_ID_ARRAY_TYPE = new QName(LEAD_XSD_NS_URI, "LEADFileIDArrayType");

    /**
     * DATA_ID_ARRAY_TYPE
     */
    public static final QName DATA_ID_ARRAY_TYPE = new QName(LEAD_XSD_NS_URI, "DataIDArrayType");

    public static final QName STRING_TYPE = new QName(LEAD_XSD_NS_URI, "StringParameterType");

      /**
       * IntegerArrayType
       */
      public static final QName INTEGER_TYPE = new QName(LEAD_XSD_NS_URI, "IntegerParameterType");

      /**
       * FloatArrayType
       */
      public static final QName FLOAT_TYPE = new QName(LEAD_XSD_NS_URI, "FloatParameterType");

      /**
       * DoubleArrayType
       */
      public static final QName DOUBLE_TYPE = new QName(LEAD_XSD_NS_URI, "DoubleParameterType");

      /**
       * BooleanArrayType
       */
      public static final QName BOOLEAN_TYPE = new QName(LEAD_XSD_NS_URI, "BooleanParameterType");

      /**
       * URIArrayType
       */
      public static final QName URI_TYPE = new QName(LEAD_XSD_NS_URI, "URIParameterType");


    /**
     * Checks if a specified type is known. If the type is known, the GUI accepts string as a user's input. If not
     * known, the GUI accepts XML as the input.
     * 
     * @param type
     * @return true if the type is known; otherwise false;
     */
    public static boolean isKnownType(QName type) {
        if (WSConstants.XSD_ANY_TYPE.equals(type)) {
            // we need to input XML directly for xsd:any
            return false;
        } else if (WSConstants.XSD_NS_URI.equals(type.getNamespaceURI())) {
            return true;
        } else if (LEAD_FILE_ID_TYPE.equals(type) || DATA_ID_TYPE.equals(type) || LEAD_WORKFLOW_ID_TYPE.equals(type)
                || LEAD_NAME_LIST_FILE_TYPE.equals(type) || LEAD_NAME_LIST_PROPERTIES_FILE_TYPE.equals(type)
                || HOST_NAME_TYPE.equals(type) || STRING_ARRAY_TYPE.equals(type) || INTEGER_ARRAY_TYPE.equals(type)
                || FLOAT_ARRAY_TYPE.equals(type) || DOUBLE_ARRAY_TYPE.equals(type) || BOOLEAN_ARRAY_TYPE.equals(type)
                || QNAME_ARRAY_TYPE.equals(type) || URI_ARRAY_TYPE.equals(type) || LEAD_FILE_ID_ARRAY_TYPE.equals(type)
                || DATA_ID_ARRAY_TYPE.equals(type) || STRING_TYPE.equals(type) || URI_TYPE.equals(type)
                || INTEGER_TYPE.equals(type) || FLOAT_TYPE.equals(type) || DOUBLE_TYPE.equals(type)
                || BOOLEAN_TYPE.equals(type)) {
            return true;
        } else if (DATA_ID_TYPE.getLocalPart().equals(type.getLocalPart())) {
            // XXX temporary hack.
            logger.warn("The name space of " + type.getLocalPart() + " should be " + DATA_ID_TYPE.getNamespaceURI()
                    + ", not " + type.getNamespaceURI() + ".");
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param type
     * @return true if type is an uri type; false otherwise.
     */
    public static boolean isURIType(QName type) {
        if (WSConstants.XSD_ANY_URI.equals(type) || LEAD_NAME_LIST_PROPERTIES_FILE_TYPE.equals(type)
                || LEAD_FILE_ID_TYPE.equals(type) || LEAD_NAME_LIST_FILE_TYPE.equals(type)
                || LEAD_WORKFLOW_ID_TYPE.equals(type) || URI_TYPE.equals(type)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param type
     * @return true if type is an array type; false otherwise.
     */
    public static boolean isArrayType(QName type) {
        if (STRING_ARRAY_TYPE.equals(type) || INTEGER_ARRAY_TYPE.equals(type) || FLOAT_ARRAY_TYPE.equals(type)
                || DOUBLE_ARRAY_TYPE.equals(type) || BOOLEAN_ARRAY_TYPE.equals(type) || QNAME_ARRAY_TYPE.equals(type)
                || URI_ARRAY_TYPE.equals(type) || LEAD_FILE_ID_ARRAY_TYPE.equals(type)
                || DATA_ID_ARRAY_TYPE.equals(type)) {
            return true;
        } else if (LEAD_FILE_ID_ARRAY_TYPE.getLocalPart().equals(type.getLocalPart())) {
            // TODO remove this.
            // for workflow input message created from workflow template
            logger.warn("The name space of " + type.getLocalPart() + " should be "
                    + LEAD_FILE_ID_ARRAY_TYPE.getNamespaceURI() + ", not " + type.getNamespaceURI() + ".");
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param type
     * @return true if type is an uri array type; false otherwise.
     */
    public static boolean isURIArrayType(QName type) {
        if (URI_ARRAY_TYPE.equals(type) || LEAD_FILE_ID_ARRAY_TYPE.equals(type)) {
            return true;
        } else {
            return false;
        }
    }
}