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

package org.apache.airavata.xbaya.interpretor;

/*
 *  WorkflowInterpretorStub java implementation
 */

public class WorkflowInterpretorStub extends org.apache.axis2.client.Stub {
    protected org.apache.axis2.description.AxisOperation[] _operations;

    // hashmaps to keep the fault mapping
    private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
    private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
    private java.util.HashMap faultMessageMap = new java.util.HashMap();

    private static int counter = 0;

    private static synchronized String getUniqueSuffix() {
        // reset the counter if it is greater than 99999
        if (counter > 99999) {
            counter = 0;
        }
        counter = counter + 1;
        return Long.toString(System.currentTimeMillis()) + "_" + counter;
    }

    private void populateAxisService() throws org.apache.axis2.AxisFault {

        // creating the Service with a unique name
        _service = new org.apache.axis2.description.AxisService("WorkflowInterpretor" + getUniqueSuffix());
        addAnonymousOperations();

        // creating the operations
        org.apache.axis2.description.AxisOperation __operation;

        _operations = new org.apache.axis2.description.AxisOperation[1];

        __operation = new org.apache.axis2.description.OutInAxisOperation();

        __operation.setName(new javax.xml.namespace.QName("http://interpretor.xbaya.airavata.apache.org",
                "launchWorkflow"));
        _service.addOperation(__operation);

        _operations[0] = __operation;

    }

    // populates the faults
    private void populateFaults() {

    }

    /**
     * Constructor that takes in a configContext
     */

    public WorkflowInterpretorStub(org.apache.axis2.context.ConfigurationContext configurationContext,
            java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(configurationContext, targetEndpoint, false);
    }

    /**
     * Constructor that takes in a configContext and useseperate listner
     */
    public WorkflowInterpretorStub(org.apache.axis2.context.ConfigurationContext configurationContext,
            java.lang.String targetEndpoint, boolean useSeparateListener) throws org.apache.axis2.AxisFault {
        // To populate AxisService
        populateAxisService();
        populateFaults();

        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext, _service);

        configurationContext = _serviceClient.getServiceContext().getConfigurationContext();

        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(targetEndpoint));
        _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);

    }

    /**
     * Default Constructor
     */
    public WorkflowInterpretorStub(org.apache.axis2.context.ConfigurationContext configurationContext)
            throws org.apache.axis2.AxisFault {

        this(configurationContext, "http://silktree.cs.indiana.edu:18080/axis2/services/WorkflowInterpretor");

    }

    /**
     * Default Constructor
     */
    public WorkflowInterpretorStub() throws org.apache.axis2.AxisFault {

        this("http://silktree.cs.indiana.edu:18080/axis2/services/WorkflowInterpretor");

    }

    /**
     * Constructor taking the target endpoint
     */
    public WorkflowInterpretorStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null, targetEndpoint);
    }

    /**
     * Auto generated method signature
     * 
     * @see org.apache.airavata.xbaya.WorkflowInterpretor#launchWorkflow
     * @param launchWorkflow0
     */

    public java.lang.String launchWorkflow(

    java.lang.String workflowAsString1, java.lang.String topic2, java.lang.String password3,
            java.lang.String username4, NameValue[] inputs5, NameValue[] configurations6)

    throws java.rmi.RemoteException

    {
        org.apache.axis2.context.MessageContext _messageContext = null;
        try {
            org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0]
                    .getName());
            _operationClient.getOptions().setAction("urn:launchWorkflow");
            _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

            addPropertyToOperationClient(_operationClient,
                    org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

            // create a message context
            _messageContext = new org.apache.axis2.context.MessageContext();

            // create SOAP envelope with that payload
            org.apache.axiom.soap.SOAPEnvelope env = null;
            WorkflowInterpretorStub.LaunchWorkflow dummyWrappedType = null;
            env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), workflowAsString1, topic2,
                    password3, username4, inputs5, configurations6, dummyWrappedType,
                    optimizeContent(new javax.xml.namespace.QName("http://interpretor.xbaya.airavata.apache.org",
                            "launchWorkflow")));

            // adding SOAP soap_headers
            _serviceClient.addHeadersToEnvelope(env);
            // set the message context with that soap envelope
            _messageContext.setEnvelope(env);

            // add the message contxt to the operation client
            _operationClient.addMessageContext(_messageContext);

            // execute the operation client
            _operationClient.execute(true);

            org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
                    .getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

            java.lang.Object object = fromOM(_returnEnv.getBody().getFirstElement(),
                    WorkflowInterpretorStub.LaunchWorkflowResponse.class, getEnvelopeNamespaces(_returnEnv));

            return getLaunchWorkflowResponse_return((WorkflowInterpretorStub.LaunchWorkflowResponse) object);

        } catch (org.apache.axis2.AxisFault f) {

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt != null) {
                if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
                    // make the fault by reflection
                    try {
                        java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap
                                .get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                        // message class
                        java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                new java.lang.Class[] { messageClass });
                        m.invoke(ex, new java.lang.Object[] { messageObject });

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    } catch (java.lang.ClassCastException e) {
                        // we cannot intantiate the class - throw the original
                        // Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original
                        // Axis fault
                        throw f;
                    } catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original
                        // Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original
                        // Axis fault
                        throw f;
                    } catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original
                        // Axis fault
                        throw f;
                    } catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original
                        // Axis fault
                        throw f;
                    }
                } else {
                    throw f;
                }
            } else {
                throw f;
            }
        } finally {
            _messageContext.getTransportOut().getSender().cleanup(_messageContext);
        }
    }

    /**
     * Auto generated method signature for Asynchronous Invocations
     * 
     * @see org.apache.airavata.xbaya.WorkflowInterpretor#startlaunchWorkflow
     * @param launchWorkflow0
     */
    public void startlaunchWorkflow(

    java.lang.String workflowAsString1, java.lang.String topic2, java.lang.String password3,
            java.lang.String username4, NameValue[] inputs5, NameValue[] configurations6,

            final WorkflowInterpretorCallbackHandler callback)

    throws java.rmi.RemoteException {

        org.apache.axis2.client.OperationClient _operationClient = _serviceClient
                .createClient(_operations[0].getName());
        _operationClient.getOptions().setAction("urn:launchWorkflow");
        _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

        addPropertyToOperationClient(_operationClient,
                org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

        // create SOAP envelope with that payload
        org.apache.axiom.soap.SOAPEnvelope env = null;
        final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

        // Style is Doc.
        WorkflowInterpretorStub.LaunchWorkflow dummyWrappedType = null;
        env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), workflowAsString1, topic2,
                password3, username4, inputs5, configurations6, dummyWrappedType,
                optimizeContent(new javax.xml.namespace.QName("http://interpretor.xbaya.airavata.apache.org",
                        "launchWorkflow")));

        // adding SOAP soap_headers
        _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);

        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
            @Override
            public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                try {
                    org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();

                    java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                            WorkflowInterpretorStub.LaunchWorkflowResponse.class, getEnvelopeNamespaces(resultEnv));
                    callback.receiveResultlaunchWorkflow(getLaunchWorkflowResponse_return((WorkflowInterpretorStub.LaunchWorkflowResponse) object));

                } catch (org.apache.axis2.AxisFault e) {
                    callback.receiveErrorlaunchWorkflow(e);
                }
            }

            @Override
            public void onError(java.lang.Exception error) {
                if (error instanceof org.apache.axis2.AxisFault) {
                    org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
                    org.apache.axiom.om.OMElement faultElt = f.getDetail();
                    if (faultElt != null) {
                        if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
                            // make the fault by reflection
                            try {
                                java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap
                                        .get(faultElt.getQName());
                                java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                                java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();
                                // message class
                                java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt
                                        .getQName());
                                java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                                java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
                                java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                        new java.lang.Class[] { messageClass });
                                m.invoke(ex, new java.lang.Object[] { messageObject });

                                callback.receiveErrorlaunchWorkflow(new java.rmi.RemoteException(ex.getMessage(), ex));
                            } catch (java.lang.ClassCastException e) {
                                // we cannot intantiate the class -
                                // throw the original Axis fault
                                callback.receiveErrorlaunchWorkflow(f);
                            } catch (java.lang.ClassNotFoundException e) {
                                // we cannot intantiate the class -
                                // throw the original Axis fault
                                callback.receiveErrorlaunchWorkflow(f);
                            } catch (java.lang.NoSuchMethodException e) {
                                // we cannot intantiate the class -
                                // throw the original Axis fault
                                callback.receiveErrorlaunchWorkflow(f);
                            } catch (java.lang.reflect.InvocationTargetException e) {
                                // we cannot intantiate the class -
                                // throw the original Axis fault
                                callback.receiveErrorlaunchWorkflow(f);
                            } catch (java.lang.IllegalAccessException e) {
                                // we cannot intantiate the class -
                                // throw the original Axis fault
                                callback.receiveErrorlaunchWorkflow(f);
                            } catch (java.lang.InstantiationException e) {
                                // we cannot intantiate the class -
                                // throw the original Axis fault
                                callback.receiveErrorlaunchWorkflow(f);
                            } catch (org.apache.axis2.AxisFault e) {
                                // we cannot intantiate the class -
                                // throw the original Axis fault
                                callback.receiveErrorlaunchWorkflow(f);
                            }
                        } else {
                            callback.receiveErrorlaunchWorkflow(f);
                        }
                    } else {
                        callback.receiveErrorlaunchWorkflow(f);
                    }
                } else {
                    callback.receiveErrorlaunchWorkflow(error);
                }
            }

            @Override
            public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils
                        .getInboundFaultFromMessageContext(faultContext);
                onError(fault);
            }

            @Override
            public void onComplete() {
                try {
                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                } catch (org.apache.axis2.AxisFault axisFault) {
                    callback.receiveErrorlaunchWorkflow(axisFault);
                }
            }
        });

        org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if (_operations[0].getMessageReceiver() == null && _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
            _operations[0].setMessageReceiver(_callbackReceiver);
        }

        // execute the operation client
        _operationClient.execute(false);

    }

    /**
     * A utility method that copies the namepaces from the SOAPEnvelope
     */
    private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env) {
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return returnMap;
    }

    private javax.xml.namespace.QName[] opNameArray = null;

    private boolean optimizeContent(javax.xml.namespace.QName opName) {

        if (opNameArray == null) {
            return false;
        }
        for (int i = 0; i < opNameArray.length; i++) {
            if (opName.equals(opNameArray[i])) {
                return true;
            }
        }
        return false;
    }

    public static class ExtensionMapper {

        public static java.lang.Object getTypeObject(java.lang.String namespaceURI, java.lang.String typeName,
                javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {

            if ("http://interpretor.xbaya.airavata.apache.org".equals(namespaceURI) && "NameValue".equals(typeName)) {

                return NameValue.Factory.parse(reader);

            }

            throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
        }

    }

    public static class LaunchWorkflowResponse implements org.apache.axis2.databinding.ADBBean {

        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://interpretor.xbaya.airavata.apache.org", "launchWorkflowResponse", "ns1");

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if (namespace.equals("http://interpretor.xbaya.airavata.apache.org")) {
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * field for _return
         */

        protected java.lang.String local_return;

        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will
         * be used to determine whether to include this field in the serialized XML
         */
        protected boolean local_returnTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String get_return() {
            return local_return;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            _return
         */
        public void set_return(java.lang.String param) {

            if (param != null) {
                // update the setting tracker
                local_returnTracker = true;
            } else {
                local_returnTracker = true;

            }

            this.local_return = param;

        }

        /**
         * isReaderMTOMAware
         * 
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader
                        .getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        /**
         * 
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException {

            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

                @Override
                public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                    LaunchWorkflowResponse.this.serialize(MY_QNAME, factory, xmlWriter);
                }
            };
            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

        }

        @Override
        public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        @Override
        public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, boolean serializeType)
                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if ((namespace != null) && (namespace.trim().length() > 0)) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (serializeType) {

                java.lang.String namespacePrefix = registerPrefix(xmlWriter,
                        "http://interpretor.xbaya.airavata.apache.org");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix
                            + ":launchWorkflowResponse", xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                            "launchWorkflowResponse", xmlWriter);
                }

            }
            if (local_returnTracker) {
                namespace = "";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "return", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);

                    } else {
                        xmlWriter.writeStartElement(namespace, "return");
                    }

                } else {
                    xmlWriter.writeStartElement("return");
                }

                if (local_return == null) {
                    // write the nil attribute

                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);

                } else {

                    xmlWriter.writeCharacters(local_return);

                }

                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();

        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);

            }

            xmlWriter.writeAttribute(namespace, attName, attValue);

        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace, java.lang.String attName, java.lang.String attValue,
                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException {

            java.lang.String attributeNamespace = qname.getNamespaceURI();
            java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            java.lang.String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }

            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        /**
         * method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":"
                            + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames, javax.xml.stream.XMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not
                // possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite
                                    .append(prefix)
                                    .append(":")
                                    .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil
                                    .convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil
                                .convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace)
                throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }

                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            return prefix;
        }

        /**
         * databinding method to get an XML representation of this object
         * 
         */
        @Override
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                throws org.apache.axis2.databinding.ADBException {

            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (local_returnTracker) {
                elementList.add(new javax.xml.namespace.QName("", "return"));

                elementList.add(local_return == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
                        .convertToString(local_return));
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(),
                    attribList.toArray());

        }

        /**
         * Factory class that keeps the parse method
         */
        public static class Factory {

            /**
             * static method to create the object Precondition: If this object is an element, the current or next start
             * element starts this object and any intervening reader events are ignorable If this object is not an
             * element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element If this object
             * is a complex type, the reader is positioned at the end element of its outer element
             */
            public static LaunchWorkflowResponse parse(javax.xml.stream.XMLStreamReader reader)
                    throws java.lang.Exception {
                LaunchWorkflowResponse object = new LaunchWorkflowResponse();

                java.lang.String nillableValue = null;
                try {

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance", "type");
                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

                            if (!"launchWorkflowResponse".equals(type)) {
                                // find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (LaunchWorkflowResponse) ExtensionMapper.getTypeObject(nsUri, type, reader);
                            }

                        }

                    }

                    new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() && new javax.xml.namespace.QName("", "return").equals(reader.getName())) {

                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "nil");
                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)) {

                            java.lang.String content = reader.getElementText();

                            object.set_return(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                        } else {

                            reader.getElementText(); // throw away text nodes if
                                                     // any.
                        }

                        reader.next();

                    } // End of if for expected property start element

                    else {

                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement())
                        // A start element we are not expecting indicates a
                        // trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement "
                                + reader.getLocalName());

                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }

        }// end of factory class

    }

    public static class LaunchWorkflow implements org.apache.axis2.databinding.ADBBean {

        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://interpretor.xbaya.airavata.apache.org", "launchWorkflow", "ns1");

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if (namespace.equals("http://interpretor.xbaya.airavata.apache.org")) {
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * field for WorkflowAsString
         */

        protected java.lang.String localWorkflowAsString;

        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will
         * be used to determine whether to include this field in the serialized XML
         */
        protected boolean localWorkflowAsStringTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getWorkflowAsString() {
            return localWorkflowAsString;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            WorkflowAsString
         */
        public void setWorkflowAsString(java.lang.String param) {

            if (param != null) {
                // update the setting tracker
                localWorkflowAsStringTracker = true;
            } else {
                localWorkflowAsStringTracker = true;

            }

            this.localWorkflowAsString = param;

        }

        /**
         * field for Topic
         */

        protected java.lang.String localTopic;

        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will
         * be used to determine whether to include this field in the serialized XML
         */
        protected boolean localTopicTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getTopic() {
            return localTopic;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            Topic
         */
        public void setTopic(java.lang.String param) {

            if (param != null) {
                // update the setting tracker
                localTopicTracker = true;
            } else {
                localTopicTracker = true;

            }

            this.localTopic = param;

        }

        /**
         * field for Password
         */

        protected java.lang.String localPassword;

        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will
         * be used to determine whether to include this field in the serialized XML
         */
        protected boolean localPasswordTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getPassword() {
            return localPassword;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            Password
         */
        public void setPassword(java.lang.String param) {

            if (param != null) {
                // update the setting tracker
                localPasswordTracker = true;
            } else {
                localPasswordTracker = true;

            }

            this.localPassword = param;

        }

        /**
         * field for Username
         */

        protected java.lang.String localUsername;

        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will
         * be used to determine whether to include this field in the serialized XML
         */
        protected boolean localUsernameTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return java.lang.String
         */
        public java.lang.String getUsername() {
            return localUsername;
        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            Username
         */
        public void setUsername(java.lang.String param) {

            if (param != null) {
                // update the setting tracker
                localUsernameTracker = true;
            } else {
                localUsernameTracker = true;

            }

            this.localUsername = param;

        }

        /**
         * field for Inputs This was an Array!
         */

        protected NameValue[] localInputs;

        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will
         * be used to determine whether to include this field in the serialized XML
         */
        protected boolean localInputsTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return NameValue[]
         */
        public NameValue[] getInputs() {
            return localInputs;
        }

        /**
         * validate the array for Inputs
         */
        protected void validateInputs(NameValue[] param) {

        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            Inputs
         */
        public void setInputs(NameValue[] param) {

            validateInputs(param);

            if (param != null) {
                // update the setting tracker
                localInputsTracker = true;
            } else {
                localInputsTracker = true;

            }

            this.localInputs = param;
        }

        /**
         * Auto generated add method for the array for convenience
         * 
         * @param param
         *            NameValue
         */
        public void addInputs(NameValue param) {
            if (localInputs == null) {
                localInputs = new NameValue[] {};
            }

            // update the setting tracker
            localInputsTracker = true;

            java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localInputs);
            list.add(param);
            this.localInputs = (NameValue[]) list.toArray(new NameValue[list.size()]);

        }

        /**
         * field for Configurations This was an Array!
         */

        protected NameValue[] localConfigurations;

        /*
         * This tracker boolean wil be used to detect whether the user called the set method for this attribute. It will
         * be used to determine whether to include this field in the serialized XML
         */
        protected boolean localConfigurationsTracker = false;

        /**
         * Auto generated getter method
         * 
         * @return NameValue[]
         */
        public NameValue[] getConfigurations() {
            return localConfigurations;
        }

        /**
         * validate the array for Configurations
         */
        protected void validateConfigurations(NameValue[] param) {

        }

        /**
         * Auto generated setter method
         * 
         * @param param
         *            Configurations
         */
        public void setConfigurations(NameValue[] param) {

            validateConfigurations(param);

            if (param != null) {
                // update the setting tracker
                localConfigurationsTracker = true;
            } else {
                localConfigurationsTracker = true;

            }

            this.localConfigurations = param;
        }

        /**
         * Auto generated add method for the array for convenience
         * 
         * @param param
         *            NameValue
         */
        public void addConfigurations(NameValue param) {
            if (localConfigurations == null) {
                localConfigurations = new NameValue[] {};
            }

            // update the setting tracker
            localConfigurationsTracker = true;

            java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localConfigurations);
            list.add(param);
            this.localConfigurations = (NameValue[]) list.toArray(new NameValue[list.size()]);

        }

        /**
         * isReaderMTOMAware
         * 
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader
                        .getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
                isReaderMTOMAware = false;
            }
            return isReaderMTOMAware;
        }

        /**
         * 
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement(final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException {

            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this, MY_QNAME) {

                @Override
                public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                    LaunchWorkflow.this.serialize(MY_QNAME, factory, xmlWriter);
                }
            };
            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME, factory, dataSource);

        }

        @Override
        public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
            serialize(parentQName, factory, xmlWriter, false);
        }

        @Override
        public void serialize(final javax.xml.namespace.QName parentQName, final org.apache.axiom.om.OMFactory factory,
                org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter, boolean serializeType)
                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {

            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if ((namespace != null) && (namespace.trim().length() > 0)) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (serializeType) {

                java.lang.String namespacePrefix = registerPrefix(xmlWriter,
                        "http://interpretor.xbaya.airavata.apache.org");
                if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix
                            + ":launchWorkflow", xmlWriter);
                } else {
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "launchWorkflow",
                            xmlWriter);
                }

            }
            if (localWorkflowAsStringTracker) {
                namespace = "";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "workflowAsString", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);

                    } else {
                        xmlWriter.writeStartElement(namespace, "workflowAsString");
                    }

                } else {
                    xmlWriter.writeStartElement("workflowAsString");
                }

                if (localWorkflowAsString == null) {
                    // write the nil attribute

                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);

                } else {

                    xmlWriter.writeCharacters(localWorkflowAsString);

                }

                xmlWriter.writeEndElement();
            }
            if (localTopicTracker) {
                namespace = "";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "topic", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);

                    } else {
                        xmlWriter.writeStartElement(namespace, "topic");
                    }

                } else {
                    xmlWriter.writeStartElement("topic");
                }

                if (localTopic == null) {
                    // write the nil attribute

                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);

                } else {

                    xmlWriter.writeCharacters(localTopic);

                }

                xmlWriter.writeEndElement();
            }
            if (localPasswordTracker) {
                namespace = "";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "password", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);

                    } else {
                        xmlWriter.writeStartElement(namespace, "password");
                    }

                } else {
                    xmlWriter.writeStartElement("password");
                }

                if (localPassword == null) {
                    // write the nil attribute

                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);

                } else {

                    xmlWriter.writeCharacters(localPassword);

                }

                xmlWriter.writeEndElement();
            }
            if (localUsernameTracker) {
                namespace = "";
                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "username", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);

                    } else {
                        xmlWriter.writeStartElement(namespace, "username");
                    }

                } else {
                    xmlWriter.writeStartElement("username");
                }

                if (localUsername == null) {
                    // write the nil attribute

                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);

                } else {

                    xmlWriter.writeCharacters(localUsername);

                }

                xmlWriter.writeEndElement();
            }
            if (localInputsTracker) {
                if (localInputs != null) {
                    for (int i = 0; i < localInputs.length; i++) {
                        if (localInputs[i] != null) {
                            localInputs[i].serialize(new javax.xml.namespace.QName("", "inputs"), factory, xmlWriter);
                        } else {

                            // write null attribute
                            java.lang.String namespace2 = "";
                            if (!namespace2.equals("")) {
                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                if (prefix2 == null) {
                                    prefix2 = generatePrefix(namespace2);

                                    xmlWriter.writeStartElement(prefix2, "inputs", namespace2);
                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                    xmlWriter.setPrefix(prefix2, namespace2);

                                } else {
                                    xmlWriter.writeStartElement(namespace2, "inputs");
                                }

                            } else {
                                xmlWriter.writeStartElement("inputs");
                            }

                            // write the nil attribute
                            writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);
                            xmlWriter.writeEndElement();

                        }

                    }
                } else {

                    // write null attribute
                    java.lang.String namespace2 = "";
                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "inputs", namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);

                        } else {
                            xmlWriter.writeStartElement(namespace2, "inputs");
                        }

                    } else {
                        xmlWriter.writeStartElement("inputs");
                    }

                    // write the nil attribute
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);
                    xmlWriter.writeEndElement();

                }
            }
            if (localConfigurationsTracker) {
                if (localConfigurations != null) {
                    for (int i = 0; i < localConfigurations.length; i++) {
                        if (localConfigurations[i] != null) {
                            localConfigurations[i].serialize(new javax.xml.namespace.QName("", "configurations"),
                                    factory, xmlWriter);
                        } else {

                            // write null attribute
                            java.lang.String namespace2 = "";
                            if (!namespace2.equals("")) {
                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                if (prefix2 == null) {
                                    prefix2 = generatePrefix(namespace2);

                                    xmlWriter.writeStartElement(prefix2, "configurations", namespace2);
                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                    xmlWriter.setPrefix(prefix2, namespace2);

                                } else {
                                    xmlWriter.writeStartElement(namespace2, "configurations");
                                }

                            } else {
                                xmlWriter.writeStartElement("configurations");
                            }

                            // write the nil attribute
                            writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);
                            xmlWriter.writeEndElement();

                        }

                    }
                } else {

                    // write null attribute
                    java.lang.String namespace2 = "";
                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "configurations", namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);

                        } else {
                            xmlWriter.writeStartElement(namespace2, "configurations");
                        }

                    } else {
                        xmlWriter.writeStartElement("configurations");
                    }

                    // write the nil attribute
                    writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "nil", "1", xmlWriter);
                    xmlWriter.writeEndElement();

                }
            }
            xmlWriter.writeEndElement();

        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);

            }

            xmlWriter.writeAttribute(namespace, attName, attValue);

        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace, java.lang.String attName, java.lang.String attValue,
                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException {

            java.lang.String attributeNamespace = qname.getNamespaceURI();
            java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
            if (attributePrefix == null) {
                attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
            }
            java.lang.String attributeValue;
            if (attributePrefix.trim().length() > 0) {
                attributeValue = attributePrefix + ":" + qname.getLocalPart();
            } else {
                attributeValue = qname.getLocalPart();
            }

            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attributeValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attributeValue);
            }
        }

        /**
         * method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":"
                            + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames, javax.xml.stream.XMLStreamWriter xmlWriter)
                throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not
                // possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite
                                    .append(prefix)
                                    .append(":")
                                    .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil
                                    .convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil
                                .convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace)
                throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);

            if (prefix == null) {
                prefix = generatePrefix(namespace);

                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }

                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            return prefix;
        }

        /**
         * databinding method to get an XML representation of this object
         * 
         */
        @Override
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                throws org.apache.axis2.databinding.ADBException {

            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (localWorkflowAsStringTracker) {
                elementList.add(new javax.xml.namespace.QName("", "workflowAsString"));

                elementList.add(localWorkflowAsString == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
                        .convertToString(localWorkflowAsString));
            }
            if (localTopicTracker) {
                elementList.add(new javax.xml.namespace.QName("", "topic"));

                elementList.add(localTopic == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
                        .convertToString(localTopic));
            }
            if (localPasswordTracker) {
                elementList.add(new javax.xml.namespace.QName("", "password"));

                elementList.add(localPassword == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
                        .convertToString(localPassword));
            }
            if (localUsernameTracker) {
                elementList.add(new javax.xml.namespace.QName("", "username"));

                elementList.add(localUsername == null ? null : org.apache.axis2.databinding.utils.ConverterUtil
                        .convertToString(localUsername));
            }
            if (localInputsTracker) {
                if (localInputs != null) {
                    for (int i = 0; i < localInputs.length; i++) {

                        if (localInputs[i] != null) {
                            elementList.add(new javax.xml.namespace.QName("", "inputs"));
                            elementList.add(localInputs[i]);
                        } else {

                            elementList.add(new javax.xml.namespace.QName("", "inputs"));
                            elementList.add(null);

                        }

                    }
                } else {

                    elementList.add(new javax.xml.namespace.QName("", "inputs"));
                    elementList.add(localInputs);

                }

            }
            if (localConfigurationsTracker) {
                if (localConfigurations != null) {
                    for (int i = 0; i < localConfigurations.length; i++) {

                        if (localConfigurations[i] != null) {
                            elementList.add(new javax.xml.namespace.QName("", "configurations"));
                            elementList.add(localConfigurations[i]);
                        } else {

                            elementList.add(new javax.xml.namespace.QName("", "configurations"));
                            elementList.add(null);

                        }

                    }
                } else {

                    elementList.add(new javax.xml.namespace.QName("", "configurations"));
                    elementList.add(localConfigurations);

                }

            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(),
                    attribList.toArray());

        }

        /**
         * Factory class that keeps the parse method
         */
        public static class Factory {

            /**
             * static method to create the object Precondition: If this object is an element, the current or next start
             * element starts this object and any intervening reader events are ignorable If this object is not an
             * element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element If this object
             * is a complex type, the reader is positioned at the end element of its outer element
             */
            public static LaunchWorkflow parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
                LaunchWorkflow object = new LaunchWorkflow();

                java.lang.String nillableValue = null;
                try {

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance", "type");
                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;
                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                            }
                            nsPrefix = nsPrefix == null ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

                            if (!"launchWorkflow".equals(type)) {
                                // find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (LaunchWorkflow) ExtensionMapper.getTypeObject(nsUri, type, reader);
                            }

                        }

                    }

                    new java.util.Vector();

                    reader.next();

                    java.util.ArrayList list5 = new java.util.ArrayList();

                    java.util.ArrayList list6 = new java.util.ArrayList();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()
                            && new javax.xml.namespace.QName("", "workflowAsString").equals(reader.getName())) {

                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "nil");
                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)) {

                            java.lang.String content = reader.getElementText();

                            object.setWorkflowAsString(org.apache.axis2.databinding.utils.ConverterUtil
                                    .convertToString(content));

                        } else {

                            reader.getElementText(); // throw away text nodes if
                                                     // any.
                        }

                        reader.next();

                    } // End of if for expected property start element

                    else {

                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() && new javax.xml.namespace.QName("", "topic").equals(reader.getName())) {

                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "nil");
                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)) {

                            java.lang.String content = reader.getElementText();

                            object.setTopic(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                        } else {

                            reader.getElementText(); // throw away text nodes if
                                                     // any.
                        }

                        reader.next();

                    } // End of if for expected property start element

                    else {

                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()
                            && new javax.xml.namespace.QName("", "password").equals(reader.getName())) {

                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "nil");
                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)) {

                            java.lang.String content = reader.getElementText();

                            object.setPassword(org.apache.axis2.databinding.utils.ConverterUtil
                                    .convertToString(content));

                        } else {

                            reader.getElementText(); // throw away text nodes if
                                                     // any.
                        }

                        reader.next();

                    } // End of if for expected property start element

                    else {

                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()
                            && new javax.xml.namespace.QName("", "username").equals(reader.getName())) {

                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "nil");
                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)) {

                            java.lang.String content = reader.getElementText();

                            object.setUsername(org.apache.axis2.databinding.utils.ConverterUtil
                                    .convertToString(content));

                        } else {

                            reader.getElementText(); // throw away text nodes if
                                                     // any.
                        }

                        reader.next();

                    } // End of if for expected property start element

                    else {

                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() && new javax.xml.namespace.QName("", "inputs").equals(reader.getName())) {

                        // Process the array and step past its final element's
                        // end.

                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "nil");
                        if ("true".equals(nillableValue) || "1".equals(nillableValue)) {
                            list5.add(null);
                            reader.next();
                        } else {
                            list5.add(NameValue.Factory.parse(reader));
                        }
                        // loop until we find a start element that is not part
                        // of this array
                        boolean loopDone5 = false;
                        while (!loopDone5) {
                            // We should be at the end element, but make sure
                            while (!reader.isEndElement())
                                reader.next();
                            // Step out of this element
                            reader.next();
                            // Step to next element event.
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isEndElement()) {
                                // two continuous end elements means we are
                                // exiting the xml structure
                                loopDone5 = true;
                            } else {
                                if (new javax.xml.namespace.QName("", "inputs").equals(reader.getName())) {

                                    nillableValue = reader.getAttributeValue(
                                            "http://www.w3.org/2001/XMLSchema-instance", "nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)) {
                                        list5.add(null);
                                        reader.next();
                                    } else {
                                        list5.add(NameValue.Factory.parse(reader));
                                    }
                                } else {
                                    loopDone5 = true;
                                }
                            }
                        }
                        // call the converter utility to convert and set the
                        // array

                        object.setInputs((NameValue[]) org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                NameValue.class, list5));

                    } // End of if for expected property start element

                    else {

                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()
                            && new javax.xml.namespace.QName("", "configurations").equals(reader.getName())) {

                        // Process the array and step past its final element's
                        // end.

                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "nil");
                        if ("true".equals(nillableValue) || "1".equals(nillableValue)) {
                            list6.add(null);
                            reader.next();
                        } else {
                            list6.add(NameValue.Factory.parse(reader));
                        }
                        // loop until we find a start element that is not part
                        // of this array
                        boolean loopDone6 = false;
                        while (!loopDone6) {
                            // We should be at the end element, but make sure
                            while (!reader.isEndElement())
                                reader.next();
                            // Step out of this element
                            reader.next();
                            // Step to next element event.
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isEndElement()) {
                                // two continuous end elements means we are
                                // exiting the xml structure
                                loopDone6 = true;
                            } else {
                                if (new javax.xml.namespace.QName("", "configurations").equals(reader.getName())) {

                                    nillableValue = reader.getAttributeValue(
                                            "http://www.w3.org/2001/XMLSchema-instance", "nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)) {
                                        list6.add(null);
                                        reader.next();
                                    } else {
                                        list6.add(NameValue.Factory.parse(reader));
                                    }
                                } else {
                                    loopDone6 = true;
                                }
                            }
                        }
                        // call the converter utility to convert and set the
                        // array

                        object.setConfigurations((NameValue[]) org.apache.axis2.databinding.utils.ConverterUtil
                                .convertToArray(NameValue.class, list6));

                    } // End of if for expected property start element

                    else {

                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement())
                        // A start element we are not expecting indicates a
                        // trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement "
                                + reader.getLocalName());

                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }

        }// end of factory class

    }

    private org.apache.axiom.om.OMElement toOM(WorkflowInterpretorStub.LaunchWorkflow param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

        try {
            return param.getOMElement(WorkflowInterpretorStub.LaunchWorkflow.MY_QNAME,
                    org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }

    }

    private org.apache.axiom.om.OMElement toOM(WorkflowInterpretorStub.LaunchWorkflowResponse param,
            boolean optimizeContent) throws org.apache.axis2.AxisFault {

        try {
            return param.getOMElement(WorkflowInterpretorStub.LaunchWorkflowResponse.MY_QNAME,
                    org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }

    }

    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory,
            java.lang.String param1, java.lang.String param2, java.lang.String param3, java.lang.String param4,
            NameValue[] param5, NameValue[] param6, WorkflowInterpretorStub.LaunchWorkflow dummyWrappedType,
            boolean optimizeContent) throws org.apache.axis2.AxisFault {

        try {
            WorkflowInterpretorStub.LaunchWorkflow wrappedType = new WorkflowInterpretorStub.LaunchWorkflow();

            wrappedType.setWorkflowAsString(param1);

            wrappedType.setTopic(param2);

            wrappedType.setPassword(param3);

            wrappedType.setUsername(param4);

            wrappedType.setInputs(param5);

            wrappedType.setConfigurations(param6);

            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

            emptyEnvelope.getBody().addChild(
                    wrappedType.getOMElement(WorkflowInterpretorStub.LaunchWorkflow.MY_QNAME, factory));

            return emptyEnvelope;
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    /* methods to provide back word compatibility */

    private java.lang.String getLaunchWorkflowResponse_return(WorkflowInterpretorStub.LaunchWorkflowResponse wrappedType) {

        return wrappedType.get_return();

    }

    /**
     * get the default envelope
     */
    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory) {
        return factory.getDefaultEnvelope();
    }

    private java.lang.Object fromOM(org.apache.axiom.om.OMElement param, java.lang.Class type,
            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault {

        try {

            if (WorkflowInterpretorStub.LaunchWorkflow.class.equals(type)) {

                return WorkflowInterpretorStub.LaunchWorkflow.Factory.parse(param.getXMLStreamReaderWithoutCaching());

            }

            if (WorkflowInterpretorStub.LaunchWorkflowResponse.class.equals(type)) {

                return WorkflowInterpretorStub.LaunchWorkflowResponse.Factory.parse(param
                        .getXMLStreamReaderWithoutCaching());

            }

        } catch (java.lang.Exception e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
        return null;
    }

}
