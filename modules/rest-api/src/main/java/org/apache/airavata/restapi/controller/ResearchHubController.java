package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.airavata.research.session.dto.RedirectResponse;
import org.apache.airavata.research.session.service.ResearchSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/research-hub")
@Tag(name = "Research Hub", description = "Research Hub Operations")
public class ResearchHubController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchHubController.class);

    private final ResearchSessionService sessionService;

    public ResearchHubController(ResearchSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/start/project/{projectId}")
    @Operation(summary = "Spawn new project session")
    public ResponseEntity<RedirectResponse> spawnSession(
            @PathVariable("projectId") String projectId, @RequestParam("sessionName") String sessionName) {
        LOGGER.info("Spawning session ({}) for project: {}", sessionName, projectId);
        var spawnUrl = sessionService.spawnSession(projectId, sessionName);
        LOGGER.info("Session spawned: {}", spawnUrl);
        return ResponseEntity.ok(new RedirectResponse(spawnUrl));
    }

    @GetMapping("/resume/session/{sessionId}")
    @Operation(summary = "Resume an existing session")
    public ResponseEntity<RedirectResponse> resumeSession(@PathVariable("sessionId") String sessionId) {
        LOGGER.info("Resuming session: {}", sessionId);
        var sessionUrl = sessionService.resumeSession(sessionId);
        LOGGER.info("Resume success: {}", sessionUrl);
        return ResponseEntity.ok(new RedirectResponse(sessionUrl));
    }
}
