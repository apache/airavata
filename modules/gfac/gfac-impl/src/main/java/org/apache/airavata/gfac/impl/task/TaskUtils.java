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
package org.apache.airavata.gfac.impl.task;

import org.apache.airavata.gfac.core.context.TaskContext;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class TaskUtils {

    public static URI getDestinationURI(TaskContext taskContext,String hostName, String inputPath, String fileName) throws URISyntaxException {
        String experimentDataDir = taskContext.getParentProcessContext().getProcessModel().getExperimentDataDir();
        String filePath;
        if(experimentDataDir != null && !experimentDataDir.isEmpty()) {
            if(!experimentDataDir.endsWith(File.separator)){
                experimentDataDir += File.separator;
            }
            if (experimentDataDir.startsWith(File.separator)) {
                filePath = experimentDataDir + fileName;
            } else {
                filePath = inputPath + experimentDataDir + fileName;
            }
        } else {
            filePath = inputPath + taskContext.getParentProcessContext().getProcessId() + File.separator + fileName;
        }
        //FIXME
        return new URI("file", taskContext.getParentProcessContext().getStorageResourceLoginUserName(), hostName, 22, filePath, null, null);

    }
}
