package com.microservice.catalogservice.infrastructure.gateways;

import com.microservice.catalogservice.application.exceptions.VideoNotFoundException;
import com.microservice.catalogservice.application.gateways.VideoCommandGateway;
import com.microservice.catalogservice.domain.Video;
import com.microservice.catalogservice.infrastructure.mappers.VideoMapper;
import com.microservice.catalogservice.infrastructure.persistence.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoGatewayRepository implements VideoCommandGateway {

    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    @Override
    public void saveVideo(Video video) {
        var entity = videoMapper.domainToEntity(video);
        videoRepository.save(entity);

        log.info("[PostgreSQL] New saved video with id: {}", video.getId());
    }

    @Override
    public Video getVideoById(UUID videoId) {
        return videoRepository.findById(videoId)
                .map(videoMapper::entityToDomain)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with id: " + videoId));
    }

    @Override
    public boolean isVideoSaved(UUID id) {
        return videoRepository.existsById(id);
    }
}
