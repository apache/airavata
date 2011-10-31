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

package org.apache.airavata.commons.gfac.wsdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.xml.namespace.QName;

import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.wsdl.extensions.schema.SchemaImpl;

public class TypesGenerator implements WSDLConstants {
    private static final String ENDPOINT_REFERENCE_TYPE = "EndpointReferenceType";

    public static Types addTypes(Definition def, DOMImplementation dImpl, ServiceDescriptionType serviceDesc,
            String typens, String globalTypens) {
        ArrayList<Element> schemaTable = new ArrayList<Element>();
        Vector complexTypes = new Vector();
        HashMap<String, String> knownTypes = new HashMap<String, String>();

        Element documentation = null;

        Document doc = dImpl.createDocument(typens, SCHEMA, null);

        Element schema = doc.createElementNS(XSD_NAMESPACE, "schema");
        // Element schema = doc.getDocumentElement();
        schema.setAttribute(TARGET_NAMESPACE, typens);
        schema.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.w3.org/2001/XMLSchema");
        // schema.setAttribute(XMLNS,
        // XSD_NAMESPACE);
        schema.setAttribute(ELEMENT_FORM_DEFAULT, UNQUALIFIED);

        Element globalSchema = doc.createElementNS(XSD_NAMESPACE, "schema");
        // Element globalSchema = doc.getDocumentElement();
        globalSchema.setAttribute(TARGET_NAMESPACE, globalTypens);
        globalSchema.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.w3.org/2001/XMLSchema");
        // globalSchema.setAttribute(XMLNS,
        // XSD_NAMESPACE);
        globalSchema.setAttribute(ELEMENT_FORM_DEFAULT, UNQUALIFIED);

        Types types = def.createTypes();

        InputParameterType[] inputParams = serviceDesc.getInputParametersArray();
        Vector inParamNames = new Vector();
        Vector inParamDesc = new Vector();
        Vector inParamTypes = new Vector();

        for (int k = 0; k < inputParams.length; ++k) {
            inParamNames.add(inputParams[k].getParameterName());
            inParamDesc.add(inputParams[k].getParameterDescription());
            inParamTypes.add(inputParams[k].getParameterType().getType().toString());
            inputParams[k].getParameterValueArray();
        }

        OutputParameterType[] outputParams = serviceDesc.getOutputParametersArray();
        Vector outParamNames = new Vector();
        Vector outParamDesc = new Vector();
        Vector outParamTypes = new Vector();

        for (int k = 0; k < outputParams.length; ++k) {
            outParamNames.add(outputParams[k].getParameterName());
            outParamDesc.add(outputParams[k].getParameterDescription());
            outParamTypes.add(outputParams[k].getParameterType().getType().toString());
        }

        String methodName = serviceDesc.getPortType().getMethod().getMethodName();
        WSDLMessageBean wsdlMsgBean = new WSDLMessageBean();
        wsdlMsgBean.setNamespace(typens);
        // Set the method name in the message object
        wsdlMsgBean.setMethodName(methodName);

        // === write all the input parameters ========
        // Input message name
        String inputMessageName = methodName + GFacSchemaConstants.SERVICE_IN_PARAMS_SUFFIX;

        // Set the input message name in the message object
        wsdlMsgBean.setInElementName(inputMessageName);

        // local data types
        Element wsdlInputParams = doc.createElement("element");
        String inputElementName = methodName + GFacSchemaConstants.SERVICE_IN_PARAMS_SUFFIX;
        String inputParametersType = methodName + GFacSchemaConstants.SERVICE_INPUT_PARAMS_TYPE_SUFFIX;

        wsdlInputParams.setAttribute("name", inputElementName);
        wsdlInputParams.setAttribute("type", TYPENS + ":" + inputParametersType);

        schema.appendChild(wsdlInputParams);
        // local data types

        wsdlMsgBean.setInMsgParamNames(inParamNames);
        wsdlMsgBean.setInMsgParamTypes(inParamTypes);

        Element first = doc.createElement(COMPLEX_TYPE);
        first.setAttribute("name", inputParametersType);

        Element sequence = doc.createElement("sequence");

        for (int j = 0; j < inParamNames.size(); ++j) {
            Element elem = doc.createElement("element");
            String paramName = (String) inParamNames.get(j);
            paramName = paramName.replaceAll(HYPHEN, HYPHEN_REPLACEMENT);
            elem.setAttribute("name", paramName);
            String dataType = (String) inParamTypes.get(j);

            if (dataType.equals("String")) {
                elem.setAttribute("type", XSD_STRING_TYPE);
            } else if (dataType.equals("Integer")) {
                elem.setAttribute("type", "xsd:int");
            } else if (dataType.equals("Float")) {
                elem.setAttribute("type", "xsd:float");
            } else if (dataType.equals("Double")) {
                elem.setAttribute("type", "xsd:double");
            } else if (dataType.equals("Boolean")) {
                elem.setAttribute("type", "xsd:boolean");
            } else if (dataType.equals("QName")) {
                elem.setAttribute("type", "xsd:QName");
            } else if (dataType.equals("URI")) {
                elem.setAttribute("type", "xsd:anyURI");
            }
            // schemas for complex types will be written later
            else if (dataType.equals("StringArray") || dataType.equals("IntegerArray") || dataType.equals("FloatArray")
                    || dataType.equals("DoubleArray") || dataType.equals("BooleanArray")
                    || dataType.equals("QNameArray") || dataType.equals("HostName")
                    || dataType.equals(GFacSchemaConstants.Types.TYPE_URI_ARRAY)
                    || dataType.equals(GFacSchemaConstants.Types.TYPE_DATAID)) {
                elem.setAttribute("type", GLOBAL_TYPENS + ":" + dataType + "Type");
                if (!complexTypes.contains(dataType)) {
                    complexTypes.add(dataType);
                }
            } else if (dataType.equals(GFacSchemaConstants.Types.TYPE_DATAID_ARRAY)) {
                elem.setAttribute("type", GLOBAL_TYPENS + ":" + dataType + "Type");
                if (!complexTypes.contains(GFacSchemaConstants.Types.TYPE_DATAID_ARRAY)) {
                    complexTypes.add(GFacSchemaConstants.Types.TYPE_DATAID_ARRAY);
                }
                if (!complexTypes.contains(GFacSchemaConstants.Types.TYPE_DATAID)) {
                    complexTypes.add(GFacSchemaConstants.Types.TYPE_DATAID);
                }
            } else if (dataType.equals("StringEnum") || dataType.equals("IntegerEnum") || dataType.equals("FloatEnum")
                    || dataType.equals("DoubleEnum")) {
                elem.setAttribute("type", TYPENS + ":" + methodName + "_" + paramName + "_" + dataType + "Type");
            } else if (dataType.equals(ENDPOINT_REFERENCE_TYPE)) {
                if (!knownTypes.containsKey(ENDPOINT_REFERENCE_TYPE)) {
                    schemaTable.add(createAddressingSchema(doc));
                    knownTypes.put(ENDPOINT_REFERENCE_TYPE, ENDPOINT_REFERENCE_TYPE);
                }
                elem.setAttribute("type", WSDLGenerator.WSA_PREFIX + ":" + ENDPOINT_REFERENCE_TYPE);
            }

            Element annotation = doc.createElement(ANNOTATION);

            // TODO create Metadata

            WSDLGenerator.createMetadata(inputParams[j], doc, annotation);

            documentation = doc.createElement(DOCUMENTATION);
            documentation.appendChild(doc.createTextNode((String) inParamDesc.get(j)));
            annotation.appendChild(documentation);
            elem.appendChild(annotation);
            sequence.appendChild(elem);
        } // end for inParams

        first.appendChild(sequence);
        schema.appendChild(first);

        // write the enumeration types
        for (int j = 0; j < inParamNames.size(); ++j) {
            String dataType = (String) inParamTypes.get(j);
            if (dataType.equals("StringEnum") || dataType.equals("IntegerEnum") || dataType.equals("FloatEnum")
                    || dataType.equals("DoubleEnum")) {
                String paramName = (String) inParamNames.get(j);
                String[] paramValues = serviceDesc.getInputParametersArray(j).getParameterValueArray();
                if (paramValues == null)
                    continue;
                Element elem = doc.createElement(SIMPLE_TYPE);
                elem.setAttribute("name", methodName + "_" + paramName + "_" + dataType + "Type");

                for (int k = 0; k < paramValues.length; ++k) {
                    documentation = doc.createElement(DOCUMENTATION);
                    // documentation.setAttribute(XML_LANG,
                    // ENGLISH);

                    Element value = doc.createElement("value");
                    value.appendChild(doc.createTextNode(paramValues[k]));
                    documentation.appendChild(value);
                    elem.appendChild(documentation);
                }

                Element restriction = doc.createElement("restriction");
                if (dataType.equals("StringEnum")) {
                    restriction.setAttribute("base", "xsd:string");
                } else if (dataType.equals("IntegerEnum")) {
                    restriction.setAttribute("base", "xsd:int");
                } else if (dataType.equals("FloatEnum")) {
                    restriction.setAttribute("base", "xsd:float");
                } else if (dataType.equals("DoubleEnum")) {
                    restriction.setAttribute("base", "xsd:double");
                }
                for (int k = 0; k < paramValues.length; ++k) {
                    Element enumeration = doc.createElement("enumeration");
                    enumeration.setAttribute("value", paramValues[k]);
                    restriction.appendChild(enumeration);
                }
                elem.appendChild(restriction);
                schema.appendChild(elem);
            }
        }

        // ====== write the output parameters ==============
        // Output message name
        if (outputParams.length > 0) {
            String outputMessageName = methodName + GFacSchemaConstants.SERVICE_OUT_PARAMS_SUFFIX;
            // Set the output message name in the message object
            wsdlMsgBean.setOutElementName(outputMessageName);
            Element wsdlOutputParams = doc.createElement("element");
            String outputElementName = methodName + GFacSchemaConstants.SERVICE_OUT_PARAMS_SUFFIX;
            String outputParametersType = methodName + GFacSchemaConstants.SERVICE_OUTPUT_PARAMS_TYPE_SUFFIX;
            wsdlOutputParams.setAttribute("name", outputElementName);
            wsdlOutputParams.setAttribute("type", TYPENS + ":" + outputParametersType);
            schema.appendChild(wsdlOutputParams);
            wsdlMsgBean.setOutMsgParamNames(outParamNames);
            wsdlMsgBean.setOutMsgParamTypes(outParamTypes);

            // Now add the output parameters
            first = doc.createElement(COMPLEX_TYPE);
            first.setAttribute("name", outputParametersType);
            sequence = doc.createElement("sequence");

            for (int j = 0; j < outParamNames.size(); ++j) {
                Element elem = doc.createElement("element");
                elem.setAttribute("name", (String) outParamNames.get(j));
                String dataType = (String) outParamTypes.get(j);

                if (dataType.equals("String") || dataType.equals("StdOut") || dataType.equals("StdErr")) {
                    elem.setAttribute("type", XSD_STRING_TYPE);
                } else if (dataType.equals("Integer")) {
                    elem.setAttribute("type", "xsd:int");
                } else if (dataType.equals("Float")) {
                    elem.setAttribute("type", "xsd:float");
                } else if (dataType.equals("Double")) {
                    elem.setAttribute("type", "xsd:double");
                } else if (dataType.equals("Boolean")) {
                    elem.setAttribute("type", "xsd:boolean");
                } else if (dataType.equals("URI")) {
                    elem.setAttribute("type", "xsd:anyURI");
                } else if (dataType.equals("QName")) {
                    elem.setAttribute("type", "xsd:QName");
                } else if (dataType.equals("StringArray") || dataType.equals("IntegerArray")
                        || dataType.equals("FloatArray") || dataType.equals("DoubleArray")
                        || dataType.equals("BooleanArray") || dataType.equals("QNameArray")
                        || dataType.equals("LEADFileID") || dataType.equals("LEADFileIDArray")
                        || dataType.equals(GFacSchemaConstants.Types.TYPE_DATAID)
                        || dataType.equals(GFacSchemaConstants.Types.TYPE_URI_ARRAY)) {
                    elem.setAttribute("type", GLOBAL_TYPENS + ":" + dataType + "Type");
                    if (!complexTypes.contains(dataType)) {
                        complexTypes.add(dataType);
                    }
                } else if (dataType.equals(GFacSchemaConstants.Types.TYPE_DATAID_ARRAY)) {
                    elem.setAttribute("type", GLOBAL_TYPENS + ":" + dataType + "Type");
                    if (!complexTypes.contains(GFacSchemaConstants.Types.TYPE_DATAID_ARRAY)) {
                        complexTypes.add(GFacSchemaConstants.Types.TYPE_DATAID_ARRAY);
                    }
                    if (!complexTypes.contains(GFacSchemaConstants.Types.TYPE_DATAID)) {
                        complexTypes.add(GFacSchemaConstants.Types.TYPE_DATAID);
                    }
                }

                // Create an annotation
                Element annotation = doc.createElement(ANNOTATION);
                WSDLGenerator.createMetadata(serviceDesc.getOutputParametersArray(j), doc, annotation);
                documentation = doc.createElement(DOCUMENTATION);
                documentation.appendChild(doc.createTextNode((String) outParamDesc.get(j)));
                annotation.appendChild(documentation);
                elem.appendChild(annotation);
                sequence.appendChild(elem);

            } // end for outParamNames.size()
        }

        first.appendChild(sequence);
        schema.appendChild(first);

        // write the schemas for the complex types in the input and output
        // parameters

        for (int i = 0; i < complexTypes.size(); ++i) {
            Element wsdlFirstElement = doc.createElement(COMPLEX_TYPE);
            String dataType = (String) complexTypes.get(i);
            first.setAttribute("name", dataType + "Type");
            Element wsdlSequence = doc.createElement("sequence");
            Element elem = doc.createElement("element");
            elem.setAttribute("name", GFacSchemaConstants.ARRAY_VALUE);

            if (dataType.equals("StringArray")) {
                elem.setAttribute("type", "xsd:string");
            } else if (dataType.equals("IntegerArray")) {
                elem.setAttribute("type", "xsd:int");
            } else if (dataType.equals("FloatArray")) {
                elem.setAttribute("type", "xsd:float");
            } else if (dataType.equals("DoubleArray")) {
                elem.setAttribute("type", "xsd:double");
            } else if (dataType.equals("BooleanArray")) {
                elem.setAttribute("type", "xsd:boolean");
            } else if (dataType.equals("QNameArray")) {
                elem.setAttribute("type", "xsd:anyQName");
            } else {
                continue;
            }

            elem.setAttribute("minOccurs", "0");
            elem.setAttribute("maxOccurs", "unbounded");
            wsdlSequence.appendChild(elem);
            first.appendChild(wsdlSequence);
            globalSchema.appendChild(first);
        }

        // types.setDocumentationElement(schema);
        SchemaImpl schemaImpl = new SchemaImpl();
        // SchemaImportImpl schemaimport = new SchemaImportImpl();
        // schemaimport.setNamespaceURI("http://www.extreme.indiana.edu/lead/xsd");
        // schemaimport.setSchemaLocationURI("http://www.extreme.indiana.edu/gfac/gfac-simple-types.xsd");
        // schemaImpl.addImport(schemaimport);

        Element importEle = doc.createElement("import");
        importEle.setAttribute("namespace", "http://www.extreme.indiana.edu/lead/xsd");
        importEle.setAttribute("schemaLocation", "http://www.extreme.indiana.edu/gfac/gfac-simple-types.xsd");
        schema.insertBefore(importEle, schema.getFirstChild());

        // schema.appendChild();

        schemaImpl.setElement(schema);
        schemaImpl.setElementType(new QName("http://www.w3.org/2001/XMLSchema", "schema"));

        SchemaImpl globalSchemaImpl = new SchemaImpl();
        globalSchemaImpl.setElement(globalSchema);
        globalSchemaImpl.setElementType(new QName("http://www.w3.org/2001/XMLSchema", "schema"));
        globalSchemaImpl.getElement().setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");

        types.addExtensibilityElement(schemaImpl);

        return types;
    }

    public static Element createAddressingSchema(Document doc) {
        Element wsAddressing = doc.createElementNS(XSD_NAMESPACE, "schema");
        // Element globalSchema = doc.getDocumentElement();
        wsAddressing.setAttribute(TARGET_NAMESPACE, "http://www.w3.org/2005/08/addressing");
        wsAddressing.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.w3.org/2001/XMLSchema");
        wsAddressing.setAttribute("attributeFormDefault", "unqualified");
        wsAddressing.setAttribute(ELEMENT_FORM_DEFAULT, "qualified");

        Element type = doc.createElement("complexType");
        type.setAttribute("name", ENDPOINT_REFERENCE_TYPE);

        Element sequence = doc.createElement("sequence");
        Element any = doc.createElement("any");
        any.setAttribute("namespace", "##any");
        any.setAttribute("minOccurs", "0");

        sequence.appendChild(any);
        type.appendChild(sequence);
        wsAddressing.appendChild(type);

        return wsAddressing;
    }
}