package com.microservice.catalogservice.infrastructure.persistence.entities;

import com.microservice.catalogservice.domain.enums.VideoStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    private Long author;

    private String title;

    private String description;

    private VideoStatus videoStatus;

    private Long duration;

    private Long size;
}
