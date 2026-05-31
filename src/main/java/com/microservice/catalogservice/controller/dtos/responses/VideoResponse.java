package com.microservice.catalogservice.controller.dtos.responses;

import java.util.UUID;

public record VideoResponse(
        UUID videoId,
        Long author,
        String title,
        String description,
        String thumbnailUrl,
        String manifestUrl
) {
}
