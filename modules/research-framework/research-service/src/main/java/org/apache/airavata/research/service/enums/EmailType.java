/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.research.service.enums;

public enum EmailType {
    NEW_USER {
        public String getSubject() {
            return "New Cybershuttle User";
        }

        public String getMessage(String newUserEmail) {
            return String.format(
                    """
                    Hi, %s has just signed up for Cybershuttle.
                    """,
                    newUserEmail);
        }
    };

    public abstract String getSubject();

    public abstract String getMessage(String newUserEmail);
}
