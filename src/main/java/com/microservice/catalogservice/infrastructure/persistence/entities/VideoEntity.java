package com.microservice.catalogservice.infrastructure.persistence.entities;

import com.microservice.catalogservice.domain.enums.VideoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "videos")
public class VideoEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Long author;

    private String title;

    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoStatus videoStatus;

    @Column(nullable = false)
    private Long duration;

    @Column(nullable = false)
    private Long size;

    private String thumbnailUrl;

    private String manifestUrl;
}
