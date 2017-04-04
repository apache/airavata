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
//import org.apache.airavata.gfac.core.handler.GFacHandler;
//import org.apache.airavata.gfac.core.handler.GFacHandlerException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.util.Properties;
//
//public class LocalDirectorySetupHandler implements GFacHandler {
//    private static final Logger log = LoggerFactory.getLogger(LocalDirectorySetupHandler.class);
//
//    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
//        log.info("Invoking LocalDirectorySetupHandler ...");
//        log.debug("working directory = " + jobExecutionContext.getWorkingDir());
//        log.debug("temp directory = " + jobExecutionContext.getWorkingDir());
//
//        makeFileSystemDir(jobExecutionContext.getWorkingDir());
//        makeFileSystemDir(jobExecutionContext.getInputDir());
//        makeFileSystemDir(jobExecutionContext.getOutputDir());
//    }
//
//    @Override
//    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
//        // TODO: Auto generated method body.
//    }
//
//    private void makeFileSystemDir(String dir) throws GFacHandlerException {
//           File f = new File(dir);
//           if (f.isDirectory() && f.exists()) {
//               return;
//           } else if (!new File(dir).mkdir()) {
//               throw new GFacHandlerException("Cannot create directory " + dir);
//           }
//    }
//
//    public void initProperties(Properties properties) throws GFacHandlerException {
//
//    }
//}
