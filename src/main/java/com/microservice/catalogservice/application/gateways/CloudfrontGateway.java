package com.microservice.catalogservice.application.gateways;

import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;

import java.util.UUID;

public interface CloudfrontGateway {
    CookiesForCustomPolicy generateCookiesForCustomPolicy(UUID videoId);
}
