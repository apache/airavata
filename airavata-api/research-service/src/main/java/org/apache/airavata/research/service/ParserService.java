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
package org.apache.airavata.research.service;

import java.util.List;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.model.appcatalog.parser.proto.Parser;
import org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ParserService {

    private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

    private final AppCatalogRegistry appCatalogRegistry;

    public ParserService(AppCatalogRegistry appCatalogRegistry) {
        this.appCatalogRegistry = appCatalogRegistry;
    }

    public Parser getParser(RequestContext ctx, String parserId, String gatewayId) throws ServiceException {
        try {
            Parser parser = appCatalogRegistry.getParser(parserId, gatewayId);
            logger.debug("Retrieved parser {} for gateway {}", parserId, gatewayId);
            return parser;
        } catch (Exception e) {
            throw new ServiceException("Error retrieving parser with id: " + parserId + ": " + e.getMessage(), e);
        }
    }

    public String saveParser(RequestContext ctx, Parser parser) throws ServiceException {
        try {
            String parserId = appCatalogRegistry.saveParser(parser);
            logger.debug("Saved parser {} for gateway {}", parserId, ctx.getGatewayId());
            return parserId;
        } catch (Exception e) {
            throw new ServiceException("Error saving the parser: " + e.getMessage(), e);
        }
    }

    public List<Parser> listAllParsers(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            List<Parser> parsers = appCatalogRegistry.listAllParsers(gatewayId);
            logger.debug("Listed {} parsers for gateway {}", parsers.size(), gatewayId);
            return parsers;
        } catch (Exception e) {
            throw new ServiceException("Error listing parsers for gateway " + gatewayId + ": " + e.getMessage(), e);
        }
    }

    public boolean removeParser(RequestContext ctx, String parserId, String gatewayId) throws ServiceException {
        try {
            appCatalogRegistry.removeParser(parserId, gatewayId);
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
            ParsingTemplate parsingTemplate = appCatalogRegistry.getParsingTemplate(templateId, gatewayId);
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
                    appCatalogRegistry.getParsingTemplatesForExperiment(experimentId, gatewayId);
            logger.debug("Retrieved {} parsing templates for experiment {}", parsingTemplates.size(), experimentId);
            return parsingTemplates;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error retrieving parsing templates for experiment: " + experimentId + ": " + e.getMessage(), e);
        }
    }

    public String saveParsingTemplate(RequestContext ctx, ParsingTemplate parsingTemplate) throws ServiceException {
        try {
            String templateId = appCatalogRegistry.saveParsingTemplate(parsingTemplate);
            logger.debug("Saved parsing template {} for gateway {}", templateId, ctx.getGatewayId());
            return templateId;
        } catch (Exception e) {
            throw new ServiceException("Error saving the parsing template: " + e.getMessage(), e);
        }
    }

    public boolean removeParsingTemplate(RequestContext ctx, String templateId, String gatewayId)
            throws ServiceException {
        try {
            appCatalogRegistry.removeParsingTemplate(templateId, gatewayId);
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
            List<ParsingTemplate> templates = appCatalogRegistry.listAllParsingTemplates(gatewayId);
            logger.debug("Listed {} parsing templates for gateway {}", templates.size(), gatewayId);
            return templates;
        } catch (Exception e) {
            throw new ServiceException(
                    "Error listing parsing templates for gateway " + gatewayId + ": " + e.getMessage(), e);
        }
    }
}
