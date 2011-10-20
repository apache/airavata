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

package org.apache.airavata.commons.gfac.type;

public class Parameter implements Type {
    private org.apache.airavata.schemas.gfac.Parameter parameterType;
    private DataType type;

    public Parameter() {
        this.parameterType = org.apache.airavata.schemas.gfac.Parameter.Factory.newInstance();
    }

    public Parameter(org.apache.airavata.schemas.gfac.Parameter pt) {
        this.parameterType = pt;
    }

    public String getName() {
        return parameterType.getName();
    }

    public void setName(String name) {
        this.parameterType.setName(name);
    }

    public String getDescription() {
        return parameterType.getDescription();
    }

    public void setDescription(String description) {
        this.parameterType.setDescription(description);
    }

    // TODO
    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }
}