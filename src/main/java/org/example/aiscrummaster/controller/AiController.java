package org.example.aiscrummaster.controller;

import org.example.aiscrummaster.dto.DecompositionResponseDTO;

import org.example.aiscrummaster.service.AiService;
import org.example.aiscrummaster.service.AiToJiraConverter;
import org.example.aiscrummaster.service.JiraIntegrationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final JiraIntegrationService jiraService;
    private final AiToJiraConverter aiToJiraConverter;

    public AiController(AiService aiService, JiraIntegrationService jiraService, AiToJiraConverter aiToJiraConverter) {
        this.aiService = aiService;
        this.jiraService = jiraService;
        this.aiToJiraConverter = aiToJiraConverter;
    }

    @PostMapping("/decompose")
    public Map<String, String> decomposeAndCreate(@RequestBody String technicalSpec) {

        String projectKey = "ASM";
        DecompositionResponseDTO decomposition = aiService.decompose(technicalSpec);
        return jiraService.createIssuesFromAi(decomposition, projectKey);
    }

    @PostMapping
    public DecompositionResponseDTO decompose(@RequestBody String technicalSpec) {
        return aiService.decompose(technicalSpec);
    }

    @PostMapping("/toJira")
    public List<Map<String, Object>> convert(@RequestBody String technicalSpec) {
        DecompositionResponseDTO decomposition = aiService.decompose(technicalSpec);
        return aiToJiraConverter.convert(decomposition, "ASM");
    }
}

