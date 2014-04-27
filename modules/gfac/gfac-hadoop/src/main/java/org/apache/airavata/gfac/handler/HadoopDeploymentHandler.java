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

package org.apache.airavata.gfac.handler;

import com.google.common.io.Files;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.schemas.gfac.HadoopHostType;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.whirr.Cluster;
import org.apache.whirr.ClusterController;
import org.apache.whirr.ClusterControllerFactory;
import org.apache.whirr.ClusterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;

import static org.apache.whirr.ClusterSpec.Property.*;
import static org.apache.whirr.ClusterSpec.Property.INSTANCE_TEMPLATES;
import static org.apache.whirr.ClusterSpec.Property.PRIVATE_KEY_FILE;

/**
 * This handler takes care of deploying hadoop in cloud(in cloud bursting scenarios) and
 * deploying hadoop in local cluster. In case of existing hadoop cluster this will ignore
 * cluster setup just use the hadoop configuration provided by user.
 */
public class HadoopDeploymentHandler implements GFacHandler {
    private static final Logger logger = LoggerFactory.getLogger("hadoop-dep-handler");

    /**
     * Once invoked this method will deploy Hadoop in a local cluster or cloud based on the
     * configuration provided. If there is a already deployed hadoop cluster this will skip
     * deployment.
     *
     * @param jobExecutionContext job execution context containing all the required configurations
     *                            and runtime information.
     * @throws GFacHandlerException
     */
    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        if(jobExecutionContext.isInPath()){
            handleInPath(jobExecutionContext);
        } else {
            handleOutPath(jobExecutionContext);
        }
    }

    private void handleInPath(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        HostDescription hostDescription =
                jobExecutionContext.getApplicationContext().getHostDescription();
        if (!isHadoopDeploymentAvailable(hostDescription)) {
            // Temp directory to keep generated configuration files.
            File tempDirectory = Files.createTempDir();
            try {
                File hadoopSiteXML = launchHadoopCluster(hostDescription, tempDirectory);
                jobExecutionContext.getInMessageContext().addParameter("HADOOP_SITE_XML", hadoopSiteXML.getAbsolutePath());
                jobExecutionContext.getInMessageContext().addParameter("HADOOP_DEPLOYMENT_TYPE", "WHIRR");
                // TODO: Add hadoop-site.xml to job execution context.
            } catch (IOException e) {
                throw new GFacHandlerException("IO Error while processing configurations.",e);
            } catch (ConfigurationException e) {
                throw  new GFacHandlerException("Whirr configuration error.", e);
            } catch (InterruptedException e) {
                throw new GFacHandlerException("Hadoop cluster launch interrupted.", e);
            } catch (TransformerException e) {
                throw new GFacHandlerException("Error while creating hadoop-site.xml", e);
            } catch (ParserConfigurationException e) {
                throw new GFacHandlerException("Error while creating hadoop-site.xml", e);
            }
        } else {
            jobExecutionContext.getInMessageContext().addParameter("HADOOP_DEPLOYMENT_TYPE",
                    "MANUAL");
            jobExecutionContext.getInMessageContext().addParameter("HADOOP_CONFIG_DIR",
                    ((HadoopHostType)hostDescription.getType()).getHadoopConfigurationDirectory());
            logger.info("Hadoop configuration is available. Skipping hadoop deployment.");
            if(logger.isDebugEnabled()){
                logger.debug("Hadoop configuration directory: " +
                        getHadoopConfigDirectory(hostDescription));
            }
        }
    }

    private void handleOutPath(JobExecutionContext jobExecutionContext){
        MessageContext inMessageContext = jobExecutionContext.getInMessageContext();
        if(((String)inMessageContext.getParameter("HADOOP_DEPLOYMENT_TYPE")).equals("WHIRR")){
            // TODO: Shutdown hadoop cluster.
            logger.info("Shutdown hadoop cluster.");
        }
    }

    private File launchHadoopCluster(HostDescription hostDescription, File workingDirectory)
            throws IOException, GFacHandlerException, ConfigurationException, InterruptedException, TransformerException, ParserConfigurationException {
        ClusterSpec hadoopClusterSpec =
                whirrConfigurationToClusterSpec(hostDescription, workingDirectory);
        ClusterController hadoopClusterController =
                createClusterController(hadoopClusterSpec.getServiceName());
        Cluster hadoopCluster =  hadoopClusterController.launchCluster(hadoopClusterSpec);

        logger.info(String.format("Started cluster of %s instances.\n",
                hadoopCluster.getInstances().size()));

        File siteXML = new File(workingDirectory, "hadoop-site.xml");
        clusterPropertiesToHadoopSiteXml(hadoopCluster.getConfiguration(), siteXML);

        return siteXML;
    }

    private ClusterController createClusterController(String serviceName){
        ClusterControllerFactory factory = new ClusterControllerFactory();
        ClusterController controller = factory.create(serviceName);

        if(controller == null){
            logger.warn("Unable to find the service {0}, using default.", serviceName);
            controller = factory.create(null);
        }

        return controller;
    }

    private ClusterSpec whirrConfigurationToClusterSpec(HostDescription hostDescription,
                                                        File workingDirectory) throws IOException, GFacHandlerException, ConfigurationException {
        File whirrConfig = getWhirrConfigurationFile(hostDescription, workingDirectory);
        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
        Configuration configuration = new PropertiesConfiguration(whirrConfig);
        compositeConfiguration.addConfiguration(configuration);

        ClusterSpec hadoopClusterSpec = new ClusterSpec(compositeConfiguration);

        for (ClusterSpec.Property required : EnumSet.of(CLUSTER_NAME, PROVIDER, IDENTITY, CREDENTIAL,
                INSTANCE_TEMPLATES, PRIVATE_KEY_FILE)) {
            if (hadoopClusterSpec.getConfiguration().getString(required.getConfigName()) == null) {
                throw new IllegalArgumentException(String.format("Option '%s' not set.",
                        required.getSimpleName()));
            }
        }

        return hadoopClusterSpec;
    }

    private File getWhirrConfigurationFile(HostDescription hostDescription, File workingDirectory)
            throws GFacHandlerException, IOException {
        HadoopHostType hadoopHostDesc = (HadoopHostType)hostDescription;
        if(hadoopHostDesc.isSetWhirrConfiguration()){
            HadoopHostType.WhirrConfiguration whirrConfig = hadoopHostDesc.getWhirrConfiguration();
            if(whirrConfig.isSetConfigurationFile()){
                File whirrConfigFile = new File(whirrConfig.getConfigurationFile());
                if(!whirrConfigFile.exists()){
                    throw new GFacHandlerException(
                            "Specified whirr configuration file doesn't exists.");
                }

                FileUtils.copyFileToDirectory(whirrConfigFile, workingDirectory);

                return new File(workingDirectory, whirrConfigFile.getName());
            } else if(whirrConfig.isSetConfiguration()){
                Properties whirrConfigProps =
                        whirrConfigurationsToProperties(whirrConfig.getConfiguration());
                File whirrConfigFile = new File(workingDirectory, "whirr-hadoop.config");
                whirrConfigProps.store(
                        new FileOutputStream(whirrConfigFile), null);

                return whirrConfigFile;
            }
        }

        throw new GFacHandlerException("Cannot find Whirr configurations. Whirr configuration "
                + "is required if you don't have already running Hadoop deployment.");
    }

    private Properties whirrConfigurationsToProperties(
            HadoopHostType.WhirrConfiguration.Configuration configuration){
        Properties whirrConfigProps = new Properties();

        for(HadoopHostType.WhirrConfiguration.Configuration.Property property:
                configuration.getPropertyArray()) {
            whirrConfigProps.put(property.getName(), property.getValue());
        }

        return whirrConfigProps;
    }

    private void clusterPropertiesToHadoopSiteXml(Properties props, File hadoopSiteXml) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();

        Document hadoopSiteXmlDoc = documentBuilder.newDocument();

        hadoopSiteXmlDoc.setXmlVersion("1.0");
        hadoopSiteXmlDoc.setXmlStandalone(true);
        hadoopSiteXmlDoc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"configuration.xsl\"");

        Element configEle = hadoopSiteXmlDoc.createElement("configuration");

        hadoopSiteXmlDoc.appendChild(configEle);

        for(Map.Entry<Object, Object> entry : props.entrySet()){
            addPropertyToConfiguration(entry, configEle, hadoopSiteXmlDoc);
        }

        saveDomToFile(hadoopSiteXmlDoc, hadoopSiteXml);
    }

    private void saveDomToFile(Document dom, File destFile) throws TransformerException {
        Source source = new DOMSource(dom);

        Result result = new StreamResult(destFile);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, result);
    }

    private void addPropertyToConfiguration(Map.Entry<Object, Object> entry, Element configElement, Document doc){
        Element property = doc.createElement("property");
        configElement.appendChild(property);

        Element nameEle = doc.createElement("name");
        nameEle.setTextContent(entry.getKey().toString());
        property.appendChild(nameEle);

        Element valueEle = doc.createElement("value");
        valueEle.setTextContent(entry.getValue().toString());
        property.appendChild(valueEle);
    }

    private boolean isHadoopDeploymentAvailable(HostDescription hostDescription) {
        return ((HadoopHostType) hostDescription.getType()).isSetHadoopConfigurationDirectory();
    }

    private String getHadoopConfigDirectory(HostDescription hostDescription){
        return ((HadoopHostType)hostDescription.getType()).getHadoopConfigurationDirectory();
    }

    public void initProperties(Map<String, String> properties) throws GFacHandlerException, GFacException {

    }
}