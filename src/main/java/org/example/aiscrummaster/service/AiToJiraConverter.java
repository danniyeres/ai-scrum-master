package org.example.aiscrummaster.service;

import lombok.RequiredArgsConstructor;
import org.example.aiscrummaster.dto.DecompositionResponseDTO;
import org.example.aiscrummaster.dto.StoryDTO;
import org.example.aiscrummaster.dto.SubtaskDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiToJiraConverter {

    @Value("${jira.fields.story-points:customfield_10028}")
    private String storyPointsField;

    public List<Map<String, Object>> convert(DecompositionResponseDTO dto, String projectKey) {
        List<Map<String, Object>> issues = new ArrayList<>();
        String epicKeyPlaceholder = "EPIC_PLACEHOLDER";

        // 1. Epic
        if (dto.getEpic() != null) {
            Map<String, Object> fields = new HashMap<>(); // ← HashMap, а не Map.of!
            fields.put("project", Map.of("key", projectKey));
            fields.put("summary", dto.getEpic().getSummary());
            fields.put("description", toAdf(dto.getEpic().getDescription()));
            fields.put("issuetype", Map.of("name", "Epic"));

            issues.add(Map.of(
                    "fields", fields,
                    "issueType", "Epic",
                    "summary", dto.getEpic().getSummary()
            ));
        }

        // 2. Stories + Sub-tasks
        if (dto.getStories() != null) {
            for (StoryDTO story : dto.getStories()) {

                // Story
                Map<String, Object> storyFields = new HashMap<>();
                storyFields.put("project", Map.of("key", projectKey));
                storyFields.put("summary", story.getSummary());
                storyFields.put("description", toAdf(story.getDescription()));
                storyFields.put("issuetype", Map.of("name", "Story"));
                storyFields.put("parent", Map.of("key", epicKeyPlaceholder)); // потом заменим

                if (story.getStoryPoints() > 0) {
                    storyFields.put(storyPointsField, story.getStoryPoints());
                }
                if (story.getAssignee() != null) {
                    storyFields.put("assignee", Map.of("tempEmail", story.getAssignee()));
                }

                assert story.getAssignee() != null;
                issues.add(Map.of(
                        "fields", storyFields,
                        "issueType", "Story",
                        "summary", story.getSummary(),
                        "tempAssignee", story.getAssignee()
                ));

                // Sub-tasks
                if (story.getSubtasks() != null) {
                    for (SubtaskDTO sub : story.getSubtasks()) {
                        Map<String, Object> subFields = new HashMap<>();
                        subFields.put("project", Map.of("key", projectKey));
                        subFields.put("summary", sub.getSummary());
                        subFields.put("description", toAdf(""));
                        subFields.put("issuetype", Map.of("name", "Sub-task"));
                        subFields.put("parent", Map.of("key", "STORY_" + story.getSummary().hashCode())); // временный

                        if (story.getAssignee() != null) {
                            subFields.put("assignee", Map.of("tempEmail", story.getAssignee()));
                        }

                        issues.add(Map.of("fields", subFields, "issueType", "Sub-task"));
                    }
                }
            }
        }
        return issues;
    }

    private Map<String, Object> toAdf(String text) {
        String content = text == null || text.isBlank() ? "Создано AI Scrum Master" : text;
        return new HashMap<>(Map.of( // ← тоже HashMap!
                "type", "doc",
                "version", 1,
                "content", List.of(Map.of(
                        "type", "paragraph",
                        "content", List.of(Map.of("type", "text", "text", content))
                ))
        ));
    }
}