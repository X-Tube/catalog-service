package com.microservice.catalogservice.infrastructure.persistence;

import com.microservice.catalogservice.infrastructure.persistence.entities.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface VideoRepository extends JpaRepository<VideoEntity, UUID> {
}
