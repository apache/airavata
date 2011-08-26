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

package org.apache.airavata.services.gfac.axis2.handlers;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.airavata.core.gfac.context.impl.AmazonSecurityContext;
import org.apache.airavata.services.gfac.axis2.GFacService;
import org.apache.airavata.services.gfac.axis2.utils.MessageContextUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;

public class AmazonSecurityHandler extends AbstractHandler {

    private static final String SECURITY_CONTEXT = "security-context";
    private static final String AMAZON = "amazon-webservices";
    private static final String ACCESS_KEY_ID = "access-key-id";
    private static final String SECRET_ACCESS_KEY = "secret-access-key";

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        SOAPHeader header = envelope.getHeader();
        if (header != null) {
            Iterator it = header.examineAllHeaderBlocks();
            while (it.hasNext()) {
                SOAPHeaderBlock x = (SOAPHeaderBlock) it.next();
                String elementName = x.getLocalName();

                if (elementName.equals(SECURITY_CONTEXT)) {

                    OMElement amazon = x.getFirstChildWithName(new QName(null, AMAZON));

                    if (amazon != null) {

                        AmazonSecurityContext amazonSecurityContext = new AmazonSecurityContext();

                        OMElement accessKey = amazon.getFirstChildWithName(new QName(null, ACCESS_KEY_ID));
                        OMElement secretKey = amazon.getFirstChildWithName(new QName(null, SECRET_ACCESS_KEY));

                        amazonSecurityContext.setAccessKey(accessKey.getText());
                        amazonSecurityContext.setSecretKey(secretKey.getText());

                        // set to context
                        MessageContextUtil.addContextToProperty(msgContext, GFacService.SECURITY_CONTEXT, "amazon",
                                amazonSecurityContext);
                    }
                }
            }
        }
        return InvocationResponse.CONTINUE;
    }

}
