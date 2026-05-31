package com.microservice.catalogservice.application.gateways;

import java.util.UUID;

public interface StorageGateway {
    String generateThumbnailURL(UUID id);
}
