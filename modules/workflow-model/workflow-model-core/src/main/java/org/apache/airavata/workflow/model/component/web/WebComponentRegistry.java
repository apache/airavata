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
package org.apache.airavata.workflow.model.component.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.workflow.model.component.ComponentReference;
import org.apache.airavata.workflow.model.component.ComponentRegistry;
import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.component.ws.WSComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebComponentRegistry extends ComponentRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebComponentRegistry.class);

    private URL url;

    private List<ComponentReference> tree;

    private Map<String, List<WSComponent>> componentsMap;

    /**
     * Creates a WebComponentRegistryClient
     * 
     * @param urlString
     * @throws MalformedURLException
     * @throws IOException
     */
    public WebComponentRegistry(String urlString) throws MalformedURLException, IOException {
        this(new URL(urlString));
    }

    /**
     * Creates a WebComponentRegistryClient
     * 
     * @param url
     *            The URL of the web page.
     */
    public WebComponentRegistry(URL url) {
        this.url = url;
        this.componentsMap = new HashMap<String, List<WSComponent>>();
    }

    /**
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return this.url.toString();
    }

    /**
     * @see org.apache.airavata.workflow.model.component.registry.ComponentRegistry#getComponentReferenceList()
     */
    @Override
    public List<ComponentReference> getComponentReferenceList() throws ComponentRegistryException {
        tree = new ArrayList<ComponentReference>();
        parse();
        return this.tree;
    }

    /**
     * Returns a list of component of a specified name.
     * 
     * @param name
     *            The name of the component. The name here is a relative URL specified in <a href="name"> tag, and is
     *            same as the name of a corresponding ComponentTree.
     * @return The list of components of the specified name
     */
    public List<WSComponent> getComponents(String name) {
        // This method is only used from a test.
        List<WSComponent> components = this.componentsMap.get(name);
        return components;
    }

    private void parse() throws ComponentRegistryException {
        try {

            HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();

            int count = 0;
            // TODO checking 3 is not enough
            while (String.valueOf(connection.getResponseCode()).startsWith("3")) {
                String location = connection.getHeaderField("Location");
                logger.debug("Redirecting to " + location);
                connection.disconnect();
                this.url = new URL(location);
                connection = (HttpURLConnection) this.url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                count++;
                if (count > 10) {
                    throw new ComponentRegistryException("Too many redirect");
                }
            }

            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            HtmlRegistryParserCallback callback = new HtmlRegistryParserCallback();
            ParserDelegator parser = new ParserDelegator();
            parser.parse(reader, callback, false);
        } catch (IOException e) {
            throw new ComponentRegistryException(e);
        }
    }

    private void addComponents(String name) {
        try {
            URL wsdlUrl = new URL(this.url, name);
            logger.debug("WSDL URL: " + wsdlUrl);
            String wsdlString = IOUtil.readToString(wsdlUrl.openStream());
            logger.debug("WSDL: " + wsdlString);
            List<WSComponent> components = WSComponentFactory.createComponents(wsdlString);
            addComponents(name, components);
        } catch (MalformedURLException e) {
            // Ignore
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            // Ignore
            logger.error(e.getMessage(), e);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addComponents(String name, List<WSComponent> components) {
        this.componentsMap.put(name, components);
        WebComponentReference componentReference = new WebComponentReference(name, components);
        this.tree.add(componentReference);
    }

    private class HtmlRegistryParserCallback extends HTMLEditorKit.ParserCallback {

        /**
         * @see javax.swing.text.html.HTMLEditorKit.ParserCallback#handleStartTag(javax.swing.text.html.HTML.Tag,
         *      javax.swing.text.MutableAttributeSet, int)
         */
        @Override
        public void handleStartTag(Tag tag, MutableAttributeSet attrSet, int pos) {
            if (tag == HTML.Tag.A) {
                String name = (String) attrSet.getAttribute(HTML.Attribute.HREF);
                addComponents(name);
            }
        }
    }
}