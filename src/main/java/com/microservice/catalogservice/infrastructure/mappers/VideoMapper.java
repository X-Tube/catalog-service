package com.microservice.catalogservice.infrastructure.mappers;

import com.microservice.catalogservice.controller.dtos.responses.VideoResponse;
import com.microservice.catalogservice.domain.Video;
import com.microservice.catalogservice.infrastructure.listerners.payload.VideoEventPayload;
import com.microservice.catalogservice.infrastructure.persistence.entities.VideoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    Video eventToDomain(VideoEventPayload payload);

    VideoEntity domainToEntity(Video video);

    Video entityToDomain(VideoEntity videoEntity);

    @Mapping(target = "videoId", source = "video.id")
    VideoResponse domainToResponse(Video video);
}
