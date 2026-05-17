package com.maximys.nexus.client.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

// Информация об обновления из GitHub Releases API

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubReleaseInfo {
    
    @JsonProperty("tag_name")
    private String tagName; // Например, "v0.3.0"

    @JsonProperty("body")
    private String body; // Текст описания релиза (Changelog)

}
