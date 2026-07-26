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
package org.apache.airavata.models.appcatalog.appdeployment;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.appdeployment.proto.CommandObject}.
 */
public record CommandObject(String command, int commandOrder) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String command = "";
        private int commandOrder;

        private Builder() {}

        private Builder(CommandObject source) {
            this.command = source.command;
            this.commandOrder = source.commandOrder;
        }

        public Builder setCommand(String command) {
            this.command = command;
            return this;
        }

        public Builder setCommandOrder(int commandOrder) {
            this.commandOrder = commandOrder;
            return this;
        }

        public CommandObject build() {
            return new CommandObject(command, commandOrder);
        }
    }
}
