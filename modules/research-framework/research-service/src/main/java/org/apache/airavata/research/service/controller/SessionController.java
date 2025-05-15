package org.apache.airavata.research.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.airavata.research.service.enums.SessionStatusEnum;
import org.apache.airavata.research.service.handlers.ResearchHubHandler;
import org.apache.airavata.research.service.handlers.SessionHandler;
import org.apache.airavata.research.service.model.UserContext;
import org.apache.airavata.research.service.model.entity.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rf/sessions")
@Tag(name = "Sessions", description = "All operations related to sessions (created from projects")
public class SessionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionController.class);

    @Autowired
    private SessionHandler sessionHandler;

    @GetMapping("/")
    @Operation(summary = "Get all sessions by session status and userId")
    public ResponseEntity<List<Session>> getSessions(@RequestParam(value="status", required = false) SessionStatusEnum status) {
        LOGGER.info("Getting all sessions for user: {}, status filter: {}", UserContext.userId(), status);
        String userId = UserContext.userId();
        List<Session> sessions;
        if (status == null) {
            sessions = sessionHandler.findAllByUserId(userId);
        } else {
            sessions = sessionHandler.findAllByUserIdAndStatus(userId, status);
        }
        return ResponseEntity.ok(sessions);
    }

    @PatchMapping("/{sessionId}")
    @Operation(summary = "Update a session's status")
    public ResponseEntity<Session> updateSessionStatus(@PathVariable(value="sessionId") String sessionId, @RequestParam(value="status") SessionStatusEnum status) {
        LOGGER.info("Updating session status for session: {} to {}", sessionId, status);
        return ResponseEntity.ok(sessionHandler.updateSessionStatus(sessionId, status));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete a session")
    public ResponseEntity<Boolean> deleteSessoin(@PathVariable(value="sessionId") String sessionId) {
        LOGGER.info("Deleting session session: {}", sessionId);
        sessionHandler.deleteSession(sessionId);

        return ResponseEntity.ok(Boolean.TRUE);
    }
}
