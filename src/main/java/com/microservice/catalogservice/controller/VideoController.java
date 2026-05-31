package com.microservice.catalogservice.controller;

import com.microservice.catalogservice.application.usecases.query.VideoQueryUseCase;
import com.microservice.catalogservice.controller.dtos.responses.PaginatedVideoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoQueryUseCase videoQueryUseCase;

    @GetMapping("/feed")
    public ResponseEntity<PaginatedVideoResponse> getVideosForFeed(
            @RequestParam int size,
            @RequestParam int page
    ) {
        return ResponseEntity.ok(videoQueryUseCase.getVideosForFeed(size, page));
    }
}
