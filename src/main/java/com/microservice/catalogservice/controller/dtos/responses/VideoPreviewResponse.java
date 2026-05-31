package com.microservice.catalogservice.controller.dtos.responses;

import lombok.Builder;

import java.util.UUID;

@Builder
public record VideoPreviewResponse(
        UUID videoId,
        Long author,
        String title,
        String thumbnailURL
) {
}
