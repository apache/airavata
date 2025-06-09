/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

namespace java org.apache.airavata.model.appcatalog.parser
namespace php Airavata.Model.AppCatalog.Parser
namespace cpp apache.airavata.model.appcatalog.parser
namespace py airavata.model.appcatalog.parser

enum IOType {
    FILE, PROPERTY
}

struct ParserInput {
    1: required string id;
    2: required string name;
    3: required bool requiredInput;
    4: required string parserId
    5: required IOType type;
}

struct ParserOutput {
    1: required string id;
    2: required string name;
    3: required bool requiredOutput;
    4: required string parserId;
    5: required IOType type;
}

struct Parser {
    1: required string id;
    2: required string imageName;
    3: required string outputDirPath;
    4: required string inputDirPath;
    5: required string executionCommand;
    6: required list<ParserInput> inputFiles;
    7: required list<ParserOutput> outputFiles;
    8: required string gatewayId;
}

struct ParserConnectorInput {
    1: required string id;
    2: required string inputId;
    3: string parentOutputId; // either it is an output of parent parser or a constant value
    4: string value;
    5: required string parserConnectorId;
}

struct ParserConnector {
    1: required string id;
    2: required string parentParserId;
    3: required string childParserId;
    4: required list<ParserConnectorInput> connectorInputs;
    5: required string parsingTemplateId;
}

struct ParsingTemplateInput {
    1: required string id;
    2: required string targetInputId
    3: string applicationOutputName; // either it is an output of application or a constant value
    4: string value;
    5: required string parsingTemplateId;
}

struct ParsingTemplate {
    1: required string id;
    2: required string applicationInterface;
    3: required list<ParsingTemplateInput> initialInputs;
    4: required list<ParserConnector> parserConnections;
    5: required string gatewayId;
}