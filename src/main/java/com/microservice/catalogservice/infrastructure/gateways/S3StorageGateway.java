package com.microservice.catalogservice.infrastructure.gateways;

import com.microservice.catalogservice.application.exceptions.CloudFrontException;
import com.microservice.catalogservice.application.gateways.StorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3StorageGateway implements StorageGateway {

    @Value("${cloudfront.resource-url}")
    private String resourceUrl;

    @Value("${cloudfront.private-key}")
    private String privateKey;

    @Value("${cloudfront.key-pair-id}")
    private String keyPairId;

    @Override
    public CookiesForCustomPolicy generateCookiesForCustomPolicy(UUID videoId) {
        try {
            CustomSignerRequest request = CustomSignerRequest.builder()
                    .resourceUrl(resourceUrl + videoId + "/*")
                    .privateKey(Paths.get(privateKey))
                    .keyPairId(keyPairId)
                    .expirationDate(Instant.now().plus(2, ChronoUnit.HOURS))
                    .build();

            CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

            return cloudFrontUtilities.getCookiesForCustomPolicy(request);
        } catch (Exception ex) {
            throw new CloudFrontException("Error generating CloudFront signed cookies: " + ex.getMessage());
        }
    }
}
