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
 */
package org.apache.airavata.helix.impl.task.parsing;

import java.util.HashMap;
import java.util.Map;

public class ParserDAGElement {
    private String parentParser; //returns the id
    private String childParser; // returns the id
    // input of the child parser output of the parent parser
    private Map<String, String> inputOutputMapping = new HashMap<>();

    public String getParentParser() {
        return parentParser;
    }

    public void setParentParser(String parentParser) {
        this.parentParser = parentParser;
    }

    public String getChildParser() {
        return childParser;
    }

    public void setChildParser(String childParser) {
        this.childParser = childParser;
    }

    public Map<String, String> getInputOutputMapping() {
        return inputOutputMapping;
    }

    public void setInputOutputMapping(Map<String, String> inputOutputMapping) {
        this.inputOutputMapping = inputOutputMapping;
    }
}
