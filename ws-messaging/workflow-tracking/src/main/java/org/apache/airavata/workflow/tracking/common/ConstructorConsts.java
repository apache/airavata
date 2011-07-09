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

package org.apache.airavata.workflow.tracking.common;

import org.apache.airavata.workflow.tracking.client.Callback;

/**
 * This contains the names of parameters passed to the notifier constructor. e.g. props =
 * util.Props.newProps(CONSTS.WORKFLOW_ID, "wfId001"). set(CONSTS.NODE_ID, "nodeId001"). set(CONSTS.TIMESTEP,
 * "time0001"). set(CONSTS.BROKER_URL, "rainier:12346")); Notifier notifier = NotifierFactory.createNotifier(props);
 */
public enum ConstructorConsts {

    NOTIFIER_IMPL_CLASS(String.class), ENABLE_BATCH_PROVENANCE(String.class), PUBLISHER_IMPL_CLASS(String.class), ENABLE_ASYNC_PUBLISH(
            String.class), TOPIC(String.class), CALLBACK_LISTENER(Callback.class), BROKER_EPR(String.class), ANNOTATIONS(
            AnnotationProps.class), KARMA_URL(String.class), KARMA_IMPL(Object.class);

    public Class getValueType() {
        return valueType;
    }

    public boolean checkValueType(Class otherType) {
        if (otherType == null)
            return false;
        if (valueType.isAssignableFrom(otherType))
            return true;
        else
            return false;
    }

    private Class valueType;

    private ConstructorConsts(Class type_) {
        valueType = type_;
    }
}
