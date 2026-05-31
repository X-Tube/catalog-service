package com.microservice.catalogservice.controller.dtos.responses;

import lombok.Builder;

import java.util.List;

@Builder
public record PaginatedVideoResponse(
        List<VideoPreviewResponse> content,
        int currentPage,
        int totalPages,
        long totalElements
) {
}
