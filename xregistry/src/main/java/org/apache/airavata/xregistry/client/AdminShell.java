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

import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.context.GlobalContext;

import xregistry.generated.ListSubActorsGivenAGroupResponseDocument.ListSubActorsGivenAGroupResponse.Actor;

public class AdminShell extends AbstractShell {
    private Command[] commands;

    public AdminShell(String registryWsdlUrl) throws XregistryException {
        super(new AdminClient(new GlobalContext(true),registryWsdlUrl));
        commands = new Command[] { 
                new Command("addG", registryClient, "<groupID> <desciption>") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        ((AdminClient) getClient()).createGroup(args.get(0),args.get(1));
                        return null;
                    }
                },  
                new Command("addU", registryClient,"<userID> <desciption>") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        ((AdminClient) getClient()).createUser(args.get(0),args.get(1),false);
                        return null;
                    }
                },  
                new Command("addG2G", registryClient,"<groupName> <grouptoAddedName>") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        ((AdminClient) getClient()).addGrouptoGroup(args.get(0),args.get(1));
                        return null;
                    }
                },  
                new Command("addU2G", registryClient,"<groupName> <usertoAdded>") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        ((AdminClient) getClient()).addUsertoGroup(args.get(0),args.get(1));
                        return null;
                    }
                },  
                new Command("rmG", registryClient,"<groupName>") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        ((AdminClient) getClient()).deleteGroup(args.get(0));
                        return null;
                    }
                }, 
                new Command("rmU", registryClient,"<userName>") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        ((AdminClient) getClient()).deleteUser(args.get(0));
                        return null;
                    }
                }, 
                new Command("rmUinG", registryClient,"<groupName> <usertoAdded>") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        ((AdminClient) getClient()).removeUserFromGroup(args.get(0),args.get(1));
                        return null;
                    }
                }, 
                new Command("rmGinG", registryClient,"<groupName> <grouptoRemoved>") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        ((AdminClient) getClient()).removeGroupFromGroup(args.get(0),args.get(1));
                        return null;
                    }
                }, 
                
                new Command("lsU", registryClient,"") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        return ((AdminClient) getClient()).listUsers();
                    }
                }, 
                new Command("lsG", registryClient,"") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        return ((AdminClient) getClient()).listGroups();
                    }
                },
                new Command("lsG4U", registryClient,"<username>") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        return ((AdminClient) getClient()).listGroupsGivenAUser(args.get(0));
                    }
                }, 
                new Command("lsAct4G", registryClient,"groupName") {
                    public String[] handleCommand(ArrayList<String> args)
                            throws XregistryException {
                        Actor[] actors = ((AdminClient) getClient()).listSubActorsGivenAGroup(args.get(0));
                        if(actors == null){
                            return null;
                        }else{
                            String[] actorNames = new String[actors.length];
                            for(int i = 0;i<actorNames.length;i++){
                                actorNames[i] = actors[i].getActor();
                            }
                            return actorNames;
                        }
                        
                    }
                }, 
            };
        
        for(Command cmd:commands){
            commandMap.put(cmd.getName(),cmd);
        }
    }

    public static void main(String[] args) {
        try {
            AbstractShell shell;
            if (args.length > 0) {
                shell = new AdminShell(args[0]);
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
