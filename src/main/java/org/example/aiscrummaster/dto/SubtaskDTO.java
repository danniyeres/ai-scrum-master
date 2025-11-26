package org.example.aiscrummaster.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor@JsonIgnoreProperties(ignoreUnknown = true)

public class SubtaskDTO {
    private String summary;
    private String description;
}
