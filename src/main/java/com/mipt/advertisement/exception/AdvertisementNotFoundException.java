package com.mipt.advertisement.exception;

import java.util.UUID;

public class AdvertisementNotFoundException extends RuntimeException {

    public AdvertisementNotFoundException(UUID id) {
        super("Advertisement not found with id: " + id);
    }

    public AdvertisementNotFoundException(String message) {
        super(message);
    }
}