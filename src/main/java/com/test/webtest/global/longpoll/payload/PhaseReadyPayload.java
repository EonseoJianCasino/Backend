package com.test.webtest.global.longpoll.payload;

import com.test.webtest.global.longpoll.LongPollingTopic;

import java.time.Instant;
import java.util.UUID;

public record PhaseReadyPayload (
        LongPollingTopic type,
        UUID testId,
        Instant at
){}
