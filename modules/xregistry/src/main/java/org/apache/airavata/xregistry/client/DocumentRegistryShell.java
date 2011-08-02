/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.airavata.xregistry.client;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.auth.Authorizer;
import org.apache.airavata.xregistry.context.GlobalContext;
import org.apache.airavata.xregistry.doc.AppData;
import org.apache.airavata.xregistry.doc.DocData;
import org.apache.airavata.xregistry.doc.DocParser;
import org.apache.airavata.xregistry.utils.Utils;


public class DocumentRegistryShell extends AbstractShell {
    private Command[] commands;

    public Command[] getCommands() {
        return commands;
    }

    public DocumentRegistryShell(String registryWsdlUrl) throws XregistryException {
        super(new DocumentRegistryClient(new GlobalContext(true), registryWsdlUrl));
        commands = new Command[] { new Command("app2Hosts", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return ((DocumentRegistryClient) getClient()).app2Hosts(args.get(0));
            }

            public String getHelp() {
                return getName() + " <appName>";
            }
        }, new Command("findApp", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                AppData[] results = ((DocumentRegistryClient) getClient()).findAppDesc(args.get(0));
                ArrayList<String> formattedResult = new ArrayList<String>(results.length);

                for (AppData result : results) {
                    formattedResult.add(result.name + " " + result.secondryName);
                }
                return formattedResult.toArray(new String[0]);
            }

            public String getHelp() {
                return getName() + " <appName>";
            }
        }, new Command("findHost", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return Utils.docData2String(((DocumentRegistryClient) getClient()).findHosts(args
                        .get(0)));
            }

            public String getHelp() {
                return getName() + " <hostName>";
            }
        }, new Command("findSM", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return Utils.docData2String(((DocumentRegistryClient) getClient())
                        .findServiceDesc(args.get(0)));
            }

            public String getHelp() {
                return getName() + " <serviceName>";
            }
        }, new Command("findCwsdl", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return Utils.docData2String(((DocumentRegistryClient) getClient())
                        .findServiceInstance(args.get(0)));
            }

            public String getHelp() {
                return getName() + " <cwsdl-name>";
            }
        },

        new Command("lsApp", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return Utils.docData2String(((DocumentRegistryClient) getClient()).findAppDesc(""));
            }

            public String getHelp() {
                return getName();
            }
        }
        , new Command("lsHost", registryClient) {
         public String[] handleCommand(ArrayList<String> args) throws XregistryException {
             return Utils.docData2String(((DocumentRegistryClient) getClient()).findHosts(""));
         }
        }
         , new Command("lsSm", registryClient) {
             public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                 return Utils.docData2String(((DocumentRegistryClient) getClient()).findServiceDesc(""));
             }
         }
         , new Command("lsCwsdl", registryClient) {
             public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                 return Utils.docData2String(((DocumentRegistryClient) getClient()).findServiceInstance(""));
             }
         }

        , new Command("getAwsdl", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return new String[] { ((DocumentRegistryClient) getClient()).getAbstractWsdl(QName.valueOf(args.get(0))) };
            }

            public String getHelp() {
                return getName() + " <awsdlName>";
            }
        }, new Command("getApp", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return new String[] { ((DocumentRegistryClient) getClient()).getAppDesc(
                        args.get(0), args.get(1)) };
            }

            public String getHelp() {
                return getName() + " <appname hostname>";
            }
        }, new Command("getCwsdl", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return new String[] { ((DocumentRegistryClient) getClient()).getConcreateWsdl(QName.valueOf(args.get(0))) };
            }

            public String getHelp() {
                return getName() + " <cwsdlName>";
            }
        }, new Command("getHost", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return new String[] { ((DocumentRegistryClient) getClient()).getHostDesc(args
                        .get(0)) };
            }

            public String getHelp() {
                return getName() + " <hostName>";
            }
        }, new Command("getSM", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                return new String[] { ((DocumentRegistryClient) getClient()).getServiceDesc(QName.valueOf(args
                        .get(0))) };
            }

            public String getHelp() {
                return getName() + " <serviceName>";
            }
        }, new Command("addApp", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                String appDescAsStr = Utils.readFile(args.get(0));
                ((DocumentRegistryClient) getClient()).registerAppDesc(appDescAsStr);
                return null;
            }

            public String getHelp() {

                return getName() + " <service-desc-file,awsdl-file>";
            }
        }, new Command("addSM", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                String serviceDescAsStr = Utils.readFile(args.get(0));
                String awsdlAsStr = DocParser.createWsdl(serviceDescAsStr,true);
                ((DocumentRegistryClient) getClient()).registerServiceDesc(serviceDescAsStr,
                        awsdlAsStr);
                return null;
            }

            public String getHelp() {

                return getName() + " <service-desc-file,awsdl-file>";
            }
        }, new Command("addCwsdl", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                String cwsdlAsStr = Utils.readFile(args.get(0));
                int lifetime = Integer.parseInt(args.get(1));
                ((DocumentRegistryClient) getClient()).registerConcreteWsdl(cwsdlAsStr, lifetime);
                return null;
            }

            public String getHelp() {
                return getName() + " <cwsdl-file lifetimeSeconds>";
            }
        }, new Command("addHost", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                String appdescAsStr = Utils.readFile(args.get(0));
                ((DocumentRegistryClient) getClient()).registerHostDesc(appdescAsStr);
                return null;
            }

            public String getHelp() {

                return getName() + " <appDesc-file>";
            }
        }, new Command("rmApp", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                ((DocumentRegistryClient) getClient()).removeAppDesc(QName.valueOf(args.get(0)), args.get(1));
                return null;
            }

            public String getHelp() {

                return getName() + " <appName,hostName>";
            }
        }, new Command("rmCwsdl", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                ((DocumentRegistryClient) getClient()).removeConcreteWsdl(QName.valueOf(args.get(0)));
                return null;
            }

            public String getHelp() {
                return getName() + " <cwsdl-qname>";
            }
        }, new Command("rmHost", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                ((DocumentRegistryClient) getClient()).removeHostDesc(args.get(0));
                return null;
            }

            public String getHelp() {
                return getName() + " <cwsdl-qname>";
            }
        },new Command("share", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                String action = XregistryConstants.Action.Read.toString();
                ((DocumentRegistryClient) getClient()).addCapability(args.get(0), args.get(1), false, action);
                return null;
            }

            public String getHelp() {
                return getName() + " <cwsdl-qname>";
            }
        }, new Command("rmSM", registryClient) {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                ((DocumentRegistryClient) getClient()).removeServiceDesc(QName.valueOf(args.get(0)));
                return null;
            }

            public String getHelp() {
                return getName() + " <cwsdl-qname>";
            }
        }, };

        for (Command cmd : commands) {
            commandMap.put(cmd.getName(), cmd);
        }

    }

    public static void main(String[] args) {
        try {
            AbstractShell shell;
            if (args.length > 0) {
                shell = new DocumentRegistryShell(args[0]);
                shell.runTheShell();
            } else {
                throw new XregistryException(
                        "initialization Command DocumentRegistryShell <registry-shell-wsdl-url>");
            }
        } catch (XregistryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
