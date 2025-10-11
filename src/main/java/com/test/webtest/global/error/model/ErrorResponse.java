package com.test.webtest.global.error.model;

import java.time.Instant;

public record ErrorResponse(
        String code, String message, Instant timeStamp
) {}
