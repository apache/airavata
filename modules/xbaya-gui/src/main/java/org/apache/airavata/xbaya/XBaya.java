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
package org.apache.airavata.xbaya;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.xbaya.XBayaConfiguration.XBayaExecutionMode;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBaya {

    private static final Logger logger = LoggerFactory.getLogger(XBaya.class);

    private XBayaConfiguration config;

    private XBayaEngine engine;

    public static int preservice = 0;

    /**
     * Constructs an XBayaEngine.
     * 
     * @param args
     */
    public XBaya(String[] args) {
        parseArguments(args);
        try {
            this.engine = new XBayaEngine(this.config);

        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            try {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            } catch (Throwable t) {
                logger.error(e.getMessage(), e);
            }
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            try {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            } catch (Throwable t) {
                // Cannot do anything
                logger.error(e.getMessage(), e);

            }
        }
    }

    /**
     * Returns the XBayaEngine.
     * 
     * @return The XBayaEngine
     */
    public XBayaEngine getEngine() {
        return this.engine;
    }

    private void printUsage() {
        System.err.println("Usage: java " + XBaya.class.getName() + " [-help]" + "[-config file]" + " [-title title]"
                + " [-workflow workflow]" + " [-enableLocalRegistry]" + " [-localRegistry dir]"
                + " [-gpelEngineURL url]" + " [-templateID templateID]" + " [-instanceID instanceID]"
                + " [-gfacURL url]" + " [-dscURL url" + " [-startMonitor {true,false}]" + " [-brokerURL url]"
                + " [-topic topic]" + " [-pullMode {true,false}]" + " [-myProxyServer host]" + " [-karmaURL url]"
                + " [-karmaWorkflowInstanceID]" + " [-myProxyPort port]" + " [-myProxyUsername username]"
                + " [-myProxyLifetime sec]" + " [-loadMyProxy {true,false}]" + " [-messageBoxURL url]"
                + " [-width width]" + " [-height height]" + " [-exitOnClose false/true]" + "[-enableProvenance false/true]"
                + "[-enableProvenanceSmartRun false/true]" + "[-runWithCrossProduct true/false]"+"[-mode ide/monitor]" + "[--x x-coordinates of left top corner] " +
                "+ [--y y-coordinate of left top corner]");
    }

    private void parseArguments(String[] args) {
        try {
            this.config = new XBayaConfiguration();

            int index = 0;
            while (index < args.length) {
                String arg = args[index];
                String possibleValue = "";
                if ((index + 1) < args.length) {
                    possibleValue = args[index + 1];
                }
                logger.debug("arg: " + arg + " " + possibleValue);
                if ("-help".equalsIgnoreCase(arg)) {
                    printUsage();
                    System.exit(0);
                } else if ("-config".equalsIgnoreCase(arg)) {
                    index++;
                    String configPath = args[index];
                    try {
                        this.config.loadConfiguration(configPath);
                    } catch (RuntimeException e) {
                        String message = "Error while reading config file, " + configPath;
                        logger.warn(message, e);
                        this.config.addError(new WorkflowException(message, e));
                    }
                } else if ("-title".equalsIgnoreCase(arg)) {
                    index++;
                    this.config.setTitle(args[index]);
                } else if ("-workflow".equalsIgnoreCase(arg)) {
                    index++;
                    this.config.setWorkflow(args[index]);
                } else if ("-startMonitor".equalsIgnoreCase(arg)) {
                    this.config.setStartMonitor(true);
                } else if ("-brokerURL".equalsIgnoreCase(arg)) {
                    index++;
                    String brokerURL = args[index];
                    try {
                        this.config.setBrokerURL(parseURL(brokerURL));
                    } catch (URISyntaxException e) {
                        String message = "The broker URL is in wrong format: " + brokerURL;
                        logger.warn(message, e);
                        this.config.addError(new WorkflowException(message, e));
                    }
                } else if ("-odeEngine".equalsIgnoreCase(arg)) {
                    index++;
                    this.config.setOdeURL(args[index]);

                } else if ("-templateID".equalsIgnoreCase(arg)) {
                    index++;
                    this.config.setWorkflow(args[index]);

                } else if ("-topic".equalsIgnoreCase(arg)) {

                    index++;
                    this.config.setTopic(args[index]);
                } else if ("-pullMode".equalsIgnoreCase(arg)) {
                    if (index < args.length - 1) {
                        String nextArg = args[index + 1];
                        if (nextArg.startsWith("-")) {
                            this.config.setPullMode(true);
                        } else if ("true".equalsIgnoreCase(nextArg)) {
                            index++;
                            this.config.setPullMode(true);
                        } else if ("false".equalsIgnoreCase(nextArg)) {
                            index++;
                            this.config.setPullMode(false);
                        } else {
                            String message = "-pullMode has to be either true or false, not " + nextArg;
                            logger.warn(message);
                            this.config.addError(new WorkflowException(message));
                        }
                    } else {
                        // This is the last arg
                        this.config.setPullMode(true);
                    }
                } else if ("-messageBoxURL".equalsIgnoreCase(arg) || "-msgBoxURL".equalsIgnoreCase(arg)) {
                    index++;
                    String messageBoxURL = args[index];
                    try {
                        this.config.setMessageBoxURL(parseURL(messageBoxURL));
                    } catch (URISyntaxException e) {
                        String message = "The message box URL is in wrong format: " + messageBoxURL;
                        logger.warn(message, e);
                        this.config.addError(new WorkflowException(message, e));
                    }
                } else if ("-width".equalsIgnoreCase(arg)) {
                    index++;
                    String width = args[index];
                    try {
                        this.config.setWidth(Integer.parseInt(width));
                    } catch (NumberFormatException e) {
                        String message = "The width must be an integer: " + width;
                        logger.warn(message, e);
                        this.config.addError(new WorkflowException(message, e));
                    }
                } else if ("-height".equalsIgnoreCase(arg)) {
                    index++;
                    String height = args[index];
                    try {
                        this.config.setHeight(Integer.parseInt(height));
                    } catch (NumberFormatException e) {
                        String message = "The height must be an integer: " + height;
                        logger.warn(message, e);
                        this.config.addError(new WorkflowException(message, e));
                    }
                } else if ("-exitOnClose".equalsIgnoreCase(arg)) {
                    index++;
                    String exit = args[index];
                    if ("false".equalsIgnoreCase(exit)) {
                        this.config.setCloseOnExit(false);
                    }
                }  else if ("-enableProvenance".equalsIgnoreCase(arg)) {
                    index++;
                    String exit = args[index];
                    if ("true".equalsIgnoreCase(exit)) {
                        this.config.setCollectProvenance(true);
                    }
                }  else if ("-enableProvenanceSmartRun".equalsIgnoreCase(arg)) {
                    index++;
                    String exit = args[index];
                    if ("true".equalsIgnoreCase(exit)) {
                        this.config.setProvenanceSmartRun(true);
                    }
                }  else if ("-runWithCrossProduct".equalsIgnoreCase(arg)) {
                    index++;
                    String exit = args[index];
                    if ("false".equalsIgnoreCase(exit)) {
                        this.config.setRunWithCrossProduct(false);
                    }
                }  else if ("-mode".equalsIgnoreCase(arg)) {
                	index++;
                	String modeValue = args[index].toUpperCase();
                	this.config.setXbayaExecutionMode(XBayaExecutionMode.valueOf(modeValue));
                } else if ("-x".equalsIgnoreCase(arg)) {
                    index++;
                    this.config.setX(Integer.parseInt(args[index]));
                } else if ("-y".equalsIgnoreCase(arg)) {
                    index++;
                    this.config.setY(Integer.parseInt(args[index]));
                } else {
                    String message = "Unknown option: " + arg;
                    logger.error(message);
                    this.config.addError(new WorkflowException(message));
                }
                index++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            String message = "Argument is missing after " + args[args.length - 1];
            logger.error(message, e);
            this.config.addError(new WorkflowException(message));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            String message = "Unknown error while parsing the arguments";
            this.config.addError(new WorkflowException(message, e));
        }
        
    }

    private URI parseURL(String urlString) throws URISyntaxException {
        if (urlString.trim().length() == 0) {
            // This makes it possible to not use some of our default services.
            return null;
        } else if ("null".equalsIgnoreCase(urlString)) {
            // This is a workaround that JNLP doesn't take empty string as an
            // argument.
            return null;
        } else {
            return new URI(urlString).parseServerAuthority();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new XBaya(args);
    }
}

