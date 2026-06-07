package com.microservice.catalogservice.application.usecases;

import com.microservice.catalogservice.application.gateways.CloudfrontGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CookieUseCase {

    @Value("${server.domain}")
    private String domainUrl;

    private final CloudfrontGateway storageGateway;

    public List<ResponseCookie> generateCloudFrontCookies(UUID videoId) {
        var awsCookies = storageGateway.generateCookiesForCustomPolicy(videoId);

        return List.of(
                buildCookie("CloudFront-Policy", extractValue(awsCookies.policyHeaderValue())),
                buildCookie("CloudFront-Signature", extractValue(awsCookies.signatureHeaderValue())),
                buildCookie("CloudFront-Key-Pair-Id", extractValue(awsCookies.keyPairIdHeaderValue()))
        );
    }

    private String extractValue(String awsString) {
        return awsString.split("=", 2)[1];
    }

    private ResponseCookie buildCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .domain(domainUrl)
                .path("/")
                .sameSite("Strict")
                .maxAge(7200)
                .build();
    }
}