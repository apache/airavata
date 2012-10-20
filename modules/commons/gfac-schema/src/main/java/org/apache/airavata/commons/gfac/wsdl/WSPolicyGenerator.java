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

import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WSPolicyGenerator implements WSDLConstants {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public static UnknownExtensibilityElement createServiceLevelPolicy(DOMImplementation dImpl, String policyID) {
        Document doc = dImpl.createDocument(WSP_NAMESPACE, "wsp:Policy", null);

        Element policy = doc.getDocumentElement();
        policy.setAttribute("wsu:Id", policyID);
        Element exactlyOne = doc.createElement("wsp:ExactlyOne");
        Element all = doc.createElement("wsp:All");

        /*
         * Element policyEncoding = doc.createElement("wspe:Utf816FFFECharacterEncoding");
         * all.appendChild(policyEncoding);
         */

        Element asymmBinding = doc.createElement("sp:AsymmetricBinding");
        asymmBinding.setAttribute("xmlns:sp", SP_NAMESPACE);
        Element policy1 = doc.createElement("wsp:Policy");

        Element initiatorToken = doc.createElement("sp:InitiatorToken");
        Element policy2 = doc.createElement("wsp:Policy");
        Element x509Token = doc.createElement("sp:X509Token");
        x509Token.setAttribute("sp:IncludeToken",
                "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient");

        Element policy3 = doc.createElement("wsp:Policy");
        Element x509V3Token10 = doc.createElement("sp:WssX509V3Token10");
        policy3.appendChild(x509V3Token10);
        x509Token.appendChild(policy3);
        policy2.appendChild(x509Token);
        initiatorToken.appendChild(policy2);
        policy1.appendChild(initiatorToken);

        // <sp:RecipientToken>
        // <wsp:Policy>
        // <sp:X509Token
        // sp:IncludeToken="http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Never">
        // <wsp:Policy>
        // <sp:WssX509V3Token10/>
        // </wsp:Policy>
        // </sp:X509Token>
        // </wsp:Policy>
        // </sp:RecipientToken>

        Element recipientToken = doc.createElement("sp:RecipientToken");
        policy2 = doc.createElement("wsp:Policy");
        x509Token = doc.createElement("sp:X509Token");
        // x509Token.setAttribute(
        // "sp:IncludeToken","http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Never");
        x509Token.setAttribute("sp:IncludeToken",
                "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient");
        x509Token.appendChild(createEmptyElementHierachy("wsp:Policy", new String[] { "sp:WssX509V3Token10" }, doc));
        policy2.appendChild(x509Token);
        recipientToken.appendChild(policy2);

        policy1.appendChild(recipientToken);

        Element algorithmSuite = doc.createElement("sp:AlgorithmSuite");
        algorithmSuite.appendChild(createEmptyElementHierachy("wsp:Policy", new String[] { "sp:Basic256",
                "sp:InclusiveC14N" }, doc));
        policy1.appendChild(algorithmSuite);

        Element layout = doc.createElement("sp:Layout");
        layout.appendChild(createEmptyElementHierachy("wsp:Policy", new String[] { "sp:Strict" }, doc));
        policy1.appendChild(layout);
        // Element ts = doc.createElement("sp:IncludeTimestamp");
        // policy1.appendChild(ts);
        asymmBinding.appendChild(policy1);

        all.appendChild(asymmBinding);

        /*
         * <sp:Wss10 xmlns:sp="http://schemas.xmlsoap.org/ws/2005/07/securitypolicy"> <wsp:Policy>
         * <sp:MustSupportRefKeyIdentifier/> <sp:MustSupportRefIssuerSerial/> </wsp:Policy> </sp:Wss10>
         */

        Element wss10 = doc.createElement("sp:Wss10");
        wss10.appendChild(createEmptyElementHierachy("wsp:Policy", new String[] { "sp:MustSupportRefKeyIdentifier",
                "sp:MustSupportRefIssuerSerial" }, doc));
        all.appendChild(wss10);

        exactlyOne.appendChild(all);
        policy.appendChild(exactlyOne);

        UnknownExtensibilityElement elem = new UnknownExtensibilityElement();
        elem.setElement(policy);
        elem.setElementType(new QName(WSP_NAMESPACE, "wsp:Policy"));
        return elem;
    }

    private static Element createEmptyElementHierachy(String parent, String[] childern, Document doc) {
        Element parentEle = doc.createElement(parent);
        for (int x = 0; x < childern.length; x++) {
            parentEle.appendChild(doc.createElement(childern[x]));
        }
        return parentEle;
    }

    private static Element createElementHierachy(String parent, String[] childern, String[] values, Document doc) {
        Element parentEle = doc.createElement(parent);
        for (int x = 0; x < childern.length; x++) {
            Element temp = doc.createElement(childern[x]);
            temp.appendChild(doc.createTextNode(values[x]));
            parentEle.appendChild(temp);
        }
        return parentEle;
    }

    private static UnknownExtensibilityElement createWSPolicyRef(DOMImplementation dImpl, String id) {
        Document doc = dImpl.createDocument(WSP_NAMESPACE, "wsp:PolicyReference", null);
        Element policyRef = doc.getDocumentElement();
        policyRef.setAttribute("URI", "#" + id);
        UnknownExtensibilityElement elem = new UnknownExtensibilityElement();
        elem.setElement(policyRef);
        elem.setElementType(new QName(WSP_NAMESPACE, "PolicyReference"));
        return elem;
    }

}