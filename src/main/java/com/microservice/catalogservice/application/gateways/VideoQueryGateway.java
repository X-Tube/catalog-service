package com.microservice.catalogservice.application.gateways;

import com.microservice.catalogservice.domain.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VideoQueryGateway {
    Page<Video> getRandomVideosForFeed(Pageable pageable);
}
