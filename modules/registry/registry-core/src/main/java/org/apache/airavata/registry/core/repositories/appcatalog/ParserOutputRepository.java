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
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.parser.ParserOutput;
import org.apache.airavata.registry.core.entities.appcatalog.ParserOutputEntity;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserOutputRepository extends AppCatAbstractRepository<ParserOutput, ParserOutputEntity, String> {

    private static final Logger logger = LoggerFactory.getLogger(ParserInputRepository.class);

    public ParserOutputRepository() {
        super(ParserOutput.class, ParserOutputEntity.class);
    }

    public ParserOutput getParserOutput(String outputId) throws AppCatalogException {
        try {
            return super.get(outputId);
        } catch (Exception e) {
            logger.error("Failed to fetch parser output with id " + outputId, e);
            throw new AppCatalogException("Failed to fetch parser output with id " + outputId, e);
        }
    }
}
