package org.apache.airavata.agent.connection.service.controllers;

import javax.validation.Valid;

import org.apache.airavata.agent.connection.service.handlers.AgentConnectionHandler;
import org.apache.airavata.agent.connection.service.models.AgentCommandExecutionAck;
import org.apache.airavata.agent.connection.service.models.AgentCommandExecutionRequest;
import org.apache.airavata.agent.connection.service.models.AgentCommandExecutionResponse;
import org.apache.airavata.agent.connection.service.models.AgentEnvSetupAck;
import org.apache.airavata.agent.connection.service.models.AgentEnvSetupRequest;
import org.apache.airavata.agent.connection.service.models.AgentEnvSetupResponse;
import org.apache.airavata.agent.connection.service.models.AgentInfoResponse;
import org.apache.airavata.agent.connection.service.models.AgentJupyterExecutionAck;
import org.apache.airavata.agent.connection.service.models.AgentJupyterExecutionRequest;
import org.apache.airavata.agent.connection.service.models.AgentJupyterExecutionResponse;
import org.apache.airavata.agent.connection.service.models.AgentPythonExecutionAck;
import org.apache.airavata.agent.connection.service.models.AgentPythonExecutionRequest;
import org.apache.airavata.agent.connection.service.models.AgentPythonExecutionResponse;
import org.apache.airavata.agent.connection.service.models.AgentTunnelAck;
import org.apache.airavata.agent.connection.service.models.AgentTunnelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final static Logger logger = LoggerFactory.getLogger(AgentController.class);
    private final AgentConnectionHandler agentConnectionHandler;

    public AgentController(AgentConnectionHandler agentConnectionHandler) {
        this.agentConnectionHandler = agentConnectionHandler;
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<AgentInfoResponse> getAgentInfo(@PathVariable("agentId") String agentId) {
        return ResponseEntity.accepted().body(agentConnectionHandler.isAgentUp(agentId));
    }

    @PostMapping("/create/tunnel")
    public ResponseEntity<AgentTunnelAck> runTunnelCreationOnAgent(@Valid @RequestBody AgentTunnelRequest tunnelRequest) {
        return ResponseEntity.accepted().body(agentConnectionHandler.runTunnelOnAgent(tunnelRequest));
    }

    @PostMapping("/create/env")
    public ResponseEntity<AgentEnvSetupAck> runEnvSetupOnAgent(@Valid @RequestBody AgentEnvSetupRequest envSetupRequest) {
        logger.info("Received env setup request to run on agent {}", envSetupRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(envSetupRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runEnvSetupOnAgent(envSetupRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", envSetupRequest.getAgentId());
            AgentEnvSetupAck ack = new AgentEnvSetupAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/create/env/{executionId}")
    public ResponseEntity<AgentEnvSetupResponse> getEnvSetupResponse(@PathVariable("executionId") String executionId) {
        return ResponseEntity.accepted().body(agentConnectionHandler.getEnvSetupResponse(executionId));
    }

    @PostMapping("/execute/shell")
    public ResponseEntity<AgentCommandExecutionAck> runCommandOnAgent(@Valid @RequestBody AgentCommandExecutionRequest commandRequest) {
        logger.info("Received command request to run on agent {}", commandRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(commandRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runCommandOnAgent(commandRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", commandRequest.getAgentId());
            AgentCommandExecutionAck ack = new AgentCommandExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/execute/shell/{executionId}")
    public ResponseEntity<AgentCommandExecutionResponse> getExecutionResponse(@PathVariable("executionId") String executionId) {
        return ResponseEntity.accepted().body(agentConnectionHandler.getCommandExecutionResponse(executionId));
    }

    @PostMapping("/execute/jupyter")
    public ResponseEntity<AgentJupyterExecutionAck> runJupyterOnAgent(@Valid @RequestBody AgentJupyterExecutionRequest executionRequest) {
        logger.info("Received jupyter execution request to run on agent {}", executionRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(executionRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runJupyterOnAgent(executionRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", executionRequest.getAgentId());
            AgentJupyterExecutionAck ack = new AgentJupyterExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/execute/jupyter/{executionId}")
    public ResponseEntity<AgentJupyterExecutionResponse> getJupyterResponse(@PathVariable("executionId") String executionId) {
        return ResponseEntity.accepted().body(agentConnectionHandler.getJupyterExecutionResponse(executionId));
    }

    @PostMapping("/execute/python")
    public ResponseEntity<AgentPythonExecutionAck> runPythonOnAgent(@Valid @RequestBody AgentPythonExecutionRequest pythonRunRequest) {
        logger.info("Received python execution request to run on agent {}", pythonRunRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(pythonRunRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runPythonOnAgent(pythonRunRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", pythonRunRequest.getAgentId());
            AgentPythonExecutionAck ack = new AgentPythonExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/execute/python/{executionId}")
    public ResponseEntity<AgentPythonExecutionResponse> getPythonResponse(@PathVariable("executionId") String executionId) {
        return ResponseEntity.accepted().body(agentConnectionHandler.getPythonExecutionResponse(executionId));
    }

}
