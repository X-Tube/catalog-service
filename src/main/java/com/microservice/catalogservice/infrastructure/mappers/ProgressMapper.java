package com.microservice.catalogservice.infrastructure.mappers;

import com.microservice.catalogservice.domain.VideoProgress;
import com.microservice.catalogservice.infrastructure.gateways.payload.ProgressEventPayload;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProgressMapper {

    VideoProgress eventToDomain(ProgressEventPayload payload);
}
