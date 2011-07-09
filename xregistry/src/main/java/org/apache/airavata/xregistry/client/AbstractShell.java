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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.airavata.xregistry.XregistryException;


public abstract class AbstractShell {
    public static final String REGISTRY_SHELL_PROMPT = "#xregistry>";

    protected Object registryClient;

    protected final HashMap<String, Command> commandMap;

    public AbstractShell(Object registryClientpassed) throws XregistryException {
        this.registryClient = registryClientpassed;
        commandMap = new LinkedHashMap<String, Command>();

        Command helpcmd = new Command("?", registryClient, "- prints this help") {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                ArrayList<String> results = new ArrayList<String>();
                for (Command cmd : commandMap.values()) {
                    results.add(cmd.getHelp());
                }
                return results.toArray(new String[0]);
            }
        };
        Command exitcmd = new Command("exit", registryClient, "Exit from Shell") {
            public String[] handleCommand(ArrayList<String> args) throws XregistryException {
                System.exit(0);
                return null;
            }
        };
        commandMap.put(helpcmd.getName(), helpcmd);
        commandMap.put(exitcmd.getName(), exitcmd);
    }

    public void handleCommand(Command cmd, ArrayList<String> parameters) throws XregistryException {
        try {
            String[] results = cmd.handleCommand(parameters);
            if (results != null) {
                for (String result : results) {
                    System.out.println(result);
                }
            }
        } catch (RuntimeException e) {
            System.out.println(cmd.getName() + " Falied Command format is " + cmd.getHelp());
            e.printStackTrace();
        }
    }

    public void runTheShell() throws XregistryException {
        try {
            System.out.print(REGISTRY_SHELL_PROMPT);
            BufferedReader commandReader = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            boolean running = true;
            while (running) {
                line = commandReader.readLine();
                if (line.trim().length() > 0) {
                    String command = findNextToken(line, 0);
                    // StringTokenizer t = new StringTokenizer(line);
                    // String command = t.nextToken();

                    ArrayList<String> commandArgs = new ArrayList<String>();
                    int start = command.length() + 1;
                    String token = findNextToken(line, start);
                    while (token != null) {
                        commandArgs.add(token.replaceAll("\'", "").replaceAll("\"", ""));
                        start = start + token.length() + 1;
                        token = findNextToken(line, start);
                    }

                    Command cmd = commandMap.get(command);
                    if (cmd != null) {
                        if (commandArgs.size() > 0 && "?".equals(commandArgs.get(0))) {
                            System.out.println(cmd.getHelp());
                        }
                        try{
                            handleCommand(cmd, commandArgs);    
                        }catch(Exception e){
                            System.out.print("Error:"+e.getMessage());
                        }
                    }else{
                        System.out.println("Command "+ command + " not found");
                    }
                }
                System.out.print(REGISTRY_SHELL_PROMPT);
            }
        } catch (IOException e) {
            throw new XregistryException(e);
        }
    }

    private String findNextToken(String command, int start) {

        while (start < command.length()) {
            if (Character.isWhitespace(command.charAt(start))) {
                start++;
            } else {
                int end = 0;
                char c = command.charAt(start);
                if ('\"' == c) {
                    end = command.indexOf('\"', start + 1);
                } else if ('\'' == c) {
                    end = command.indexOf('\'', start + 1);
                } else {
                    end = command.indexOf(' ', start + 1);
                }

                if (end > 0) {
                    return command.substring(start, end);
                } else {
                    return command.substring(start);
                }
            }
        }
        return null;
    }

}
