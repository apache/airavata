package org.apache.airavata.agent.connection.service.controllers;

import org.apache.airavata.agent.connection.service.handlers.AgentConnectionHandler;
import org.apache.airavata.agent.connection.service.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final static Logger logger = LoggerFactory.getLogger(AgentController.class);

    private AgentConnectionHandler agentConnectionHandler;

    public AgentController(AgentConnectionHandler agentConnectionHandler) {
        this.agentConnectionHandler = agentConnectionHandler;
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<AgentInfoResponse> getAgentInfo(@PathVariable("agentId") String agentId) {
        return ResponseEntity.accepted().body(agentConnectionHandler.isAgentUp(agentId));
    }

    @PostMapping("/tunnel")
    public ResponseEntity<AgentTunnelAck> runTunnelCreationOnAgent(@Valid @RequestBody AgentTunnelCreationRequest tunnelRequest) {
        return ResponseEntity.accepted().body(agentConnectionHandler.runTunnelOnAgent(tunnelRequest));
    }

    @PostMapping("/executecommandrequest")
    public ResponseEntity<AgentCommandAck> runCommandOnAgent(@Valid @RequestBody AgentCommandRequest commandRequest) {
        logger.info("Received command request to run on agent {}", commandRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(commandRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runCommandOnAgent(commandRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", commandRequest.getAgentId());
            AgentCommandAck ack = new AgentCommandAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/executecommandresponse/{executionId}")
    public ResponseEntity<AgentCommandResponse> getExecutionResponse(@PathVariable("executionId") String executionId) {
        return ResponseEntity.accepted().body(agentConnectionHandler.getAgentCommandResponse(executionId));
    }

    @PostMapping("/executejupyterrequest")
    public ResponseEntity<JupyterExecutionAck> runJupyterOnAgent(@Valid @RequestBody JupyterExecutionRequest executionRequest) {
        logger.info("Received jupyter execution request to run on agent {}", executionRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(executionRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runJupyterOnAgent(executionRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", executionRequest.getAgentId());
            JupyterExecutionAck ack = new JupyterExecutionAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/executejupyterresponse/{executionId}")
    public ResponseEntity<JupyterExecutionResponse> getJupyterResponse(@PathVariable("executionId") String executionId) {
        return ResponseEntity.accepted().body(agentConnectionHandler.getJupyterExecutionResponse(executionId));
    }


    @PostMapping("/executepythonrequest")
    public ResponseEntity<AgentPythonRunAck> runPythonOnAgent(@Valid @RequestBody AgentPythonRunRequest pythonRunRequest) {
        logger.info("Received python execution request to run on agent {}", pythonRunRequest.getAgentId());
        if (agentConnectionHandler.isAgentUp(pythonRunRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentConnectionHandler.runPythonOnAgent(pythonRunRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", pythonRunRequest.getAgentId());
            AgentPythonRunAck ack = new AgentPythonRunAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

    @GetMapping("/executepythonresponse/{executionId}")
    public ResponseEntity<AgentPythonRunResponse> getPythonResponse(@PathVariable("executionId") String executionId) {
        return ResponseEntity.accepted().body(agentConnectionHandler.getPythonExecutionResponse(executionId));
    }

}
