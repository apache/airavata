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
package org.apache.airavata.workflow.engine.invoker;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;

public class WorkflowInputUtil {

    public static String createInputForGFacService(WSComponentPort port,String input){
        DataType paramType = port.getType();
        StringBuffer inputString = new StringBuffer("<");
        if("StringParameterType".equals(paramType) || "URIParameterType".equals(paramType) ||
                "DoubleParameterType".equals(paramType) || "IntegerParameterType".equals(paramType)
                || "FloatParameterType".equals(paramType)|| "BooleanParameterType".equals(paramType)
                || "FileParameterType".equals(paramType)){
            inputString.append(port.getName()).append(">").
                    append(getValueElement(input)).append("</").append(port.getName()).append(">");
        }else if(paramType.toString().equals("ArrayType")){
            inputString.append(port.getName()).append(">");
            String[] valueList = StringUtil.getElementsFromString(input);
            for(String inputValue:valueList){
                inputString.append(getValueElement(inputValue));
            }
            inputString.append(getValueElement(port.getName()));
        }
        inputString.append(">");
        return inputString.toString();
    }

    private static String getValueElement(String value){
       return "<value>" + value + "</value>";
    }
}
