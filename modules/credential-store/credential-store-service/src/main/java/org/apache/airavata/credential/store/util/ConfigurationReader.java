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
package org.apache.airavata.credential.store.util;

import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 8/25/13
 * Time: 6:40 AM
 */

/**
 * Reads credential store specific configurations from the client.xml file.
 */
public class ConfigurationReader {

    private String successUrl;

    private String errorUrl;

    private String portalRedirectUrl;

    public String getPortalRedirectUrl() {
        return portalRedirectUrl;
    }

    public void setPortalRedirectUrl(String portalRedirectUrl) {
        this.portalRedirectUrl = portalRedirectUrl;
    }

    public ConfigurationReader() throws CredentialStoreException {

        try {
            loadConfigurations();
        } catch (Exception e) {
            throw new CredentialStoreException("Unable to read credential store specific configurations." , e);
        }


    }

    private void loadConfigurations() throws ParserConfigurationException,
            IOException, SAXException {
        InputStream inputStream
                = this.getClass().getClassLoader().getResourceAsStream("credential-store/client.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("credential-store");

        readElementValue(nodeList);

    }

    private void readElementValue(NodeList nodeList) {
        for (int temp = 0; temp < nodeList.getLength(); temp++) {

            Node nNode = nodeList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;

                this.successUrl = eElement.getElementsByTagName("successUri").item(0).getTextContent();
                this.errorUrl =  eElement.getElementsByTagName("errorUri").item(0).getTextContent();
                this.portalRedirectUrl = eElement.getElementsByTagName("redirectUri").item(0).getTextContent();
            }
        }
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }
}
