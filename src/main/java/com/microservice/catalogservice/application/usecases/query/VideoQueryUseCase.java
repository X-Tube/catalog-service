package com.microservice.catalogservice.application.usecases.query;

import com.microservice.catalogservice.application.gateways.StorageGateway;
import com.microservice.catalogservice.application.gateways.VideoQueryGateway;
import com.microservice.catalogservice.controller.dtos.responses.PaginatedVideoResponse;
import com.microservice.catalogservice.controller.dtos.responses.VideoPreviewResponse;
import com.microservice.catalogservice.domain.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoQueryUseCase {

    private final VideoQueryGateway videoQueryGateway;
    private final StorageGateway storageGateway;

    @Transactional(readOnly = true)
    public PaginatedVideoResponse getVideosForFeed(int size, int page) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Video> videos = videoQueryGateway.getRandomVideosForFeed(pageable);

        List<VideoPreviewResponse> content = videos.getContent().stream()
                .map(video -> {
                    return VideoPreviewResponse.builder()
                            .title(video.getTitle())
                            .videoId(video.getId())
                            .author(video.getAuthor())
                            .thumbnailURL(video.getThumbnailUrl())
                            .build();
                }).toList();

        return PaginatedVideoResponse.builder()
                .content(content)
                .currentPage(videos.getNumber())
                .totalElements(videos.getTotalElements())
                .totalPages(videos.getTotalPages())
                .build();
    }
}
