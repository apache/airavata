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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.handler.GFacHandlerException;
import org.apache.airavata.schemas.gfac.StdErrParameterType;
import org.apache.airavata.schemas.gfac.StdOutParameterType;
import org.apache.airavata.schemas.gfac.URIParameterType;

public class OutputUtils {
    private static String regexPattern = "\\s*=\\s*([^\\[\\s'\"][^\\s]*|\"[^\"]*\"|'[^']*'|\\[[^\\[]*\\])";

    public static Map<String, ActualParameter> fillOutputFromStdout(Map<String, Object> output, String stdout, String stderr) throws Exception {

        if (stdout == null || stdout.equals("")){
            throw new GFacHandlerException("Standard output is empty.");
        }

        Map<String, ActualParameter> result = new HashMap<String, ActualParameter>();
        Set<String> keys = output.keySet();
        for (String paramName : keys) {
        	ActualParameter actual = (ActualParameter) output.get(paramName);
            // if parameter value is not already set, we let it go
            
            if (actual == null) {
                continue;
            }
            if ("StdOut".equals(actual.getType().getType().toString())) {
                ((StdOutParameterType) actual.getType()).setValue(stdout);
                result.put(paramName, actual);
            } else if ("StdErr".equals(actual.getType().getType().toString())) {
                ((StdErrParameterType) actual.getType()).setValue(stderr);
                result.put(paramName, actual);
            } else {
            	if ("URI".equals(actual.getType().getType().toString()) &&  !((URIParameterType) actual.getType()).getValue().isEmpty()){
            		continue;
            	}
                String parseStdout = parseStdout(stdout, paramName);
                if (parseStdout != null) {
                    MappingFactory.fromString(actual, parseStdout);
                    result.put(paramName, actual);
                }
            }
        }

        return result;
    }

    private static String parseStdout(String stdout, String outParam) throws Exception {
        String regex = Pattern.quote(outParam) + regexPattern;
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

    public static String[] parseStdoutArray(String stdout, String outParam) throws Exception {
        String regex = Pattern.quote(outParam) + regexPattern;
        StringBuffer match = new StringBuffer();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(stdout);
        while (matcher.find()) {
            match.append(matcher.group(1) + ",");
        }
        if (match != null) {
            return match.toString().split(",");
        } else {
            throw new Exception("Data for the output parameter '" + outParam + "' was not found");
        }
    }
}
