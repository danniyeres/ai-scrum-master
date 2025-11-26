package org.example.aiscrummaster.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aiscrummaster.dto.DecompositionResponseDTO;
import org.example.aiscrummaster.dto.TeamMemberDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final TeamService teamService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();

    @Value("${openrouter.base-url}")
    private String URL;
    @Value("${openrouter.api-key}")
    private String API_KEY;
    @Value("${openrouter.model:x-ai/grok-4.1-fast}")
    private String MODEL;

    public AiService(TeamService teamService) {
        this.teamService = teamService;
    }

    public DecompositionResponseDTO decompose(String technicalSpec) {

        List<TeamMemberDto> members = teamService.getAllTeamMembers();
        String membersJson;
        try {
            membersJson = mapper.writeValueAsString(
                    members.stream()
                            .map(m -> Map.of(
                                    "name", m.getFullName(),
                                    "email", m.getEmail(),
                                    "role", m.getRole()
                            ))
                            .toList()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize members", e);
        }

        String prompt = """
                Ты — Senior Scrum Master и Solution Architect в крупном банке.
                
                Команда:
                %s
                
                Техническое задание:
                %s
                
                Выполни декомпозицию задачи для Jira (Company-managed проект).
                
                Требования:
                - Один Epic
                - 4–8 User Stories
                - У каждой Story — 3–6 Sub-tasks
                - Оценивай в Story Points (только 1, 2, 3, 5, 8, 13, 21)
                - Назначай исполнителя по email из списка команды (обязательно!)
                - Если не уверен — оставь assignee пустым ("assignee": "")
                
                ОТВЕЧАЙ ТОЛЬКО валидным JSON. НИКАКИХ пояснений, никакого markdown, никаких ```json.
                
                СТРОГАЯ СТРУКТУРА (не меняй имена полей!):
                
                {
                  "epic": {
                    "summary": "Краткое название эпика",
                    "description": "Подробное описание цели и бизнес-ценности"
                  },
                  "stories": [
                    {
                      "summary": "Как [кто], я хочу [цель], чтобы [зачем]"
                      "description": "Подробное описание acceptance criteria и деталей",
                      "storyPoints": 8,
                      "assignee": "ivanov@bank.com",
                      "subtasks": [
                        { "summary": "Краткое название подзадачи", "description": "Опционально — детали" },
                        { "summary": "Ещё одна подзадача", "description": "" }
                      ]
                    }
                  ]
                }
                Начинай прямо с { — без переноса строки!
                """.formatted(membersJson, technicalSpec);


        Map<String, Object> body = Map.of(
                "model",MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "Ты — Senior Scrum Master. Отвечай строго JSON."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = rest.exchange(
                URL,
                HttpMethod.POST,
                request,
                String.class
        );

        String raw = response.getBody();

        try {
            JsonNode root = mapper.readTree(raw);
            String content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return mapper.readValue(content, DecompositionResponseDTO.class);

        } catch (Exception e) {
            throw new RuntimeException("Error parsing AI response", e);
        }
    }
}
