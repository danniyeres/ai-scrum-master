package org.example.aiscrummaster.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aiscrummaster.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class JiraIntegrationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${jira.url}")           private String jiraUrl;
    @Value("${jira.username}")      private String jiraEmail;
    @Value("${jira.apiToken}")      private String jiraToken;
    @Value("${jira.fields.story-points:customfield_10028}")
    private String storyPointsField;

    private final Map<String, String> emailToAccountId = new ConcurrentHashMap<>();

    public Map<String, String> createIssuesFromAi(DecompositionResponseDTO aiResponse, String projectKey) {
        Map<String, String> summaryToKey = new HashMap<>();
        String epicKey = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString((jiraEmail + ":" + jiraToken).getBytes()));

        // 1. Создаём Epic (если есть)
        if (aiResponse.getEpic() != null && aiResponse.getEpic().getSummary() != null) {
            Map<String, Object> fields = new HashMap<>();
            fields.put("project", Map.of("key", projectKey));
            fields.put("summary", aiResponse.getEpic().getSummary());
            fields.put("description", toAdf(aiResponse.getEpic().getDescription()));
            fields.put("issuetype", Map.of("name", "Epic"));

            epicKey = createIssue(Map.of("fields", fields), headers);
            summaryToKey.put(aiResponse.getEpic().getSummary(), epicKey);
            log.info("Epic создан: {}", epicKey);
        }

        // Если эпика нет — выходим
        if (epicKey == null) {
            throw new IllegalStateException("Не удалось создать Epic — проверь ТЗ и права");
        }

        // 2. Stories
        if (aiResponse.getStories() != null) {
            for (StoryDTO story : aiResponse.getStories()) {
                if (story.getSummary() == null) continue;

                Map<String, Object> fields = new HashMap<>();
                fields.put("project", Map.of("key", projectKey));
                fields.put("summary", story.getSummary());
                fields.put("description", toAdf(story.getDescription()));
                fields.put("issuetype", Map.of("name", "Story"));

                // Привязка к эпику — БЕЗОПАСНО!
                fields.put("parent", Map.of("key", epicKey));

                // Story Points — только если > 0
                if (story.getStoryPoints() != null && story.getStoryPoints() > 0) {
                    fields.put(storyPointsField, story.getStoryPoints());
                }

                // Assignee — только если email валидный
                if (story.getAssignee() != null && !story.getAssignee().trim().isEmpty()) {
                    String accountId = getAccountIdByEmail(story.getAssignee().trim());
                    if (accountId != null) {
                        fields.put("assignee", Map.of("accountId", accountId));
                    }
                }

                String storyKey = createIssue(Map.of("fields", fields), headers);
                summaryToKey.put(story.getSummary(), storyKey);
                log.info("Story: {} → {}", story.getSummary(), storyKey);

                // 3. Sub-tasks
                if (story.getSubtasks() != null) {
                    for (SubtaskDTO sub : story.getSubtasks()) {
                        if (sub.getSummary() == null || sub.getSummary().isBlank()) continue;

                        Map<String, Object> subFields = new HashMap<>();
                        subFields.put("project", Map.of("key", projectKey));
                        subFields.put("summary", sub.getSummary());
                        subFields.put("description", toAdf(sub.getDescription()));
                        subFields.put("issuetype", Map.of("name", "Sub-task"));
                        subFields.put("parent", Map.of("key", storyKey)); // ← привязка к Story

                        // Наследуем assignee
                        if (story.getAssignee() != null) {
                            String accountId = getAccountIdByEmail(story.getAssignee());
                            if (accountId != null) {
                                subFields.put("assignee", Map.of("accountId", accountId));
                            }
                        }

                        createIssue(Map.of("fields", subFields), headers);
                    }
                }
            }
        }

        return summaryToKey;
    }

    private String createIssue(Map<String, Object> body, HttpHeaders headers) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            String json = restTemplate.postForObject(jiraUrl + "/rest/api/3/issue", entity, String.class);
            return mapper.readTree(json).path("key").asText();
        } catch (Exception e) {
            log.error("Ошибка создания задачи: {}", e.getMessage());
            throw new RuntimeException("Jira error: " + e.getMessage(), e);
        }
    }

    private String getAccountIdByEmail(String email) {
        return emailToAccountId.computeIfAbsent(email.toLowerCase(), e -> {
            try {
                String url = jiraUrl + "/rest/api/3/user/search?query=" + URLEncoder.encode(e, StandardCharsets.UTF_8);
                HttpHeaders h = new HttpHeaders();
                h.set("Authorization", h.getFirst("Authorization"));
                String resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(h), String.class).getBody();
                JsonNode node = mapper.readTree(resp);
                if (node.isArray() && !node.isEmpty()) {
                    return node.get(0).path("accountId").asText();
                }
            } catch (Exception ex) {
                log.warn("Пользователь не найден в Jira: {}", e);
            }
            return null;
        });
    }

    private Map<String, Object> toAdf(String text) {
        String content = (text == null || text.isBlank()) ? "Создано AI Scrum Master" : text.trim();
        return Map.of(
                "type", "doc",
                "version", 1,
                "content", List.of(
                        Map.of(
                                "type", "paragraph",
                                "content", List.of(Map.of("type", "text", "text", content))
                        )
                )
        );
    }
}