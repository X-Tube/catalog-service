package com.microservice.catalogservice.application.gateways;

import com.microservice.catalogservice.domain.Video;

import java.util.UUID;

public interface VideoCommandGateway {
    void saveVideo(Video video);

    Video getVideoById(UUID videoId);

    boolean isVideoSaved(UUID id);
}
