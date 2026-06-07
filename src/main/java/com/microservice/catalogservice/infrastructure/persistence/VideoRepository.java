package com.microservice.catalogservice.infrastructure.persistence;

import com.microservice.catalogservice.infrastructure.persistence.entities.VideoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<VideoEntity, UUID> {

    @Query("""
    SELECT v
    FROM VideoEntity v
    WHERE v.videoStatus = VideoStatus.READY
""")
    Page<VideoEntity> findAllReadyVideos(Pageable pageable);
}
