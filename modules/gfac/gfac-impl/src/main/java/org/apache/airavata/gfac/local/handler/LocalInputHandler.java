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
//*/
//package org.apache.airavata.gfac.local.handler;
//
//import org.apache.airavata.gfac.core.context.JobExecutionContext;
//import org.apache.airavata.gfac.core.handler.AbstractHandler;
//import org.apache.airavata.gfac.core.handler.GFacHandlerException;
//import org.apache.airavata.model.appcatalog.appinterface.DataType;
//import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
//import org.apache.commons.io.FileUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Map;
//import java.util.Properties;
//
//
//public class LocalInputHandler extends AbstractHandler {
//    private static final Logger logger = LoggerFactory.getLogger(LocalInputHandler.class);
//    @Override
//    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
//        super.invoke(jobExecutionContext);
//        Map<String, Object> inputParameters = jobExecutionContext.getInMessageContext().getParameters();
//        for (Map.Entry<String, Object> inputParamEntry : inputParameters.entrySet()) {
//            if (inputParamEntry.getValue() instanceof InputDataObjectType) {
//                InputDataObjectType inputDataObject = (InputDataObjectType) inputParamEntry.getValue();
//                if (inputDataObject.getType() == DataType.URI
//                        && inputDataObject != null
//                        && !inputDataObject.getValue().equals("")) {
//                    try {
//                        inputDataObject.setValue(stageFile(jobExecutionContext.getInputDir(), inputDataObject.getValue()));
//                    } catch (IOException e) {
//                        throw new GFacHandlerException("Error while data staging sourceFile= " + inputDataObject.getValue());
//                    }
//                }
//            }
//        }
//    }
//
//    private String stageFile(String inputDir, String sourceFilePath) throws IOException {
//        int i = sourceFilePath.lastIndexOf(File.separator);
//        String substring = sourceFilePath.substring(i + 1);
//        if (inputDir.endsWith("/")) {
//            inputDir = inputDir.substring(0, inputDir.length() - 1);
//        }
//        String targetFilePath = inputDir + File.separator + substring;
//
//        if (sourceFilePath.startsWith("file")) {
//            sourceFilePath = sourceFilePath.substring(sourceFilePath.indexOf(":") + 1, sourceFilePath.length());
//        }
//
//        File sourceFile = new File(sourceFilePath);
//        File targetFile = new File(targetFilePath);
//        if (targetFile.exists()) {
//            targetFile.delete();
//        }
//        logger.info("staging source file : " + sourceFilePath + " to target file : " + targetFilePath);
//        FileUtils.copyFile(sourceFile, targetFile);
//
//        return targetFilePath;
//    }
//
//    @Override
//    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
//
//    }
//
//    @Override
//    public void initProperties(Properties properties) throws GFacHandlerException {
//
//    }
//}
