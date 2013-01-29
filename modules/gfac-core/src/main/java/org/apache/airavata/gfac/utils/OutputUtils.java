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
package org.apache.airavata.gfac.utils;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.StdErrParameterType;
import org.apache.airavata.schemas.gfac.StdOutParameterType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputUtils {
    private OutputUtils() {
    }

    public static Map<String, ActualParameter> fillOutputFromStdout(JobExecutionContext context, String stdout, String stderr) throws Exception {

        Map<String, ActualParameter> result = new HashMap<String, ActualParameter>();
        OutputParameterType[] outputParametersArray = context.getApplicationContext().
                getServiceDescription().getType().getOutputParametersArray();
        MessageContext outMessageContext = context.getOutMessageContext();
        for (OutputParameterType outparamType : outputParametersArray) {
            String parameterName = outparamType.getParameterName();
            ActualParameter actual = (ActualParameter)outMessageContext.getParameter(outparamType.getParameterName());
            // if parameter value is not already set, we let it go
            if (actual == null) {
                continue;
            }
            if ("StdOut".equals(actual.getType().getType().toString())) {
                ((StdOutParameterType) actual.getType()).setValue(stdout);
                result.put(parameterName, actual);
            } else if ("StdErr".equals(actual.getType().getType().toString())) {
                ((StdErrParameterType) actual.getType()).setValue(stderr);
                result.put(parameterName, actual);
            } else {
                String parseStdout = parseStdout(stdout, parameterName);
                if (parseStdout != null) {
                    MappingFactory.fromString(actual, parseStdout);
                    result.put(parameterName, actual);
                }
            }
        }
        return result;
    }

    private static String parseStdout(String stdout, String outParam) throws Exception {
        String regex = Pattern.quote(outParam) + "\\s*=\\s*([^\\[\\s'\"][^\\s]*|\"[^\"]*\"|'[^']*'|\\[[^\\[]*\\])";
        String match = null;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(stdout);
        while (matcher.find()) {
            match = matcher.group(1);
        }
        if (match != null) {
            match = match.trim();
            return match;
        } else {
            throw new Exception("Data for the output parameter '" + outParam + "' was not found");
        }
    }
}
