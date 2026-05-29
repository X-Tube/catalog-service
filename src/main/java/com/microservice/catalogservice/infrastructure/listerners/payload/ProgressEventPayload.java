package com.microservice.catalogservice.infrastructure.listerners.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProgressEventPayload(
    @JsonProperty("video_id")
    @NotNull UUID videoId,

    @JsonProperty("progress_percent")
    @NotNull Integer progressPercent
) {}
