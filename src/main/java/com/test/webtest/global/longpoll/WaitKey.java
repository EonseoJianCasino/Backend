package com.test.webtest.global.longpoll;

import java.util.Objects;
import java.util.UUID;

public final class WaitKey {
    private final UUID testId;
    private final LongPollingTopic topic;

    public WaitKey(UUID testId, LongPollingTopic topic) {
        this.testId = testId;
        this.topic = topic;
    }

    public UUID getTestId() { return testId; }
    public LongPollingTopic getTopic() { return topic; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaitKey)) return false;
        WaitKey that = (WaitKey) o;
        return Objects.equals(testId, that.testId) && topic == that.topic;
    }

    @Override
    public int hashCode() {return Objects.hash(testId, topic);}

    @Override
    public String toString() {return "WaitKey{" + testId + ", " + topic + '}';}
}
