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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParserServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    ParserService parserService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        parserService = new ParserService(registryHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void getParser_returnsParser() throws Exception {
        Parser parser = new Parser();
        parser.setId("parser-1");
        when(registryHandler.getParser("parser-1", "testGateway")).thenReturn(parser);

        Parser result = parserService.getParser(ctx, "parser-1", "testGateway");

        assertNotNull(result);
        assertEquals("parser-1", result.getId());
    }

    @Test
    void saveParser_returnsParserId() throws Exception {
        Parser parser = new Parser();
        when(registryHandler.saveParser(parser)).thenReturn("parser-saved-id");

        String result = parserService.saveParser(ctx, parser);

        assertEquals("parser-saved-id", result);
        verify(registryHandler).saveParser(parser);
    }

    @Test
    void listAllParsers_returnsList() throws Exception {
        Parser p1 = new Parser();
        Parser p2 = new Parser();
        when(registryHandler.listAllParsers("testGateway")).thenReturn(List.of(p1, p2));

        List<Parser> result = parserService.listAllParsers(ctx, "testGateway");

        assertEquals(2, result.size());
    }

    @Test
    void removeParser_returnsTrue() throws Exception {
        doNothing().when(registryHandler).removeParser("parser-1", "testGateway");

        boolean result = parserService.removeParser(ctx, "parser-1", "testGateway");

        assertTrue(result);
        verify(registryHandler).removeParser("parser-1", "testGateway");
    }

    @Test
    void getParsingTemplate_returnsTemplate() throws Exception {
        ParsingTemplate template = new ParsingTemplate();
        template.setId("tpl-1");
        when(registryHandler.getParsingTemplate("tpl-1", "testGateway")).thenReturn(template);

        ParsingTemplate result = parserService.getParsingTemplate(ctx, "tpl-1", "testGateway");

        assertNotNull(result);
        assertEquals("tpl-1", result.getId());
    }

    @Test
    void saveParsingTemplate_returnsTemplateId() throws Exception {
        ParsingTemplate template = new ParsingTemplate();
        when(registryHandler.saveParsingTemplate(template)).thenReturn("tpl-saved-id");

        String result = parserService.saveParsingTemplate(ctx, template);

        assertEquals("tpl-saved-id", result);
    }

    @Test
    void removeParsingTemplate_returnsTrue() throws Exception {
        doNothing().when(registryHandler).removeParsingTemplate("tpl-1", "testGateway");

        boolean result = parserService.removeParsingTemplate(ctx, "tpl-1", "testGateway");

        assertTrue(result);
    }

    @Test
    void getParsingTemplatesForExperiment_returnsList() throws Exception {
        ParsingTemplate t1 = new ParsingTemplate();
        when(registryHandler.getParsingTemplatesForExperiment("exp-1", "testGateway"))
                .thenReturn(List.of(t1));

        List<ParsingTemplate> result = parserService.getParsingTemplatesForExperiment(ctx, "exp-1", "testGateway");

        assertEquals(1, result.size());
    }

    @Test
    void getParser_wrapsRegistryException() throws Exception {
        when(registryHandler.getParser("bad-parser", "testGateway")).thenThrow(new RuntimeException("DB error"));

        assertThrows(ServiceException.class, () -> parserService.getParser(ctx, "bad-parser", "testGateway"));
    }
}
