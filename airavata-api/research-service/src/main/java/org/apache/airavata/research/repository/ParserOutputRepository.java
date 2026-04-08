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
package org.apache.airavata.research.repository;

import org.apache.airavata.interfaces.AppCatalogException;
import org.apache.airavata.model.appcatalog.parser.proto.Parser;
import org.apache.airavata.model.appcatalog.parser.proto.ParserOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Looks up ParserOutput entries embedded in the Parser JSON column.
 * Direct table-per-row access is no longer available since outputs are stored
 * as a JSON column on the PARSER table.
 */
@Component
public class ParserOutputRepository {

    private static final Logger logger = LoggerFactory.getLogger(ParserOutputRepository.class);

    private final ParserRepository parserRepository;

    public ParserOutputRepository() {
        this.parserRepository = new ParserRepository();
    }

    public ParserOutput getParserOutput(String outputId) throws AppCatalogException {
        try {
            // ParserOutput is embedded in Parser JSON; scan all parsers for the matching output id.
            // This is an O(n) scan - acceptable since parser counts are small.
            for (Parser parser : parserRepository.findAll()) {
                for (ParserOutput output : parser.getOutputFilesList()) {
                    if (outputId.equals(output.getId())) {
                        return output;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to fetch parser output with id " + outputId, e);
            throw new AppCatalogException("Failed to fetch parser output with id " + outputId, e);
        }
    }
}
