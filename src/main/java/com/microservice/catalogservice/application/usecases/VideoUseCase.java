package com.microservice.catalogservice.application.usecases;

import com.microservice.catalogservice.domain.Video;
import com.microservice.catalogservice.domain.VideoProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoUseCase {

    public void createVideo(Video video) {
    }

    public void handleProgress(VideoProgress progress) {
    }
}
