package com.microservice.catalogservice;

import com.microservice.catalogservice.application.exceptions.VideoNotFoundException;
import com.microservice.catalogservice.domain.Video;
import com.microservice.catalogservice.domain.enums.VideoStatus;

import com.microservice.catalogservice.infrastructure.gateways.VideoQueryGatewayRepository;
import com.microservice.catalogservice.infrastructure.persistence.VideoRepository;
import com.microservice.catalogservice.infrastructure.persistence.entities.VideoEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class VideoQueryGatewayIT extends BaseIntegrationTest {

    @Autowired
    private VideoQueryGatewayRepository queryGateway;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        log.info("[TEST-SETUP] Wiping PostgreSQL and Redis to ensure clean state.");
        videoRepository.deleteAll();
        cacheManager.getCache("videos").clear();
    }

    @Test
    @DisplayName("Should successfully fetch a paginated list of videos")
    void shouldFetchPaginatedFeed() {
        log.info("[TEST-START] Testing Database Pagination logic...");

        for (int i = 0; i < 3; i++) {
            VideoEntity entity = new VideoEntity();
            entity.setId(UUID.randomUUID());
            entity.setTitle("Test Video " + i);

            entity.setAuthor(1L);
            entity.setDescription("Test Description");
            entity.setDuration(120L);
            entity.setVideoStatus(VideoStatus.READY);
            entity.setSize(1024L);

            videoRepository.save(entity);
        }

        PageRequest pageRequest = PageRequest.of(0, 2);

        log.info("[TEST-ACT] Querying the Gateway for Page 0 with Size 2.");
        Page<Video> resultPage = queryGateway.getVideosForFeed(pageRequest);

        log.info("[TEST-ASSERT] Verifying PostgreSQL returned the exact page constraints.");
        assertThat(resultPage.getContent()).hasSize(2);
        assertThat(resultPage.getTotalElements()).isEqualTo(3);
        assertThat(resultPage.getTotalPages()).isEqualTo(2);
        log.info("[TEST-SUCCESS] Pagination executed flawlessly.");
    }

    @Test
    @DisplayName("Should throw VideoNotFoundException if UUID does not exist")
    void shouldThrowExceptionWhenVideoNotFound() {
        UUID fakeId = UUID.randomUUID();
        log.info("[TEST-START] Testing 404 logic for Ghost Video ID: {}", fakeId);

        log.info("[TEST-ACT & ASSERT] Requesting non-existent video and expecting domain exception.");

        assertThatThrownBy(() -> queryGateway.getVideoById(fakeId))
                .isInstanceOf(VideoNotFoundException.class)
                .hasMessageContaining("not found");

        log.info("[TEST-SUCCESS] The exception safely bypassed the generic Postgres try-catch block.");
    }

    @Test
    @DisplayName("Should cache the database result in Redis upon first request")
    void shouldCacheVideoInRedis() {
        UUID videoId = UUID.randomUUID();
        log.info("[TEST-START] Testing Redis caching layer for Video ID: {}", videoId);

        VideoEntity entity = new VideoEntity();
        entity.setId(videoId);
        entity.setTitle("Cached Video");

        entity.setAuthor(1L);
        entity.setDescription("Test Description");
        entity.setDuration(120L);
        entity.setVideoStatus(VideoStatus.READY);
        entity.setSize(1024L);

        videoRepository.save(entity);

        log.info("[TEST-ACT] Fetching video for the first time (Cache Miss -> DB Hit).");
        Video firstResult = queryGateway.getVideoById(videoId);

        log.info("[TEST-ASSERT] Verifying Redis intercepted the result and stored it.");

        Video cachedVideo = cacheManager.getCache("videos").get(videoId, Video.class);

        assertThat(cachedVideo).isNotNull();
        assertThat(cachedVideo.getId()).isEqualTo(firstResult.getId());
        assertThat(cachedVideo.getTitle()).isEqualTo("Cached Video");

        log.info("[TEST-SUCCESS] Video was successfully serialized and stored in the cache!");
    }
}