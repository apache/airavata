package org.apache.airavata.agent.connection.service.controllers;

import org.apache.airavata.agent.connection.service.handlers.AgentHandler;
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

    private AgentHandler agentHandler;

    public AgentController(AgentHandler agentHandler) {
        this.agentHandler = agentHandler;
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<AgentInfoResponse> getAgentInfo(@PathVariable("agentId") String agentId) {
        return ResponseEntity.accepted().body(agentHandler.isAgentUp(agentId));
    }

    @PostMapping("/tunnel")
    public ResponseEntity<AgentTunnelAck> runTunnelCreationOnAgent(@Valid @RequestBody AgentTunnelCreationRequest tunnelRequest) {
        return ResponseEntity.accepted().body(agentHandler.runTunnelOnAgent(tunnelRequest));
    }
    @PostMapping("/execute")
    public ResponseEntity<AgentCommandAck> runCommandOnAgent(@Valid @RequestBody AgentCommandRequest commandRequest) {
        logger.info("Received command request to run on agent {}", commandRequest.getAgentId());
        if (agentHandler.isAgentUp(commandRequest.getAgentId()).isAgentUp()) {
            return ResponseEntity.accepted().body(agentHandler.runCommandOnAgent(commandRequest));
        } else {
            logger.warn("No agent is available to run on agent {}", commandRequest.getAgentId());
            AgentCommandAck ack = new AgentCommandAck();
            ack.setError("Agent not found");
            return ResponseEntity.accepted().body(ack);
        }
    }

}
