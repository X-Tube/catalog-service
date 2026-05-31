package com.microservice.catalogservice;

import com.microservice.catalogservice.application.usecases.CookieUseCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CookieUseCaseIT extends BaseIntegrationTest {

    @Autowired
    private CookieUseCase cookieUseCase;

    @Test
    @DisplayName("Should generate REAL cryptographic CloudFront cookies using the AWS SDK")
    void shouldGenerateRealCloudFrontCookies() {
        // 1. ARRANGE
        UUID videoId = UUID.randomUUID();
        log.info("[TEST-START] Generating CloudFront cookies for Video ID: {}", videoId);

        // 2. ACT
        long startTime = System.currentTimeMillis();
        List<ResponseCookie> resultCookies = cookieUseCase.generateCloudFrontCookies(videoId);
        long executionTime = System.currentTimeMillis() - startTime;

        log.info("[TEST-EXECUTION] RSA Cryptography completed in {} ms", executionTime);

        // 3. ASSERT
        assertThat(resultCookies).hasSize(3);
        log.info("[TEST-ASSERT] Successfully generated exactly 3 cookies.");

        ResponseCookie policyCookie = getCookieByName(resultCookies, "CloudFront-Policy");
        ResponseCookie signatureCookie = getCookieByName(resultCookies, "CloudFront-Signature");
        ResponseCookie keyPairCookie = getCookieByName(resultCookies, "CloudFront-Key-Pair-Id");

        log.debug("Policy Cookie Domain: {}", policyCookie.getDomain());
        log.debug("Key Pair ID injected: {}", keyPairCookie.getValue());

        assertThat(policyCookie.isHttpOnly()).isTrue();
        assertThat(policyCookie.isSecure()).isTrue();
        assertThat(policyCookie.getDomain()).isEqualTo("api.xtube.com");

        assertThat(keyPairCookie.getValue()).isEqualTo("123");
        assertThat(policyCookie.getValue()).isNotBlank();
        assertThat(signatureCookie.getValue()).isNotBlank();

        log.info("[TEST-SUCCESS] All cryptographic and security assertions passed flawlessly.");
    }

    private ResponseCookie getCookieByName(List<ResponseCookie> cookies, String name) {
        return cookies.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("[TEST-FAILED] Required cookie missing: {}", name);
                    return new AssertionError("Cookie " + name + " not found");
                });
    }
}