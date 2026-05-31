package com.microservice.catalogservice.infrastructure.listerners;

import com.microservice.catalogservice.application.usecases.command.VideoUseCase;
import com.microservice.catalogservice.infrastructure.listerners.payload.VideoEventPayload;
import com.microservice.catalogservice.infrastructure.listerners.payload.ProgressEventPayload;
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
            groupId = "${spring.kafka.consumer.group-id:catalog-group}"
    )
    public void createVideo(VideoEventPayload payload) {
        log.info("[KAFKA] Received Video Uploaded Event for Video ID: {}", payload.id());

        var video = videoMapper.eventToDomain(payload);
        videoUseCase.createVideo(video);

        log.info("[KAFKA] Successfully processed upload event for Video ID: {}", video.getId());
    }

    @KafkaListener(
            topics = "${kafka.topic.processing}",
            groupId = "${spring.kafka.consumer.group-id:catalog-group}"
    )
    public void videoProgress(ProgressEventPayload payload) {
        var progress = progressMapper.eventToDomain(payload);

        videoUseCase.handleProgress(progress);
        log.info("[KAFKA] Received Video Processing Event for Video ID: {}", progress.getVideoId());
    }
}