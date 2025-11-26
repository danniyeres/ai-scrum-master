package org.example.aiscrummaster.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor@JsonIgnoreProperties(ignoreUnknown = true)

public class StoryDTO {
    private String summary;
    private String description;
    private Integer storyPoints;
    private String assignee;         // email
    private List<SubtaskDTO> subtasks;
    private String priority;
    private List<String> labels;
}
