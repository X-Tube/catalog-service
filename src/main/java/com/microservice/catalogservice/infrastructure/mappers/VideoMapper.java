package com.microservice.catalogservice.infrastructure.mappers;

import com.microservice.catalogservice.domain.Video;
import com.microservice.catalogservice.infrastructure.gateways.payload.VideoEventPayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    Video eventToDomain(VideoEventPayload payload);
}
