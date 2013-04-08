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

package org.apache.airavata.gfac;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.exception.UnspecifiedApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.utils.GridConfigurationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GFacConfiguration {
    public static final Logger log = LoggerFactory.getLogger(GFacConfiguration.class);


    private AiravataAPI airavataAPI;

    private static Document handlerDoc;
    // Keep list of full qualified class names of GFac handlers which should invoked before
    // the provider
    private List<String> inHandlers = new ArrayList<String>();

    // Keep list of full qualified class names of GFac handlers which should invoked after
    // the provider
    private List<String> outHandlers = new ArrayList<String>();

    private static List<GridConfigurationHandler> gridConfigurationHandlers;

    private static String GRID_HANDLERS = "airavata.grid.handlers";

    static {
        gridConfigurationHandlers = new ArrayList<GridConfigurationHandler>();
        try {
            String handlerString = ServerSettings.getSetting(GRID_HANDLERS);
            String[] handlers = handlerString.split(",");
            for (String handlerClass : handlers) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<GridConfigurationHandler> classInstance = (Class<GridConfigurationHandler>) GFacConfiguration.class
                            .getClassLoader().loadClass(handlerClass);
                    gridConfigurationHandlers.add(classInstance.newInstance());
                } catch (Exception e) {
                    log.error("Error while loading Grid Configuration Handler class " + handlerClass, e);
                }
            }
        } catch (UnspecifiedApplicationSettingsException e) {
            //no handlers defined
        } catch (ApplicationSettingsException e1) {
            log.error("Error in reading Configuration handler data!!!", e1);
        }
    }

    public static GridConfigurationHandler[] getGridConfigurationHandlers() {
        return gridConfigurationHandlers.toArray(new GridConfigurationHandler[]{});
    }

    public GFacConfiguration(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }


    public List<String> getInHandlers() {
        //This will avoid the misconfiguration done by user in gfac-config.xml
        return removeDuplicateWithOrder(inHandlers);
    }

    public List<String> getOutHandlers() {
        //This will avoid the misconfiguration done by user in gfac-config.xml
        return removeDuplicateWithOrder(outHandlers);
    }
    public void setInHandlers(List<String> inHandlers) {
        this.inHandlers = inHandlers;
    }

    public void setOutHandlers(List<String> outHandlers) {
        this.outHandlers = outHandlers;
    }

    public void setInHandlers(String providerName, String applicationName) {
        try {
            this.inHandlers = xpathGetAttributeValueList(handlerDoc, Constants.XPATH_EXPR_GLOBAL_INFLOW_HANDLERS, Constants.GFAC_CONFIG_HANDLER_CLASS_ATTRIBUTE);
            if (applicationName != null) {
                String xPath = Constants.XPATH_EXPR_APPLICATION_HANDLERS_START + applicationName + Constants.XPATH_EXPR_APPLICATION_INFLOW_HANDLERS_END;
                List<String> strings = xpathGetAttributeValueList(handlerDoc, xPath, Constants.GFAC_CONFIG_HANDLER_CLASS_ATTRIBUTE);
                this.inHandlers.addAll(strings);
            }
            if (providerName != null) {
                String xPath = Constants.XPATH_EXPR_PROVIDER_HANDLERS_START + providerName + Constants.XPATH_EXPR_PROVIDER_INFLOW_HANDLERS_END;
                List<String> strings = xpathGetAttributeValueList(handlerDoc, xPath, Constants.GFAC_CONFIG_APPLICATION_NAME_ATTRIBUTE);
                this.inHandlers.addAll(strings);
            }
        } catch (XPathExpressionException e) {
            new GFacException("Error parsing Handler Configuration", e);
        }
    }

    public void setOutHandlers(String providerName, String applicationName) {
        try {
            this.outHandlers = xpathGetAttributeValueList(handlerDoc, Constants.XPATH_EXPR_GLOBAL_OUTFLOW_HANDLERS, Constants.GFAC_CONFIG_HANDLER_CLASS_ATTRIBUTE);
            if (applicationName != null) {
                String xPath = Constants.XPATH_EXPR_APPLICATION_HANDLERS_START + applicationName + Constants.XPATH_EXPR_APPLICATION_OUTFLOW_HANDLERS_END;
                List<String> strings = xpathGetAttributeValueList(handlerDoc, xPath, Constants.GFAC_CONFIG_HANDLER_CLASS_ATTRIBUTE);
                this.outHandlers.addAll(strings);
            }
            if(providerName != null) {
                String xPath = Constants.XPATH_EXPR_PROVIDER_HANDLERS_START + providerName + Constants.XPATH_EXPR_PROVIDER_OUTFLOW_HANDLERS_END;
                List<String> strings = xpathGetAttributeValueList(handlerDoc, xPath, Constants.GFAC_CONFIG_HANDLER_CLASS_ATTRIBUTE);
                this.outHandlers.addAll(strings);
            }
        } catch (XPathExpressionException e) {
            new GFacException("Error parsing Handler Configuration", e);
        }
    }

    /**
     * Parse GFac configuration file and populate GFacConfiguration object. XML configuration
     * file for GFac will look like below.
     * <p/>
     * <GFac>
     * <GlobalHandlers>
     * <InHandlers>
     * <Handler class="org.apache.airavata.gfac.GlobalHandler1">
     * </InHandler>
     * <OutHandlers>
     * <Handler class="org.apache.airavata.gfac.GlabalHandler2">
     * </OutHandlers>
     * </GlobalHandlers>
     * <Provider class="org.apache.airavata.gfac.providers.LocalProvider" host="LocalHost">
     * <InHandlers>
     * <Handler class="org.apache.airavata.gfac.handlers.LocalEvenSetupHandler">
     * </InHandlers>
     * <OutHandlers>
     * <Handler>org.apache.airavata.LocalOutHandler1</Handler>
     * </OutHandlers>
     * </Provider>
     * <Application name="UltraScan">
     * <InHandlers>
     * <Handler class="org.apache.airavata.gfac.handlers.LocalEvenSetupHandler">
     * </InHandlers>
     * <OutHandlers>
     * <Handler class="org.apache.airavata.gfac.LocalOutHandler1">
     * </OutHandlers>
     * </Application>
     * </GFac>
     *
     * @param configFile configuration file
     * @return GFacConfiguration object.
     */
    //FIXME
    public static GFacConfiguration create(File configFile, AiravataAPI airavataAPI, Properties configurationProperties) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        handlerDoc = docBuilder.parse(configFile);
        return new GFacConfiguration(airavataAPI);
    }

    private static String xpathGetText(Document doc, String expression) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);

        return (String) expr.evaluate(doc, XPathConstants.STRING);
    }

    /**
     * Select matching node set and extract specified attribute value.
     *
     * @param doc        XML document
     * @param expression expression to match node set
     * @param attribute  name of the attribute to extract
     * @return list of attribute values.
     * @throws XPathExpressionException
     */
    private static List<String> xpathGetAttributeValueList(Document doc, String expression, String attribute) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);

        NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        List<String> attributeValues = new ArrayList<String>();

        for (int i = 0; i < nl.getLength(); i++) {
            attributeValues.add(((Element) nl.item(i)).getAttribute(attribute));
        }

        return attributeValues;
    }

    public static GFacConfiguration create(Properties configProps) {
        return null;
    }

    private static List removeDuplicateWithOrder(List arlList) {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = arlList.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (set.add(element))
                newList.add(element);
        }
        arlList.clear();
        arlList.addAll(newList);
        return arlList;
    }

}
