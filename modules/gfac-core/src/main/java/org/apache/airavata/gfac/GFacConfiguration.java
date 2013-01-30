package org.apache.airavata.gfac;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.exception.UnspecifiedApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.utils.GridConfigurationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GFacConfiguration {
    public static final Logger log = LoggerFactory.getLogger(GFacConfiguration.class);
    public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
    public static final String MYPROXY_SERVER = "myproxy.server";
    public static final String MYPROXY_USER = "myproxy.user";
    public static final String MYPROXY_PASS = "myproxy.pass";
    public static final String MYPROXY_LIFE = "myproxy.life";

    private String myProxyServer;

    private String myProxyUser;

    private String myProxyPassphrase;

    private int myProxyLifeCycle;

    private AiravataAPI airavataAPI;

    private String trustedCertLocation;

    // Keep list of full qualified class names of GFac handlers which should invoked before
    // the provider
    private List<String> inHandlers = new ArrayList<String>();

    // Keep list of full qualified class names of GFac handlers which should invoked after
    // the provider
    private List<String> outHandlers = new ArrayList<String>();

    private static List<GridConfigurationHandler> gridConfigurationHandlers;

    private static String GRID_HANDLERS="airavata.grid.handlers";

    static{
    	gridConfigurationHandlers=new ArrayList<GridConfigurationHandler>();
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
					log.error("Error while loading Grid Configuration Handler class "+handlerClass, e);
				}
			}
		} catch (UnspecifiedApplicationSettingsException e) {
			//no handlers defined
		} catch (ApplicationSettingsException e1) {
			log.error("Error in reading Configuration handler data!!!",e1);
		}
    }

    public static GridConfigurationHandler[] getGridConfigurationHandlers(){
    	return gridConfigurationHandlers.toArray(new GridConfigurationHandler[]{});
    }
    public GFacConfiguration(AiravataAPI airavataAPI, Properties configurationProperties) {
        this.airavataAPI = airavataAPI;
        if (configurationProperties != null) {
            myProxyUser = configurationProperties.getProperty(MYPROXY_USER);
            myProxyServer = configurationProperties.getProperty(MYPROXY_SERVER);
            myProxyPassphrase = configurationProperties.getProperty(MYPROXY_PASS);
            myProxyLifeCycle = Integer.parseInt(configurationProperties.getProperty(MYPROXY_LIFE));
            trustedCertLocation = configurationProperties.getProperty(TRUSTED_CERT_LOCATION);
        } else {
            throw new NullPointerException("GFac Configuration properties cannot be null.");
        }
    }

    public GFacConfiguration(AiravataAPI airavataAPI){
        this.airavataAPI = airavataAPI;
    }

    public String getMyProxyServer() {
        return myProxyServer;
    }

    public String getMyProxyUser() {
        return myProxyUser;
    }

    public String getMyProxyPassphrase() {
        return myProxyPassphrase;
    }

    public int getMyProxyLifeCycle() {
        return myProxyLifeCycle;
    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public String getTrustedCertLocation() {
        return trustedCertLocation;
    }

    public List<String> getInHandlers() {
        return inHandlers;
    }

    public List<String> getOutHandlers() {
        return outHandlers;
    }

    public void setMyProxyServer(String myProxyServer) {
        this.myProxyServer = myProxyServer;
    }

    public void setMyProxyUser(String myProxyUser) {
        this.myProxyUser = myProxyUser;
    }

    public void setMyProxyPassphrase(String myProxyPassphrase) {
        this.myProxyPassphrase = myProxyPassphrase;
    }

    public void setMyProxyLifeCycle(int myProxyLifeCycle) {
        this.myProxyLifeCycle = myProxyLifeCycle;
    }

    public void setTrustedCertLocation(String trustedCertLocation) {
        this.trustedCertLocation = trustedCertLocation;
    }

    public void setInHandlers(List<String> inHandlers) {
        this.inHandlers = inHandlers;
    }

    public void setOutHandlers(List<String> outHandlers) {
        this.outHandlers = outHandlers;
    }

    /**
     * Parse GFac configuration file and populate GFacConfiguration object. XML configuration
     * file for GFac will look like below.
     *
     * <GFac>
     *     <MyProxy>
     *         <Server></Server>
     *         <User></User>
     *         <Passphrase></Passphrase>
     *         <LifeCycle></LifeCycle>
     *     </MyProxy>
     *     <Handlers>
     *         <InFlow>
     *             <Handler class="org.apache.airavata.gfac.handler.impl.HadoopDeploymentHandler"/>
     *         </InFlow>
     *         <OutFlow>
     *             <Handler class="org.apache.airavata.gfac.handler.impl.HadoopDeploymentHandler"/>
     *         </OutFlow>
     *     </Handlers>
     * </GFac>
     *
     * @param configFile configuration file
     * @return GFacConfiguration object.
     */
    public static GFacConfiguration create(File configFile, AiravataAPI airavataAPI) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(configFile);

        GFacConfiguration configuration = new GFacConfiguration(airavataAPI);

        configuration.setMyProxyServer(xpathGetText(doc, Constants.XPATH_EXPR_MYPROXY_SERVER));
        configuration.setMyProxyUser(xpathGetText(doc, Constants.XPATH_EXPR_MYPROXY_USER));
        configuration.setMyProxyPassphrase(xpathGetText(doc,
                Constants.XPATH_EXPR_MYPROXY_PASSPHRASE));
        configuration.setMyProxyLifeCycle(Integer.parseInt(
                xpathGetText(doc, Constants.XPATH_EXPR_MYPROXY_LIFECYCLE)));

        configuration.setInHandlers(xpathGetAttributeValueList(doc,
                Constants.XPATH_EXPR_INFLOW_HANDLERS,
                Constants.GFAC_CONFIG_HANDLER_CLASS_ATTRIBUTE));
        configuration.setOutHandlers(xpathGetAttributeValueList(doc,
                Constants.XPATH_EXPR_OUTFLOW_HANDLERS,
                Constants.GFAC_CONFIG_HANDLER_CLASS_ATTRIBUTE));

        return configuration;
    }

    private static String xpathGetText(Document doc, String expression) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);

        return (String)expr.evaluate(doc, XPathConstants.STRING);
    }

    /**
     * Select matching node set and extract specified attribute value.
     * @param doc  XML document
     * @param expression  expression to match node set
     * @param attribute name of the attribute to extract
     * @return list of attribute values.
     * @throws XPathExpressionException
     */
    private static List<String> xpathGetAttributeValueList(Document doc, String expression, String attribute) throws XPathExpressionException{
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);

        NodeList nl = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);

        List<String> attributeValues = new ArrayList<String>();

        for(int i = 0; i < nl.getLength(); i++){
            attributeValues.add(((Element)nl.item(i)).getAttribute(attribute));
        }

        return attributeValues;
    }

    public static GFacConfiguration create(Properties configProps){
        return null;
    }

}
