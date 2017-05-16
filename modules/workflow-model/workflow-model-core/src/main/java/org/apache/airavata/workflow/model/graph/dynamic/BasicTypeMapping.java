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
package org.apache.airavata.workflow.model.graph.dynamic;

import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.xmlpull.infoset.XmlAttribute;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlNamespace;

public class BasicTypeMapping {

    /**
     * STR_UNBOUNDED
     */
    private static final String STR_UNBOUNDED = "unbounded";

    /**
     * STR_MAX_OCCURS
     */
    private static final String STR_MAX_OCCURS = "maxOccurs";

    /**
     * STR_TYPE
     */
    private static final String STR_TYPE = "type";

    /**
     * NUM
     */
    private static final int NUM = 2;

    private static QName[] TYPES = new QName[NUM];

    private static String[] NAMES = new String[NUM * 2];

    private static String[] DEFAULTS = new String[NUM * 2];

    private static String[] VAR_NAMES = new String[NUM * 2];

    public static final QName STRING_QNAME = new QName("http://www.w3.org/2001/XMLSchema", "string");
    public static QName INT_QNAME = new QName("http://www.w3.org/2001/XMLSchema", "int");

    /**
     * Used to generate string1, string2, string3 ...
     */
    public static int uniqueVarappend = 0;

    static {

        TYPES[0] = INT_QNAME;

        TYPES[1] = STRING_QNAME;

        NAMES[0] = "int";
        NAMES[1] = "String";
        NAMES[0 + NUM] = "int[]";
        NAMES[1 + NUM] = "String[]";

        DEFAULTS[0] = "0";
        DEFAULTS[1] = "null";
        DEFAULTS[0 + NUM] = "null";
        DEFAULTS[1 + NUM] = "null";

        VAR_NAMES[0] = "intVal";
        VAR_NAMES[1] = "string";
        VAR_NAMES[0 + NUM] = "intArray";
        VAR_NAMES[1 + NUM] = "stringArray";

    }

    /**
     * REturns the index of the simple type which can be used to get the Java typename, etc. rerurns -1 if not a simple
     * type.
     * 
     * @param qname
     * @return
     */
    public static int getSimpleTypeIndex(QName qname) {
        for (int i = 0; i < TYPES.length; ++i) {
            if (TYPES[i].equals(qname)) {
                return i;
            }
        }
        return -1;
    }

    public static String getTypeName(int index) {
        if (index >= 2 * NUM || index < 0) {
            throw new IllegalStateException("Invalid Index");
        }
        return NAMES[index];
    }

    public static String getTypeName(QName qName) {
        return getTypeName(getSimpleTypeIndex(qName));
    }

    public static String getTypeDefault(int index) {
        if (index >= 2 * NUM || index < 0) {
            throw new IllegalStateException("Invalid Index");
        }
        return DEFAULTS[index];
    }

    public static String getTypeDefault(QName qName) {
        return getTypeDefault(getSimpleTypeIndex(qName));
    }

    public static String getTypeVariableName(int index) {
        if (index >= 2 * NUM || index < 0) {
            throw new IllegalStateException("Invalid Index");
        }
        return VAR_NAMES[index] + getUniqueVarAppender();
    }

    public static String getTypeVariableName(QName qName) {
        return getTypeVariableName(getSimpleTypeIndex(qName));
    }

    private synchronized static int getUniqueVarAppender() {
        return uniqueVarappend++;
    }

    public static void reset() {
        uniqueVarappend = 0;
    }

    public static Object getObjectOfType(int index, Object obj) {
        switch (index) {
        case 0:
            return new Integer((String) obj);
        case 1:
            return obj;
        }
        throw new WorkflowRuntimeException("type Not Supported yet!!");
    }

    public static Object getObjectOfType(QName qName, Object obj) {
        return getObjectOfType(getSimpleTypeIndex(qName), obj);
    }

    public static int getSimpleTypeIndex(XmlElement element) {

        XmlAttribute type = element.attribute(STR_TYPE);
        if (null == type) {
            return -1;
        }
        String typeQNameString = type.getValue();
        String typeName = XMLUtil.getLocalPartOfQName(typeQNameString);
        String prefix = XMLUtil.getPrefixOfQName(typeQNameString);
        XmlNamespace namespace = element.lookupNamespaceByPrefix(prefix);
        if (namespace == null) {
            return -1;
        }
        QName typeQname = new QName(namespace.getName(), typeName, prefix);
        int simpleTypeIndex = getSimpleTypeIndex(typeQname);
        if (-1 == simpleTypeIndex) {
            return -1;
        }

        XmlAttribute maxOccurs = element.attribute(STR_MAX_OCCURS);
        if (maxOccurs != null && STR_UNBOUNDED.equals(maxOccurs.getValue())) {
            // this is the comvention the arraytype index is simplextye index + NUM
            return NUM + simpleTypeIndex;
        } else {
            return simpleTypeIndex;
        }
    }

    public static boolean isArrayType(XmlElement element) {
        int index = getSimpleTypeIndex(element);
        if (index >= NUM) {
            return true;
        }
        return false;
    }

    public static Object getOutputArray(XmlElement element, String name, int simpleIndex) throws WorkflowException {
        try {
            // This code doesn't work when the output is a complex type.
            // Object output = this.outputMessage.getObjectPart(name);
            // return output;

            XmlElement outputElement = (XmlElement) element;
            Iterator valueElementItr = outputElement.elements(null, name).iterator();
            LinkedList<String> ret = new LinkedList<String>();
            while (valueElementItr.hasNext()) {
                XmlElement valueElement = (XmlElement) valueElementItr.next();
                Iterator childIt = valueElement.children().iterator();
                int numberOfChildren = 0;
                while (childIt.hasNext()) {
                    childIt.next();
                    numberOfChildren++;
                }
                if (numberOfChildren == 1) {
                    Object child = valueElement.children().iterator().next();
                    if (child instanceof String) {
                        // Value is a simple type. Return the string.
                        String value = (String) child;
                        ret.add(value);
                    }
                }
            }
            switch (simpleIndex) {
            case 0:
                Integer[] intRet = new Integer[ret.size()];
                for (int i = 0; i < ret.size(); i++) {
                    intRet[i] = Integer.parseInt(ret.get(i));
                }
                return intRet;
            case 1:
                String[] strRet = new String[ret.size()];
                for (int i = 0; i < ret.size(); i++) {
                    strRet[i] = ret.get(i);
                }
                return strRet;
            }
            throw new WorkflowException("Unknown type");

        } catch (RuntimeException e) {
            String message = "Error in getting output. name: " + name;
            throw new WorkflowException(message, e);
        }
    }

}