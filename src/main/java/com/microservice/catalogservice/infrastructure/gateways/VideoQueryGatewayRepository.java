package com.microservice.catalogservice.infrastructure.gateways;

import com.microservice.catalogservice.application.exceptions.PostgreSqlException;
import com.microservice.catalogservice.application.exceptions.VideoNotFoundException;
import com.microservice.catalogservice.application.gateways.VideoQueryGateway;
import com.microservice.catalogservice.domain.Video;
import com.microservice.catalogservice.infrastructure.mappers.VideoMapper;
import com.microservice.catalogservice.infrastructure.persistence.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoQueryGatewayRepository implements VideoQueryGateway {

    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    @Override
    public Page<Video> getVideosForFeed(Pageable pageable) {
        try {
            return videoRepository.findAll(pageable)
                    .map(videoMapper::entityToDomain);
        } catch (Exception ex) {
            throw throwDbExceptions(ex);
        }
    }

    @Cacheable(value = "videos", key = "#videoId")
    @Override
    public Video getVideoById(UUID videoId) {
        try {
            return videoRepository.findById(videoId)
                    .map(videoMapper::entityToDomain)
                    .orElseThrow(() -> new VideoNotFoundException("Video with id: " + videoId + " not found"));
        } catch (VideoNotFoundException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw throwDbExceptions(ex);
        }
    }

    private PostgreSqlException throwDbExceptions(Exception ex) {
        return new PostgreSqlException("Error while trying to execute the action: " + ex.getMessage());
    }
}
