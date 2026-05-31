package com.microservice.catalogservice.application.usecases.payloads;

import com.microservice.catalogservice.controller.dtos.responses.VideoResponse;
import org.springframework.http.ResponseCookie;

import java.util.List;

public record WatchVideoResult(
        VideoResponse video,
        List<ResponseCookie> cookies
) {
}
