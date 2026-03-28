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
package org.apache.airavata.compute.service;

import java.util.List;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceException;
import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserService {

    private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

    private final RegistryServerHandler registryHandler;

    public ParserService(RegistryServerHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    public Parser getParser(RequestContext ctx, String parserId, String gatewayId) throws ServiceException {
        try {
            Parser parser = registryHandler.getParser(parserId, gatewayId);
            logger.debug("Retrieved parser {} for gateway {}", parserId, gatewayId);
            return parser;
        } catch (Exception e) {
            throw new ServiceException("Error retrieving parser with id: " + parserId + ": " + e.getMessage(), e);
        }
    }

    public String saveParser(RequestContext ctx, Parser parser) throws ServiceException {
        try {
            String parserId = registryHandler.saveParser(parser);
            logger.debug("Saved parser {} for gateway {}", parserId, ctx.getGatewayId());
            return parserId;
        } catch (Exception e) {
            throw new ServiceException("Error saving the parser: " + e.getMessage(), e);
        }
    }

    public List<Parser> listAllParsers(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            List<Parser> parsers = registryHandler.listAllParsers(gatewayId);
            logger.debug("Listed {} parsers for gateway {}", parsers.size(), gatewayId);
            return parsers;
        } catch (Exception e) {
            throw new ServiceException("Error listing parsers for gateway " + gatewayId + ": " + e.getMessage(), e);
        }
    }

    public boolean removeParser(RequestContext ctx, String parserId, String gatewayId) throws ServiceException {
        try {
            registryHandler.removeParser(parserId, gatewayId);
            logger.debug("Removed parser {} from gateway {}", parserId, gatewayId);
            return true;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error removing parser " + parserId + " in gateway " + gatewayId + ": " + e.getMessage(), e);
        }
    }

    public ParsingTemplate getParsingTemplate(RequestContext ctx, String templateId, String gatewayId)
            throws ServiceException {
        try {
            ParsingTemplate parsingTemplate = registryHandler.getParsingTemplate(templateId, gatewayId);
            logger.debug("Retrieved parsing template {} for gateway {}", templateId, gatewayId);
            return parsingTemplate;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving parsing template with id: " + templateId + ": " + e.getMessage(), e);
        }
    }

    public List<ParsingTemplate> getParsingTemplatesForExperiment(
            RequestContext ctx, String experimentId, String gatewayId) throws ServiceException {
        try {
            List<ParsingTemplate> parsingTemplates =
                    registryHandler.getParsingTemplatesForExperiment(experimentId, gatewayId);
            logger.debug("Retrieved {} parsing templates for experiment {}", parsingTemplates.size(), experimentId);
            return parsingTemplates;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving parsing templates for experiment: " + experimentId + ": " + e.getMessage(), e);
        }
    }

    public String saveParsingTemplate(RequestContext ctx, ParsingTemplate parsingTemplate) throws ServiceException {
        try {
            String templateId = registryHandler.saveParsingTemplate(parsingTemplate);
            logger.debug("Saved parsing template {} for gateway {}", templateId, ctx.getGatewayId());
            return templateId;
        } catch (Exception e) {
            throw new ServiceException("Error saving the parsing template: " + e.getMessage(), e);
        }
    }

    public boolean removeParsingTemplate(RequestContext ctx, String templateId, String gatewayId)
            throws ServiceException {
        try {
            registryHandler.removeParsingTemplate(templateId, gatewayId);
            logger.debug("Removed parsing template {} from gateway {}", templateId, gatewayId);
            return true;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error removing parsing template " + templateId + " in gateway " + gatewayId + ": "
                            + e.getMessage(),
                    e);
        }
    }

    public List<ParsingTemplate> listAllParsingTemplates(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            List<ParsingTemplate> templates = registryHandler.listAllParsingTemplates(gatewayId);
            logger.debug("Listed {} parsing templates for gateway {}", templates.size(), gatewayId);
            return templates;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error listing parsing templates for gateway " + gatewayId + ": " + e.getMessage(), e);
        }
    }
}
