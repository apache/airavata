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

package org.apache.airavata.gfac.core.provider.utils;

import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.provider.GFacProviderException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProviderUtils {

    public static List<String> getInputParameters(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        List<String> parameters = new ArrayList<String>();
        MessageContext inMessageContext = jobExecutionContext.getInMessageContext();
        Map<String, Object> inputs = inMessageContext.getParameters();
        for (String inputParam : inputs.keySet()) {
            parameters.add(inputParam);
        }
        return parameters;
    }

}
