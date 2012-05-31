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

package org.apache.airavata.core.gfac.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.xmlbeans.XmlException;

public class OutputUtils {

    private OutputUtils() {
    }

    public static Map<String, ActualParameter> fillOutputFromStdout(MessageContext<ActualParameter> outMessage, String stdout) throws XmlException{

        Map<String, ActualParameter> result = new HashMap<String, ActualParameter>();

        for (Iterator<String> iterator = outMessage.getNames(); iterator.hasNext();) {
            String parameterName = iterator.next();

            // if parameter value is not already set, we let it go
            if (outMessage.getValue(parameterName) == null) {
                continue;
            }

            ActualParameter actual = outMessage.getValue(parameterName);
            String parseStdout = parseStdout(stdout, parameterName);
            if(parseStdout != null){
			MappingFactory.fromString(actual, parseStdout);
            result.put(parameterName, actual);
            }
        }
        return result;
    }

    private static String parseStdout(String stdout, String outParam)throws NullPointerException {
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
            throw new NullPointerException();
        }
    }
}
