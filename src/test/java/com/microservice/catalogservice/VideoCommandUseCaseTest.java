package com.microservice.catalogservice;

import com.microservice.catalogservice.application.exceptions.VideoNotFoundException;
import com.microservice.catalogservice.application.gateways.VideoCommandGateway;
import com.microservice.catalogservice.application.usecases.command.VideoCommandUseCase;
import com.microservice.catalogservice.domain.Video;
import com.microservice.catalogservice.domain.VideoProgress;
import com.microservice.catalogservice.domain.enums.VideoStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class VideoCommandUseCaseTest {

    @Mock
    private VideoCommandGateway videoGateway;

    @InjectMocks
    private VideoCommandUseCase videoCommandUseCase;

    @Test
    @DisplayName("Should explicitly save video when it does not exist in the database")
    void shouldSaveVideoWhenNotExists() {
        UUID videoId = UUID.randomUUID();
        log.info("[TEST-START] Testing 'Create Video' logic for NEW Video ID: {}", videoId);

        Video newVideo = new Video();
        newVideo.setId(videoId);

        log.debug("Mocking Gateway: Forcing isVideoSaved to return FALSE (Video does not exist).");
        when(videoGateway.isVideoSaved(videoId)).thenReturn(false);

        log.info("[TEST-ACT] Executing createVideo with new entity...");
        videoCommandUseCase.createVideo(newVideo);

        log.info("[TEST-ASSERT] Verifying the Gateway was commanded to save the video exactly once.");
        verify(videoGateway, times(1)).saveVideo(newVideo);
        log.info("[TEST-SUCCESS] New video was successfully routed to the save method.");
    }

    @Test
    @DisplayName("Should safely ignore the command when video already exists")
    void shouldNotSaveVideoWhenAlreadyExists() {
        UUID videoId = UUID.randomUUID();
        log.info("[TEST-START] Testing 'Create Video' logic for EXISTING Video ID: {}", videoId);

        Video existingVideo = new Video();
        existingVideo.setId(videoId);

        log.debug("Mocking Gateway: Forcing isVideoSaved to return TRUE (Video already exists).");
        when(videoGateway.isVideoSaved(videoId)).thenReturn(true);

        log.info("[TEST-ACT] Executing createVideo with an entity that is already saved...");
        videoCommandUseCase.createVideo(existingVideo);

        log.info("[TEST-ASSERT] Verifying the Gateway save method was completely bypassed.");
        verify(videoGateway, never()).saveVideo(any(Video.class));
        log.info("[TEST-SUCCESS] Duplicate video creation was successfully prevented.");
    }

    @Test
    @DisplayName("Should mutate status to PROCESSING when Kafka reports 0% progress")
    void shouldUpdateStatusToProcessingWhenProgressIsZero() {
        UUID videoId = UUID.randomUUID();
        log.info("[TEST-START] Testing 0% (Start) progress event for Video ID: {}", videoId);

        VideoProgress progress = new VideoProgress(videoId, 0, null, null);
        Video existingVideo = new Video();
        existingVideo.setId(videoId);

        log.debug("Mocking Gateway: Forcing getVideoById to return the target video.");
        when(videoGateway.getVideoById(videoId)).thenReturn(existingVideo);

        log.info("[TEST-ACT] Sending 0% progress payload to Use Case...");
        videoCommandUseCase.handleProgress(progress);

        log.info("[TEST-ASSERT] Verifying Gateway captured the mutated entity.");
        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoGateway, times(1)).saveVideo(videoCaptor.capture());

        Video savedVideo = videoCaptor.getValue();
        log.debug("Captured Video Status: {}", savedVideo.getVideoStatus());

        assertThat(savedVideo.getVideoStatus()).isEqualTo(VideoStatus.PROCESSING);
        log.info("[TEST-SUCCESS] Video successfully mutated to PROCESSING state.");
    }

    @Test
    @DisplayName("Should mutate status to READY and attach CDN links when Kafka reports 100% progress")
    void shouldUpdateStatusToReadyWhenProgressIsOneHundred() {
        UUID videoId = UUID.randomUUID();
        String manifestUrl = "https://cdn.test.com/manifest.m3u8";
        String thumbnailUrl = "https://cdn.test.com/thumb.png";

        log.info("[TEST-START] Testing 100% (Complete) progress event for Video ID: {}", videoId);

        VideoProgress progress = new VideoProgress(videoId, 100, thumbnailUrl, manifestUrl);
        Video existingVideo = new Video();
        existingVideo.setId(videoId);

        log.debug("Mocking Gateway: Forcing getVideoById to return the target video.");
        when(videoGateway.getVideoById(videoId)).thenReturn(existingVideo);

        log.info("[TEST-ACT] Sending 100% progress payload to Use Case...");
        videoCommandUseCase.handleProgress(progress);

        log.info("[TEST-ASSERT] Verifying the Gateway was commanded to save the finalized video.");
        ArgumentCaptor<Video> videoCaptor = ArgumentCaptor.forClass(Video.class);
        verify(videoGateway, times(1)).saveVideo(videoCaptor.capture());

        Video savedVideo = videoCaptor.getValue();
        log.debug("Captured Video Status: {}", savedVideo.getVideoStatus());
        log.debug("Captured Manifest: {}", savedVideo.getManifestUrl());

        assertThat(savedVideo.getVideoStatus()).isEqualTo(VideoStatus.READY);
        assertThat(savedVideo.getManifestUrl()).isEqualTo(manifestUrl);
        assertThat(savedVideo.getThumbnailUrl()).isEqualTo(thumbnailUrl);
        log.info("[TEST-SUCCESS] Video successfully finalized to READY state with all CDN URLs.");
    }

    @Test
    @DisplayName("Should take no action and bypass saving when progress is intermediate (e.g., 50%)")
    void shouldDoNothingWhenProgressIsIntermediate() {
        UUID videoId = UUID.randomUUID();
        log.info("[TEST-START] Testing intermediate 50% progress event for Video ID: {}", videoId);

        VideoProgress progress = new VideoProgress(videoId, 50, null, null);
        Video existingVideo = new Video();
        existingVideo.setId(videoId);

        log.debug("Mocking Gateway: Forcing getVideoById to return the target video.");
        when(videoGateway.getVideoById(videoId)).thenReturn(existingVideo);

        log.info("[TEST-ACT] Sending 50% progress payload to Use Case...");
        videoCommandUseCase.handleProgress(progress);

        log.info("[TEST-ASSERT] Verifying the switch 'default' case triggered and database was bypassed.");
        verify(videoGateway, never()).saveVideo(any(Video.class));
        log.info("[TEST-SUCCESS] Intermediate progress was safely ignored to save database writes.");
    }

    @Test
    @DisplayName("Should gracefully swallow VideoNotFoundException to prevent Kafka listener crashes")
    void shouldSwallowExceptionWhenVideoNotFound() {
        UUID videoId = UUID.randomUUID();
        log.info("[TEST-START] Testing Ghost Event recovery for non-existent Video ID: {}", videoId);

        VideoProgress progress = new VideoProgress(videoId, 100, null, null);

        log.debug("Mocking Gateway: Forcing database to throw VideoNotFoundException.");
        when(videoGateway.getVideoById(videoId)).thenThrow(new VideoNotFoundException("Simulated Database Miss"));

        log.info("[TEST-ACT] Sending payload. Use Case should intercept the thrown exception...");
        videoCommandUseCase.handleProgress(progress);

        log.info("[TEST-ASSERT] Verifying application remained stable and write operations were aborted.");
        verify(videoGateway, never()).saveVideo(any(Video.class));
        log.info("[TEST-SUCCESS] Exception was successfully swallowed and logged. Listener remains active.");
    }
}