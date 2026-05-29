package com.microservice.catalogservice.application.usecases;

import com.microservice.catalogservice.application.exceptions.VideoNotFoundException;
import com.microservice.catalogservice.application.gateways.VideoGateway;
import com.microservice.catalogservice.domain.Video;
import com.microservice.catalogservice.domain.VideoProgress;
import com.microservice.catalogservice.domain.enums.VideoStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoUseCase {

    private final VideoGateway videoGateway;

    public void createVideo(Video video) {
        if (!videoGateway.isVideoSaved(video.getId())) {
            saveVideo(video);
        }
    }

    public void handleProgress(VideoProgress progress) {
        try {
            var video = getVideo(progress.getVideoId());

            switch (progress.getProgressPercent()) {
                case 0:
                    video.setVideoStatus(VideoStatus.PROCESSING);
                    saveVideo(video);
                    break;

                case 100:
                    video.setVideoStatus(VideoStatus.READY);
                    saveVideo(video);
                    break;

                default:
                    break;
            }
        } catch (VideoNotFoundException ex) {
            log.warn("[CATALOG] Received progress for unknown Video ID: {}. Ignoring message.", progress.getVideoId());
        }
    }

    private Video getVideo(UUID videoId) {
        return videoGateway.getVideoById(videoId);
    }

    private void saveVideo(Video video) {
        videoGateway.saveVideo(video);
    }
}
