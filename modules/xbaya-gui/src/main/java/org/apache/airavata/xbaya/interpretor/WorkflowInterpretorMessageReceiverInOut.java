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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.xbaya.interpretor;
//
//import org.apache.airavata.client.stub.interpretor.NameValue;
//import org.apache.airavata.client.stub.interpretor.WorkflowInterpretorStub;
//
//
//public class WorkflowInterpretorMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver{
//    public static final String MYPROXY_USER = "myproxy.user";
//    public static final String MYPROXY_PASS = "myproxy.password";
//     public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
//        throws org.apache.axis2.AxisFault{
//
//        try {
//
//        // get the implementation class for the Web Service
//        Object obj = getTheImplementationObject(msgContext);
//
//        WorkflowInterpretorSkeleton skel = (WorkflowInterpretorSkeleton)obj;
//        //Out Envelop
//        org.apache.axiom.soap.SOAPEnvelope envelope = null;
//        //Find the axisOperation that has been set by the Dispatch phase.
//        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
//        if (op == null) {
//        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
//        }
//
//        java.lang.String methodName;
//        if((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJava(op.getName().getLocalPart())) != null)){
//
//
//
//            if("launchWorkflow".equals(methodName)){
//
//	                        WorkflowInterpretorStub.LaunchWorkflow wrappedParam =
//                                                             (WorkflowInterpretorStub.LaunchWorkflow)fromOM(
//                                    msgContext.getEnvelope().getBody().getFirstElement(),
//                                    WorkflowInterpretorStub.LaunchWorkflow.class,
//                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
//
//
//                WorkflowInterpretorStub.LaunchWorkflowResponse launchWorkflowResponse = wrapLaunchWorkflowResponse_return(
//
//
//                        skel.launchWorkflow(
//
//                                getWorkflowAsString(wrappedParam)
//                                ,
//                                getTopic(wrappedParam)
//                                ,
//                                getInputs(wrappedParam)
//                        )
//
//                );
//
//                envelope = toEnvelope(getSOAPFactory(msgContext), launchWorkflowResponse, false);
//
//            } else {
//              throw new java.lang.RuntimeException("method not found");
//            }
//
//
//        newMsgContext.setEnvelope(envelope);
//        }
//        }
//        catch (java.lang.Exception e) {
//        throw org.apache.axis2.AxisFault.makeFault(e);
//        }
//        }
//
//        //
//            private  org.apache.axiom.om.OMElement  toOM(WorkflowInterpretorStub.LaunchWorkflow param, boolean optimizeContent)
//            throws org.apache.axis2.AxisFault {
//
//
//                        try{
//                             return param.getOMElement(WorkflowInterpretorStub.LaunchWorkflow.MY_QNAME,
//                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
//                        } catch(org.apache.axis2.databinding.ADBException e){
//                            throw org.apache.axis2.AxisFault.makeFault(e);
//                        }
//
//
//            }
//
//            private  org.apache.axiom.om.OMElement  toOM(WorkflowInterpretorStub.LaunchWorkflowResponse param, boolean optimizeContent)
//            throws org.apache.axis2.AxisFault {
//
//
//                        try{
//                             return param.getOMElement(WorkflowInterpretorStub.LaunchWorkflowResponse.MY_QNAME,
//                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
//                        } catch(org.apache.axis2.databinding.ADBException e){
//                            throw org.apache.axis2.AxisFault.makeFault(e);
//                        }
//
//
//            }
//
//                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, WorkflowInterpretorStub.LaunchWorkflowResponse param, boolean optimizeContent)
//                        throws org.apache.axis2.AxisFault{
//                      try{
//                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
//
//                                    emptyEnvelope.getBody().addChild(param.getOMElement(WorkflowInterpretorStub.LaunchWorkflowResponse.MY_QNAME,factory));
//
//
//                         return emptyEnvelope;
//                    } catch(org.apache.axis2.databinding.ADBException e){
//                        throw org.apache.axis2.AxisFault.makeFault(e);
//                    }
//                    }
//
//
//                        private java.lang.String getWorkflowAsString(
//                        WorkflowInterpretorStub.LaunchWorkflow wrappedType){
//
//                                return wrappedType.getWorkflowAsString();
//
//                        }
//
//
//                        private java.lang.String getTopic(
//                       WorkflowInterpretorStub.LaunchWorkflow wrappedType){
//
//                                return wrappedType.getTopic();
//
//                        }
//
//
//                        private java.lang.String getPassword(
//                        WorkflowInterpretorStub.LaunchWorkflow wrappedType){
//
//                                return wrappedType.getPassword();
//
//                        }
//
//
//                        private java.lang.String getUsername(
//                        WorkflowInterpretorStub.LaunchWorkflow wrappedType){
//
//                                return wrappedType.getUsername();
//
//                        }
//
//
//                        private NameValue[] getInputs(
//                        WorkflowInterpretorStub.LaunchWorkflow wrappedType){
//
//                                return wrappedType.getInputs();
//
//                        }
//
//
//                        private NameValue[] getConfigurations(
//                        WorkflowInterpretorStub.LaunchWorkflow wrappedType){
//
//                                return wrappedType.getConfigurations();
//
//                        }
//
//
//
//                        private WorkflowInterpretorStub.LaunchWorkflowResponse wrapLaunchWorkflowResponse_return(
//                        java.lang.String param){
//                            WorkflowInterpretorStub.LaunchWorkflowResponse wrappedElement = new WorkflowInterpretorStub.LaunchWorkflowResponse();
//
//                            wrappedElement.set_return(param);
//
//                            return wrappedElement;
//                        }
//
//                         private WorkflowInterpretorStub.LaunchWorkflowResponse wraplaunchWorkflow(){
//                                WorkflowInterpretorStub.LaunchWorkflowResponse wrappedElement = new WorkflowInterpretorStub.LaunchWorkflowResponse();
//                                return wrappedElement;
//                         }
//
//
//
//        /**
//        *  get the default envelope
//        */
//        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
//        return factory.getDefaultEnvelope();
//        }
//
//
//        private  java.lang.Object fromOM(
//        org.apache.axiom.om.OMElement param,
//        java.lang.Class type,
//        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
//
//        try {
//
//                if (WorkflowInterpretorStub.LaunchWorkflow.class.equals(type)){
//
//                           return WorkflowInterpretorStub.LaunchWorkflow.Factory.parse(param.getXMLStreamReaderWithoutCaching());
//
//
//                }
//
//                if (WorkflowInterpretorStub.LaunchWorkflowResponse.class.equals(type)){
//
//                           return WorkflowInterpretorStub.LaunchWorkflowResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
//
//
//                }
//
//        } catch (java.lang.Exception e) {
//        throw org.apache.axis2.AxisFault.makeFault(e);
//        }
//           return null;
//        }
//
//
//
//
//
//        /**
//        *  A utility method that copies the namepaces from the SOAPEnvelope
//        */
//        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
//        java.util.Map returnMap = new java.util.HashMap();
//        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
//        while (namespaceIterator.hasNext()) {
//        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
//        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
//        }
//        return returnMap;
//        }
//
//        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
//        org.apache.axis2.AxisFault f;
//        Throwable cause = e.getCause();
//        if (cause != null) {
//            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
//        } else {
//            f = new org.apache.axis2.AxisFault(e.getMessage());
//        }
//
//        return f;
//    }
//}
