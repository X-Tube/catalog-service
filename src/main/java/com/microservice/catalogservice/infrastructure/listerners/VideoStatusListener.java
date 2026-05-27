package com.microservice.catalogservice.infrastructure.listerners;

import com.microservice.catalogservice.application.usecases.VideoUseCase;
import com.microservice.catalogservice.infrastructure.gateways.payload.VideoEventPayload;
import com.microservice.catalogservice.infrastructure.gateways.payload.ProgressEventPayload;
import com.microservice.catalogservice.infrastructure.mappers.ProgressMapper;
import com.microservice.catalogservice.infrastructure.mappers.VideoMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoStatusListener {

    private final VideoUseCase videoUseCase;
    private final VideoMapper videoMapper;
    private final ProgressMapper progressMapper;

    @KafkaListener(
            topics = "${kafka.topic.upload}",
            groupId = "${kafka.group.id}"
    )
    public void createVideo(VideoEventPayload payload) {
        var video = videoMapper.eventToDomain(payload);

        videoUseCase.createVideo(video);
        log.info("[KAFKA] Received Video Uploaded Event for Video ID: {}", video.getId());
    }

    @KafkaListener(
            topics = "${kafka.topic.processing}",
            groupId = "${kafka.group.id}"
    )
    public void videoProgress(ProgressEventPayload payload) {
        var progress = progressMapper.eventToDomain(payload);

        videoUseCase.handleProgress(progress);
        log.info("[KAFKA] Received Video Processing Event for Video ID: {}", progress.getVideoId());
    }
}