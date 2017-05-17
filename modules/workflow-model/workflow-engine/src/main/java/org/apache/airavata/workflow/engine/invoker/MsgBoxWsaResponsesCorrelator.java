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
//package org.apache.airavata.workflow.engine.invoker;
//
//import org.apache.airavata.workflow.core.XMLUtil;
//import org.apache.airavata.wsmg.msgbox.client.MsgBoxClient;
//import org.apache.axiom.om.OMElement;
//import org.apache.axis2.addressing.EndpointReference;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.xmlpull.v1.builder.XmlDocument;
//import org.xmlpull.v1.builder.XmlElement;
//import org.xmlpull.v1.builder.XmlInfosetBuilder;
//import xsul.MLogger;
//import xsul.XmlConstants;
//import xsul.XsulException;
//import xsul.processor.DynamicInfosetProcessorException;
//import xsul.ws_addressing.WsaEndpointReference;
//import xsul.ws_addressing.WsaMessageInformationHeaders;
//import xsul.wsif.WSIFMessage;
//import xsul.wsif.impl.WSIFMessageElement;
//import xsul.xwsif_runtime_async.WSIFAsyncResponseListener;
//import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
//import xsul.xwsif_runtime_async.WSIFAsyncWsaResponsesCorrelatorBase;
//
//import javax.xml.stream.XMLStreamException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.rmi.RemoteException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//public class MsgBoxWsaResponsesCorrelator extends WSIFAsyncWsaResponsesCorrelatorBase
//    implements WSIFAsyncResponsesCorrelator, Runnable
//{
//    private static final Logger logger = LoggerFactory.getLogger(MsgBoxWsaResponsesCorrelator.class);
//    private final static XmlInfosetBuilder builder = XmlConstants.BUILDER;
//
//    private String msgBoxServiceLoc;
//    private MsgBoxClient msgBoxClient;
//    EndpointReference msgBoxAddr;
//    private Thread messageBoxDonwloader;
//
//    private AsynchronousInvoker invoker;
//
//    public MsgBoxWsaResponsesCorrelator(String msgBoxServiceLoc,AsynchronousInvoker output)
//        throws DynamicInfosetProcessorException
//    {
//        this.invoker = output;
//        this.msgBoxServiceLoc = msgBoxServiceLoc;
//        msgBoxClient = new MsgBoxClient();
//        try {
//            msgBoxAddr = msgBoxClient.createMessageBox(msgBoxServiceLoc,5000L);
//            try {
//                setReplyTo(new WsaEndpointReference(new URI(msgBoxAddr.getAddress())));
//            } catch (URISyntaxException e) {
//            	logger.error(e.getLocalizedMessage(),e);  //To change body of catch statement use File | Settings | File Templates.
//            }
//            messageBoxDonwloader = new Thread(this, Thread.currentThread().getName()+"-async-msgbox-correlator");
//            messageBoxDonwloader.setDaemon(true);
//            messageBoxDonwloader.start();
//        } catch (RemoteException e) {
//        	logger.error(e.getLocalizedMessage(),e);  //To change body of catch statement use File | Settings | File Templates.
//        }
//    }
//
////    public void setMsgBoxAddr(WsaEndpointReference msgBoxAddr) {
////      this.msgBoxAddr = msgBoxAddr;
////    }
//
//
//
//    public void run() {
//        while(true) {
//            try {
//                Iterator<OMElement> omElementIterator = msgBoxClient.takeMessagesFromMsgBox(msgBoxAddr, 5000L);
//                List<XmlElement> xmlArrayList = new ArrayList<XmlElement>();
//                while (omElementIterator.hasNext()){
//                    OMElement next = omElementIterator.next();
//                    String message = next.toStringWithConsume();
//                    xmlArrayList.add(XMLUtil.stringToXmlElement3(message));
//                }
//                // now hard work: find callbacks
//                for (int i = 0; i < xmlArrayList.size(); i++) {
//                    XmlElement m = xmlArrayList.get(i);
//                    try {
//                        logger.debug(Thread.currentThread().getName());
//                        WSIFMessageElement e = new WSIFMessageElement(m);
//                        this.invoker.setOutputMessage(e);
//                        //ideally there are no multiple messages, so we can return from this thread at this point
//                        //otherwise this thread will keep running forever for each worfklow node, so there can be large
//                        // number of waiting threads in an airavata deployment
//                        return;
//                    } catch (Throwable e) {
//                    	logger.error(e.getLocalizedMessage(),e);  //To change body of catch statement use File | Settings | File Templates.
//                    }
//                }
//                try {
//                    Thread.currentThread().sleep(1000L); //do not overload msg box service ...
//                } catch (InterruptedException e) {
//                    break;
//                }
//            } catch (XsulException e) {
//                logger.error("could not retrieve messages");
//                break;
//            } catch (RemoteException e) {
//                logger.error("could not retrieve messages");
//                break;
//            } catch (XMLStreamException e) {
//                logger.error("could not retrieve messages");
//                break;
//            } catch (Exception e){
//                logger.error("could not retrieve messages");
//                break;
//            }
//        }
//    }
//
//
//
//}
