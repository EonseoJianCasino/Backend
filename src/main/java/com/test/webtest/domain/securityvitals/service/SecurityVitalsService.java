package com.test.webtest.domain.securityvitals.service;

import java.util.UUID;

public interface SecurityVitalsService {
    void scanAndSave(UUID testId);
}
