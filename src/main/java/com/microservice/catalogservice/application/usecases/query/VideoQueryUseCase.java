package com.microservice.catalogservice.application.usecases.query;

import com.microservice.catalogservice.application.gateways.VideoQueryGateway;
import com.microservice.catalogservice.application.usecases.CookieUseCase;
import com.microservice.catalogservice.application.usecases.payloads.WatchVideoResult;
import com.microservice.catalogservice.controller.dtos.responses.PaginatedVideoResponse;
import com.microservice.catalogservice.controller.dtos.responses.VideoPreviewResponse;
import com.microservice.catalogservice.domain.Video;
import com.microservice.catalogservice.infrastructure.mappers.VideoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoQueryUseCase {

    private final VideoQueryGateway videoQueryGateway;
    private final CookieUseCase cookieUseCase;
    private final VideoMapper videoMapper;

    @Transactional(readOnly = true)
    public PaginatedVideoResponse getVideosForFeed(int size, int page) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Video> videos = videoQueryGateway.getVideosForFeed(pageable);

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

    @Transactional(readOnly = true)
    public WatchVideoResult watchVideo(
            UUID videoId
    ) {
        var video = videoQueryGateway.getVideoById(videoId);
        log.info("[VideoUseCase] Video fetched with id: {}", videoId);

        var cookies = cookieUseCase.generateCloudFrontCookies(videoId);
        log.info("[CookieUseCase] CloudFront cookies generated for video: {}", videoId);

        return new WatchVideoResult(
                videoMapper.domainToResponse(video),
                cookies
        );
    }
}
