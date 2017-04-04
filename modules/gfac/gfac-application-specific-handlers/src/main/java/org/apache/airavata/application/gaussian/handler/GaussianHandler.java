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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.application.gaussian.handler;
//
//import org.apache.airavata.gfac.core.context.JobExecutionContext;
//import org.apache.airavata.gfac.core.handler.AbstractHandler;
//import org.apache.airavata.gfac.core.handler.GFacHandlerException;
//import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
//import org.apache.airavata.model.experiment.ComputationalResourceScheduling;
//import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
//import org.apache.airavata.registry.cpi.RegistryException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//public class GaussianHandler extends AbstractHandler {
//
//    private static final Logger logger = LoggerFactory.getLogger(GaussianHandler.class);
//    public static final String LINK_SECTION = "%";
//    public static final String ROUTE_SECTION = "#";
//    public static final String PROC_SHARED = "procshared";
//    public static final String MEM = "mem";
//    public static final String CHK = "chk";
//    public static final String PROC = "proc";
//
//    public static final String EQUAL = "=";
//    public static final String OPEN_PARENTHESES = "(";
//    public static final String CLOSE_PARENTHESES = ")";
//
//    private String mainInputFilePath;
//    @Override
//    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
//        super.invoke(jobExecutionContext);
//        Map<String, String> configurations;
//        Map<String, Object> parameters = jobExecutionContext.getInMessageContext().getParameters();
//        // only get first input file, which is the main input file
//        for (Object paraValue : parameters.values()) {
//            if (paraValue instanceof InputDataObjectType) {
//                InputDataObjectType inputDataObjectType = (InputDataObjectType) paraValue;
//                mainInputFilePath = inputDataObjectType.getValue();
//                break;
//            }
//        }
//        if (mainInputFilePath != null) {
//            File mainInputFile = new File(mainInputFilePath);
//            if (mainInputFile.exists()) {
//                try {
//                    configurations = parseGaussianInputFile(mainInputFile);
//                    ComputationalResourceScheduling taskScheduling = jobExecutionContext.getTaskData().getTaskScheduling();
//                    for (Map.Entry<String, String> inputConfig : configurations.entrySet()) {
//                        if (inputConfig.getKey().equals(PROC_SHARED)) {
//                            taskScheduling.setTotalCPUCount(Integer.parseInt(inputConfig.getValue()));
//                        } else if (inputConfig.getKey().equals(MEM)) {
//                            int userRequestedMem = Integer.parseInt(inputConfig.getValue());
//                            int additionalMem = (int) (userRequestedMem * 0.2);
//                            // TODO check (userRequestedMem + additionalMem)  > maxNode or Queue allowed Mem
//                            taskScheduling.setTotalPhysicalMemory(userRequestedMem + additionalMem);
//                        } else if (inputConfig.getKey().equals(PROC)) {
//                            taskScheduling.setTotalCPUCount(Integer.parseInt(inputConfig.getValue()));
//                        } else {
//                            // TODO - handle other input configurations
//                        }
//                        logger.info("$$$$$$$$ " + inputConfig.getKey() + " --> " + inputConfig.getValue() + " $$$$$$$$$$$");
//                    }
//                    experimentCatalog.update(ExperimentCatalogModelType.TASK_DETAIL, jobExecutionContext.getTaskData(), jobExecutionContext.getTaskData().getTaskID());
//                } catch (IOException e) {
//                    throw new GFacHandlerException("Error while reading main input file ", e);
//                } catch (RegistryException e) {
//                    throw new GFacHandlerException("Error while updating task details", e);
//                }
//            } else {
//                throw new GFacHandlerException("Main input file doesn't exists " + mainInputFilePath);
//            }
//
//        } else {
//            throw new GFacHandlerException("Main input file path shouldn't be null");
//        }
//
//    }
//
//    /*   %procshared=6  , put this line to the map key:procshared , value:6
//       keyword = option
//       keyword(option)
//       keyword=(option1, option2, …)
//       keyword(option1, option2, …)*/
//    // TODO - make this method private
//    public Map<String, String> parseGaussianInputFile(File mainInputFile) throws IOException {
//        Map<String, String> configs = new HashMap<String, String>();
//        BufferedReader br = new BufferedReader(new FileReader(mainInputFile));
//        String line = br.readLine();
//        while (line != null) {
//            line = line.trim();
//            String keyword = null;
//            String withoutKeyword = null;
//            String option = null;
//            if (line.startsWith(LINK_SECTION)) {
//                int equalIndex = line.indexOf(EQUAL);
//                int openParenthesesIndex = line.indexOf(OPEN_PARENTHESES);
//                // read the keyword
//                if (equalIndex > 0) {
//                    keyword = line.substring(1, equalIndex).trim();
//                    withoutKeyword = line.substring(equalIndex + 1, line.length()); // remove up to = sign
//                } else if (openParenthesesIndex > 0) {
//                    keyword = line.substring(1, openParenthesesIndex).trim();
//                    withoutKeyword = line.substring(openParenthesesIndex, line.length()); // remove left side of ( sign
//                } else {
//                    // TODO - malformed input configuration
//                }
//                // read the option
//                if (openParenthesesIndex > 0) {
//                    if (withoutKeyword.endsWith(CLOSE_PARENTHESES)) {
//                        option = withoutKeyword.substring(1, withoutKeyword.length() - 1);
//                    } else {
//                        //TODO -  malformed input configuration
//                    }
//                } else {
//                    option = withoutKeyword.trim();
//                }
//                configs.put(keyword, option);
//            } else if (line.startsWith(ROUTE_SECTION)) {
//                // parse the line
//            }
//            line = br.readLine();
//        }
//        return configs;
//    }
//
//    @Override
//    public void initProperties(Properties properties) throws GFacHandlerException {
//
//    }
//
//    @Override
//    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
//        // Implement this method if we need safe recover steps before rerun the task.
//    }
//}
