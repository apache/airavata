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
package org.apache.airavata.persistance.registry.mongo.conversion.experiment;

import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.persistance.registry.mongo.conversion.AbstractThriftDeserializer;
import org.apache.thrift.TException;

public class ExperimentDeserializer extends
        AbstractThriftDeserializer<Experiment._Fields, Experiment> {

    @Override
    protected Experiment._Fields getField(final String fieldName) {
        return Experiment._Fields.valueOf(fieldName);
    }

    @Override
    protected Experiment newInstance() {
        return new Experiment();
    }

    @Override
    protected void validate(final Experiment instance) throws TException {
        instance.validate();
    }
}