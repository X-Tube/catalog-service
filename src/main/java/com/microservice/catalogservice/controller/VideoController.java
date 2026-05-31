package com.microservice.catalogservice.controller;

import com.microservice.catalogservice.application.usecases.query.VideoQueryUseCase;
import com.microservice.catalogservice.controller.dtos.responses.PaginatedVideoResponse;
import com.microservice.catalogservice.controller.dtos.responses.VideoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
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

    @GetMapping("/{videoId}/watch")
    public ResponseEntity<VideoResponse> watchVideo(
            @PathVariable UUID videoId,
            HttpServletResponse response
    ) {
        var result = videoQueryUseCase.watchVideo(videoId);
        result.cookies().forEach(cookie -> response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString()));

        return ResponseEntity.ok(result.video());
    }
}
