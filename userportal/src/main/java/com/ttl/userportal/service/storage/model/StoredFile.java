package com.ttl.userportal.service.storage.model;

public record StoredFile(
        String originalName,
        String storedName,
        String path,
        long size,
        String contentType
) {}
